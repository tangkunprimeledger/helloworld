package com.higgs.trust.slave.core.service.action.ca;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.HashUtil;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.core.service.action.GeniusBlockService;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import com.higgs.trust.slave.model.enums.BlockVersionEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author WangQuanzhou
 * @desc init ca handler
 * @date 2018/6/6 10:25
 */
@Slf4j @Component public class CaInitHandler {

    /**
     * the default hash value
     */
    public final static String DEFAULT_HASH_FLAG = "NO_TREE";

    @Autowired NodeState nodeState;
    @Autowired GeniusBlockService geniusBlockService;
    @Autowired MerkleService merkleService;
    @Autowired BlockService blockService;

    /**
     * 使用固定算法生成CA
     * 1、生成创世快
     * 2、更新集群节点信息
     * 3、更新节点配置信息
     * 4、生成CA信息插入db
     *
     * @param caActionList
     */
    public void process(List<Action> caActionList) {

        log.info("[CaInitHandler.process] start to build genius block, caActionList size={}", caActionList.size());
        Block block = buildBlock(caActionList);

        if(log.isDebugEnabled()){
            log.debug("[CaInitHandler.process] user ={}, block={}", nodeState.getNodeName(), block.toString());
        }


        log.info("[CaInitHandler.process] start to call geniusBlockService.generateGeniusBlock");
        geniusBlockService.generateGeniusBlock(block);

    }

    private Block buildBlock(List<Action> caActionList) {
        Block block = new Block();
        block.setGenesis(true);

        List<SignedTransaction> signedTransactionList = new LinkedList();
        SignedTransaction signedTransaction = new SignedTransaction();
        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setActionList(caActionList);
        coreTransaction.setTxId("geniusBlock");
        coreTransaction.setPolicyId("IS_NULL");
        coreTransaction.setVersion(VersionEnum.V1.getCode());
        coreTransaction.setSendTime(generateBlockTime());
        coreTransaction.setSender("TRUST");
        signedTransaction.setCoreTx(coreTransaction);
        signedTransactionList.add(signedTransaction);

        block.setSignedTxList(signedTransactionList);

        BlockHeader blockHeader = new BlockHeader();
        StateRootHash stateRootHash = new StateRootHash();

        stateRootHash.setCaRootHash(HashUtil.getSHA256S(JSON.toJSONString(caActionList)));
        stateRootHash.setAccountRootHash("NO_TREE");
        stateRootHash.setContractRootHash("NO_TREE");
        stateRootHash.setPolicyRootHash("NO_TREE");
        stateRootHash.setRsRootHash("NO_TREE");
        stateRootHash.setTxReceiptRootHash("NO_TREE");
        stateRootHash.setTxRootHash(buildTxs(signedTransactionList));
        blockHeader.setStateRootHash(stateRootHash);
        blockHeader.setHeight(1L);
        blockHeader.setPreviousHash("IS_NULL");
        blockHeader.setVersion(BlockVersionEnum.V1.getCode());
        blockHeader.setBlockTime(generateBlockTime().getTime());
        blockHeader.setBlockHash(blockService.buildBlockHash(blockHeader));

        block.setSignedTxList(signedTransactionList);
        block.setBlockHeader(blockHeader);

        return block;
    }

    public Date generateBlockTime() {
        String string = "2018-06-06 20:00:00";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(string);
        } catch (ParseException e) {
            log.error("[CaInitHandler.generateBlockTime] generate block time error");
        }
        return null;
    }

    /**
     * build root has for transactions
     *
     * @param txs
     * @return
     */
    private String buildTxs(List<SignedTransaction> txs) {
        if (CollectionUtils.isEmpty(txs)) {
            return DEFAULT_HASH_FLAG;
        }
        //by merkle tree
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.TX, Arrays.asList(txs));
        if (merkleTree == null) {
            return null;
        }
        return merkleTree.getRootHash();
    }

}
