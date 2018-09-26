package com.higgs.trust.slave.core.service.block.hash;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.TransactionReceipt;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liuyu
 * @description
 * @date 2018-04-18
 */
@Component @Slf4j public class TxRootHashBuilder {
    /**
     * the default hash value
     */
    public final static String DEFAULT_HASH_FLAG = "NO_TREE";

    @Autowired MerkleService merkleService;

    /**
     * build root has for transactions
     *
     * @param txs
     * @return
     */
    public String buildTxs(List<SignedTransaction> txs) {
        if (CollectionUtils.isEmpty(txs)) {
            return DEFAULT_HASH_FLAG;
        }
        for (SignedTransaction tx : txs) {
            List<SignInfo> signDatas = tx.getSignatureList();
            if (signDatas != null) {
                Collections.sort(signDatas, new Comparator<SignInfo>() {
                    @Override public int compare(SignInfo o1, SignInfo o2) {
                        return o1.getOwner().equals(o2.getOwner()) ? 1 : 0;
                    }
                });
                tx.setSignatureList(signDatas);
            }
        }
        Collections.sort(txs, new Comparator<SignedTransaction>() {
            @Override public int compare(SignedTransaction o1, SignedTransaction o2) {
                return o1.getCoreTx().getTxId().equals(o2.getCoreTx().getTxId()) ? 1 : 0;
            }
        });
        //by merkle tree
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.TX, Arrays.asList(txs));
        if (merkleTree == null) {
            return null;
        }
        return merkleTree.getRootHash();
    }

    /**
     * build root has for transaction receipt
     *
     * @param receipts
     * @return
     */
    public String buildReceipts(Map<String, TransactionReceipt> receipts) {
        if (MapUtils.isEmpty(receipts)) {
            return DEFAULT_HASH_FLAG;
        }
        List<TransactionReceipt> list = receipts.entrySet().stream().sorted(Comparator.comparing(e->e.getKey())).map(e-> e.getValue()).collect(
            Collectors.toList());

        //by merkle tree
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.TX_RECEIEPT, Arrays.asList(list));
        if (merkleTree == null) {
            return null;
        }
        return merkleTree.getRootHash();
    }

    /**
     * get root hash from merkle tree,if is null return NO_TREE
     *
     * @param merkleTree
     * @return
     */
    public String getRootHash(MerkleTree merkleTree) {
        if (merkleTree == null) {
            return DEFAULT_HASH_FLAG;
        }
        return merkleTree.getRootHash();
    }
}
