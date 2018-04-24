package com.higgs.trust.slave.core.service.block.hash;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.TransactionReceipt;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
            return null;
        }
        for (SignedTransaction tx : txs) {
            List<String> signDatas = tx.getSignatureList();
            Collections.sort(signDatas);
            tx.setSignatureList(signDatas);
        }
        Collections.sort(txs, new Comparator<SignedTransaction>() {
            @Override public int compare(SignedTransaction o1, SignedTransaction o2) {
                return o1.equals(o2) ? 1 : 0;
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
    public String buildReceipts(List<TransactionReceipt> receipts) {
        if (CollectionUtils.isEmpty(receipts)) {
            return null;
        }
        Collections.sort(receipts, new Comparator<TransactionReceipt>() {
            @Override public int compare(TransactionReceipt o1, TransactionReceipt o2) {
                return o1.equals(o2) ? 1 : 0;
            }
        });
        //by merkle tree
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.TX_RECEIEPT, Arrays.asList(receipts));
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
