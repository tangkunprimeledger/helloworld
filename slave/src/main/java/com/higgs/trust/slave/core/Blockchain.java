package com.higgs.trust.slave.core;

import com.higgs.trust.evmcontract.core.Bloom;
import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.evmcontract.core.TransactionResultInfo;
import com.higgs.trust.evmcontract.datasource.DbSource;
import com.higgs.trust.evmcontract.db.BlockStore;
import com.higgs.trust.evmcontract.db.TransactionStore;
import com.higgs.trust.evmcontract.facade.BlockStoreAdapter;
import com.higgs.trust.evmcontract.trie.TrieImpl;
import com.higgs.trust.evmcontract.util.ByteUtil;
import com.higgs.trust.evmcontract.util.RLP;
import com.higgs.trust.evmcontract.util.RLPList;
import com.higgs.trust.slave.common.listener.CompositeTrustListener;
import com.higgs.trust.slave.common.listener.TrustListener;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author duhongming
 * @date 2018/11/30
 */
@Component
@Slf4j
public class Blockchain implements TrustListener, InitializingBean {

    private final BlockStore blockStore;
    private boolean initialized;
    private TransactionStore transactionStore;
    private BlockHeader lastBlockHeader;
    private Repository repositorySnapshot;
    private List<TransactionResultInfo> receipts;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private DbSource<byte[]> dbSource;
    @Autowired
    private Repository repository;

    private CompositeTrustListener listeners;

    public Blockchain() {
        this.blockStore = createBlockStore();
        this.listeners = new CompositeTrustListener();
    }

    private BlockStore createBlockStore() {
        BlockStore blockStore = new BlockStoreAdapter() {
            @Override
            public byte[] getBlockHashByNumber(long blockNumber, byte[] branchBlockHash) {
                Block block = blockRepository.getBlock(blockNumber);
                if (block != null) {
                    return Hex.decode(block.getBlockHeader().getBlockHash());
                }
                return null;
            }
        };
        return blockStore;
    }

    @PostConstruct
    public void init() {
        if (initialized) {
            return;
        }
        init0();
    }

    private synchronized void init0() {
        if (initialized) {
            return;
        }
        Long maxHeight = blockRepository.getMaxHeight();
        BlockHeader blockHeader = blockRepository.getBlockHeader(maxHeight);
        setLastBlockHeader(blockHeader);
        if (blockHeader != null) {
            initialized = true;
            this.transactionStore = new TransactionStore(dbSource);
        }
    }

    @Override
    public void afterPropertiesSet() {
        registerListener();
    }

    public void addListener(TrustListener listener) {
        if (listener instanceof Blockchain || listener instanceof CompositeTrustListener) {
            return;
        }
        listeners.addListener(listener);
    }

    public void removeListener(TrustListener listener) {
        listeners.removeListener(listener);
    }

    public synchronized void startExecuteBlock() {
        String root = lastBlockHeader.getStateRootHash().getStateRoot();
        receipts = new ArrayList<>();
        if (StringUtils.isNotEmpty(root)) {
            repositorySnapshot = repository.getSnapshotTo(Hex.decode(root));
        } else {
            repositorySnapshot = repository.getSnapshotTo(null);
        }
    }

    public synchronized void finishExecuteBlock(BlockHeader blockHeader) {
        Bloom logBloom = new Bloom();
        for (TransactionResultInfo result : receipts) {
            logBloom.or(result.getBloomFilter());
        }

        TrieImpl trie = new TrieImpl();
        for (TransactionResultInfo result : receipts) {
            transactionStore.put(result.getTxHash(), result);
            trie.put(result.getTxHash(), result.getEncoded());
        }

        long height = blockHeader.getHeight();
        MiniBlock miniBlock = new MiniBlock(height, logBloom.getData(), trie.getRootHash());
        dbSource.put(ByteUtil.longToBytes(height), miniBlock.getEncoded());
        transactionStore.flush();
        repositorySnapshot.commit();

        repository = repositorySnapshot;
        repositorySnapshot = null;
        lastBlockHeader = blockHeader;

        onBlock(blockHeader);

        for (TransactionResultInfo result : receipts) {
            onTransactionExecuted(result);
        }
        receipts = null;
    }

    public void putResultInfo(TransactionResultInfo result) {
        receipts.add(result);
    }

    public BlockHeader getLastBlockHeader() {
        return lastBlockHeader;
    }

    public synchronized void setLastBlockHeader(BlockHeader blockHeader) {
        lastBlockHeader = blockHeader;
        if (blockHeader != null && blockHeader.getStateRootHash().getStateRoot() != null) {
            repository = repository.getSnapshotTo(Hex.decode(blockHeader.getStateRootHash().getStateRoot()));
        }
    }

    public Repository getRepository() {
        return repository;
    }

    public Repository getRepositorySnapshot() {
        return repositorySnapshot;
    }

    public Repository getRepositorySnapshot(byte[] root) {
        return repository.getSnapshotTo(root);
    }

    public Repository getRepositorySnapshot(long blockHeight) {
        if (blockHeight > lastBlockHeader.getHeight()) {
            log.warn("Target blockHeight mast less than last block height");
            throw new IllegalArgumentException("Target blockHeight mast less than last block height");
        }
        BlockHeader blockHeader = blockRepository.getBlockHeader(blockHeight);
        return repository.getSnapshotTo(Hex.decode(blockHeader.getStateRootHash().getStateRoot()));
    }

    public BlockStore getBlockStore() {
        return blockStore;
    }

    public TransactionResultInfo getTransactionResultInfo(String txId) {
        if (transactionStore == null) {
            return null;
        }
        return transactionStore.get(txId.getBytes());
    }

    @Override
    public void onBlock(BlockHeader header) {
        listeners.onBlock(header);
    }

    @Override
    public void onTransactionExecuted(TransactionResultInfo resultInfo) {
        listeners.onTransactionExecuted(resultInfo);
    }

    private void registerListener() {
        Map<String, TrustListener> map = applicationContext.getBeansOfType(TrustListener.class);
        map.forEach((key,value) -> {
            addListener(value);
        });
    }

    private class MiniBlock {
        private long height;
        private byte[] logsBloom;
        private byte[] receiptsRoot;

        private byte[] rlpEncoded;

        public MiniBlock(long height, byte[] logsBloom, byte[] receiptsRoot) {
            this.height = height;
            this.logsBloom = logsBloom;
            this.receiptsRoot = receiptsRoot;
        }

        public MiniBlock(byte[] rlp) {
            this.rlpEncoded = rlp;
            RLPList rlpList = RLP.decode2(rlp);

            BigInteger integer = RLP.decodeBigInteger(rlpList.get(0).getRLPData(), 0);
            this.height = integer.longValue();
            this.logsBloom = rlpList.get(1).getRLPData();
            this.receiptsRoot = rlpList.get(2).getRLPData();
        }

        public byte[] getEncoded() {
            if (rlpEncoded != null) {
                return rlpEncoded;
            }

            rlpEncoded = RLP.encodeList(
                    RLP.encodeBigInteger(BigInteger.valueOf(height)),
                    RLP.encodeElement(logsBloom),
                    RLP.encodeElement(receiptsRoot)
            );

            return rlpEncoded;
        }

        public long getHeight() {
            return height;
        }

        public byte[] getLogsBloom() {
            return logsBloom;
        }

        public byte[] getReceiptsRoot() {
            return receiptsRoot;
        }
    }
}
