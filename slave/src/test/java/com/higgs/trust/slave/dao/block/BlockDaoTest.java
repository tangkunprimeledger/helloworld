package com.higgs.trust.slave.dao.block;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.po.block.BlockPO;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.StateRootHash;
import com.higgs.trust.slave.model.enums.BlockVersionEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Date;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/8
 *
 */
public class BlockDaoTest extends BaseTest {

    @Autowired BlockDao blockDao;

    @Test public void queryByHeight() {
        BlockPO blockPO = blockDao.queryByHeight(1L);
        System.out.println(blockPO);


    }

    @Test public void addBlock() {
        BlockPO blockPO = new BlockPO();

        blockPO.setHeight(1L);
        blockPO.setVersion("1.0.12.2");
        blockPO.setAccountRootHash("account-root-hash");
        blockPO.setBlockTime(new Date());
        blockPO.setBlockHash("block-root-hash");
        blockPO.setContractRootHash("contract-root-hash");
        blockPO.setPolicyRootHash("policy-root-hash");
        blockPO.setPreviousHash("previous hash");
        blockPO.setRsRootHash("rs-root-hash");
        blockPO.setTxRootHash("tx-root-hash");
        blockPO.setTxReceiptRootHash("tx-receipt-root-hash");

        blockDao.add(blockPO);

    }

    @Test
    public void buildBlockHash() {
        BlockPO blockPO = blockDao.queryByHeight(1L);
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setVersion(BlockVersionEnum.V1.getCode());
        blockHeader.setPreviousHash("0");
        blockHeader.setHeight(1L);
        blockHeader.setBlockTime(blockPO.getBlockTime().getTime());



        StateRootHash stateRoot = new StateRootHash();
        stateRoot.setAccountRootHash("NO_TREE");
        stateRoot.setContractRootHash("NO_TREE");
        stateRoot.setPolicyRootHash("NO_TREE");
        stateRoot.setRsRootHash("NO_TREE");
        stateRoot.setTxReceiptRootHash("NO_TREE");
        stateRoot.setTxRootHash("NO_TREE");
        blockHeader.setStateRootHash(stateRoot);

        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder();
        builder.append(function.hashLong(blockHeader.getHeight()));
        builder.append(function.hashLong(blockHeader.getBlockTime()));
        builder.append(function.hashString(getSafety(blockHeader.getVersion()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(blockHeader.getPreviousHash()), Charsets.UTF_8));
        StateRootHash stateRootHash = blockHeader.getStateRootHash();
        builder.append(function.hashString(getSafety(stateRootHash.getTxRootHash()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(stateRootHash.getTxReceiptRootHash()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(stateRootHash.getAccountRootHash()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(stateRootHash.getContractRootHash()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(stateRootHash.getPolicyRootHash()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(stateRootHash.getRsRootHash()), Charsets.UTF_8));
        String hash = function.hashString(builder.toString(), Charsets.UTF_8).toString();
        System.out.println(hash);
    }

    private static String getSafety(String data) {
        if (data == null) {
            return "";
        }
        return data;
    }
}