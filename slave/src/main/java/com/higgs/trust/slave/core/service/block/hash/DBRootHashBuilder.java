package com.higgs.trust.slave.core.service.block.hash;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.model.bo.StateRootHash;
import com.higgs.trust.slave.model.bo.TransactionReceipt;
import com.higgs.trust.slave.model.bo.context.PackageData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-13
 */
@Component @Slf4j public class DBRootHashBuilder {
    @Autowired MerkleService merkleService;
    @Autowired TxRootHashBuilder txRootHashBuilder;

    /**
     * build root hash for block header
     *
     * @param packageData
     * @param txReceipts
     * @return
     */
    public StateRootHash build(PackageData packageData, List<TransactionReceipt> txReceipts) {
        //hash for transactions
        String txRootHash = txRootHashBuilder.buildTxs(packageData.getCurrentBlock().getSignedTxList());
        if (StringUtils.isEmpty(txRootHash)) {
            log.error("[SnapshotRootHash.build]the txRootHash is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_BUILD_TX_ROOT_HASH_ERROR);
        }
        //hash for transaction execute results
        String txReceiptHash = txRootHashBuilder.buildReceipts(txReceipts);
        if (StringUtils.isEmpty(txReceiptHash)) {
            log.error("[SnapshotRootHash.buildHeader]the txReceiptHash is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_BUILD_TX_RECEIPT_ROOT_HASH_ERROR);
        }
        //from db
        String accountRootHash = txRootHashBuilder.getRootHash(merkleService.queryMerkleTree(MerkleTypeEnum.ACCOUNT));
        String contractRootHash = txRootHashBuilder.getRootHash(merkleService.queryMerkleTree(MerkleTypeEnum.CONTRACT));
        String policyRootHash = txRootHashBuilder.getRootHash(merkleService.queryMerkleTree(MerkleTypeEnum.POLICY));
        String rsRootHash = txRootHashBuilder.getRootHash(merkleService.queryMerkleTree(MerkleTypeEnum.RS));
        StateRootHash stateRootHash = new StateRootHash();
        stateRootHash.setTxRootHash(txRootHash);
        stateRootHash.setTxReceiptRootHash(txReceiptHash);
        stateRootHash.setAccountRootHash(accountRootHash);
        stateRootHash.setContractRootHash(contractRootHash);
        stateRootHash.setPolicyRootHash(policyRootHash);
        stateRootHash.setRsRootHash(rsRootHash);
        return stateRootHash;
    }
}
