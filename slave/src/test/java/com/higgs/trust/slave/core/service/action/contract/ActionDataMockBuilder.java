package com.higgs.trust.slave.core.service.action.contract;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.BlockVersionEnum;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActionDataMockBuilder {
    private Block block = new Block();
    private PackContext packContext = new PackContext(new Package(), block);
    private List<Action> actions = new ArrayList<>();
    private List<SignedTransaction> transList = new ArrayList<>();

    private SignedTransaction currentSignedTransaction = null;

    public ActionDataMockBuilder() {
        this.block.setSignedTxList(transList);
    }

    public ActionDataMockBuilder setBlockHeader(BlockHeader header) {
        this.block.setBlockHeader(header);
        return this;
    }

    public ActionDataMockBuilder createSignedTransaction(InitPolicyEnum policyEnum) {
        actions = new ArrayList<>();
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setPolicyId(policyEnum.getPolicyId());
        coreTx.setTxId("tx_id_"+ System.currentTimeMillis());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setActionList(actions);
        coreTx.setBizModel(new JSONObject());
        coreTx.setSender("rs-1");
        coreTx.setLockTime(new Date());

        currentSignedTransaction = new SignedTransaction();
        currentSignedTransaction.setCoreTx(coreTx);
        currentSignedTransaction.setSignatureList(new ArrayList<>());
        this.transList.add(currentSignedTransaction);
        return this;
    }

    public ActionDataMockBuilder setTxId(String txId) {
        if (null != this.currentSignedTransaction) {
            this.currentSignedTransaction.getCoreTx().setTxId(txId);
        }
        return this;
    }

    public ActionDataMockBuilder signature(String signature) {
        if (null != this.currentSignedTransaction) {
            this.currentSignedTransaction.getSignatureList().add(signature);
        }
        return this;
    }

    public ActionDataMockBuilder addAction(Action action) {
        this.actions.add(action);
        this.packContext.setCurrentAction(action);
        return this;
    }

    public ActionDataMockBuilder makeBlockHeader() {
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(1L);
        blockHeader.setPreviousHash("xxxx");
        blockHeader.setBlockHash("root-hash");
        blockHeader.setBlockTime(System.currentTimeMillis());
        blockHeader.setVersion(BlockVersionEnum.V1.getCode());
        StateRootHash rootHash = new StateRootHash();
        rootHash.setAccountRootHash("account-hash");
        rootHash.setTxRootHash("tx-hash");
        rootHash.setTxReceiptRootHash("tx-receipt-hash");
        rootHash.setPolicyRootHash("policy-hash");
        rootHash.setRsRootHash("rs-root-hash");
        rootHash.setContractRootHash("contract-hash");
        blockHeader.setStateRootHash(rootHash);

        block.setBlockHeader(blockHeader);
        return this;
    }

    public PackContext build() {
        this.packContext.setCurrentBlock(this.block);
        this.packContext.setCurrentTransaction(currentSignedTransaction);
        return packContext;
    }

    public static ActionDataMockBuilder getBuilder() {
        return new ActionDataMockBuilder();
    }
}
