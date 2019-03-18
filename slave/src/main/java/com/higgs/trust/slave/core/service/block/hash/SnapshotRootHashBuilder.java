package com.higgs.trust.slave.core.service.block.hash;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.Blockchain;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.model.bo.StateRootHash;
import com.higgs.trust.slave.model.bo.TransactionReceipt;
import com.higgs.trust.slave.model.bo.context.PackageData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author liuyu
 * @description
 * @date 2018-04-13
 */
@Component @Slf4j public class SnapshotRootHashBuilder {
    @Autowired MerkleTreeSnapshotAgent merkleTreeSnapshotAgent;
    @Autowired TxRootHashBuilder txRootHashBuilder;
    @Autowired Blockchain blockchain;

    /**
     * build root hash for block header
     *
     * @param packageData
     * @param txReceiptMap
     * @return
     */
    public StateRootHash build(PackageData packageData, Map<String, TransactionReceipt> txReceiptMap) {
        //hash for transactions
        String txRootHash = txRootHashBuilder.buildTxs(packageData.getCurrentBlock().getSignedTxList());
        if (StringUtils.isEmpty(txRootHash)) {
            log.error("[SnapshotRootHash.build]the txRootHash is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_BUILD_TX_ROOT_HASH_ERROR);
        }
        //hash for transaction execute results
        String txReceiptHash = txRootHashBuilder.buildReceipts(txReceiptMap);
        if (StringUtils.isEmpty(txReceiptHash)) {
            log.error("[SnapshotRootHash.buildHeader]the txReceiptHash is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_BUILD_TX_RECEIPT_ROOT_HASH_ERROR);
        }
        String accountRootHash = txRootHashBuilder.getRootHash(merkleTreeSnapshotAgent.buildMerkleTree(MerkleTypeEnum.ACCOUNT));
        String contractRootHash = txRootHashBuilder.getRootHash(merkleTreeSnapshotAgent.buildMerkleTree(MerkleTypeEnum.CONTRACT));
        String policyRootHash = txRootHashBuilder.getRootHash(merkleTreeSnapshotAgent.buildMerkleTree(MerkleTypeEnum.POLICY));
        String rsRootHash = txRootHashBuilder.getRootHash(merkleTreeSnapshotAgent.buildMerkleTree(MerkleTypeEnum.RS));
        String caRootHash = txRootHashBuilder.getRootHash(merkleTreeSnapshotAgent.buildMerkleTree(MerkleTypeEnum.CA));
        StateRootHash stateRootHash = new StateRootHash();
        stateRootHash.setTxRootHash(txRootHash);
        stateRootHash.setTxReceiptRootHash(txReceiptHash);
        stateRootHash.setAccountRootHash(accountRootHash);
        stateRootHash.setContractRootHash(contractRootHash);
        stateRootHash.setPolicyRootHash(policyRootHash);
        stateRootHash.setRsRootHash(rsRootHash);
        stateRootHash.setCaRootHash(caRootHash);
        stateRootHash.setStateRoot(Hex.toHexString(blockchain.getRepositorySnapshot().getRoot()));
        return stateRootHash;
    }
}
