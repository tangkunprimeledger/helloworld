package com.higgs.trust.slave.core.service.action.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.BlockVersionEnum;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActionDataMockBuilder {

    public static final String privateKey1 ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIsMM2JkqnQACN+RUqRF4UkW58XnjpjA+RP4mnA4uD9KCjlJeTxvY0yHDqsvIaiXr7Ge0QB1VAKq0xit2BWHfVU/XlO1tqK+ea7YxooeoglvOkddiJiYTZUNjKM5AhttG950PpzrmeUcl9YGEZ/DwKKee+8tqaDWdIEHBnplO6mVAgMBAAECgYBtrWwCmoDRCw30uv5C0VQIgObE9gdGekB9/kRjbHn4ggBae5gDkaDzxjxNztlv0GYnZqxY/jML/46PEuE06jBzGcOlBuobQJJ38pTg0pnNVHbkTckxfUIr1MYUDhtO18tJZUZuMbYMwwgZ9K9E0N8kjKXk+rRx+BDjlbxNPds6KQJBAMLS/HCXjAfJlzSEWqkBavAKoW+bBhZlkTH+DoNk/KidASgdFBqtPUf5w3U+j8dK4nvt8R9X7zGxRAYXpDGHUucCQQC2tZlmL858suIA/+XfQVGoKOEvLlI5tGNDLXlDaKldY8UZGqxcyKaOsqEWMQnJCUy/0zTariN7kNssptYm04wjAkB3qVlt2lcizVn24rhAh+NjzlO7le8WQIn+t7m4UIWzFsQIHFwlynQSSkEYOTXcRY14avwnsT30Opm6WDj8Rs7PAkBytzqFSmbfLIFyFzmBH0Xhyyj3sqG10WixeQ+2HzSXiljqFjE6YFETL1yszkVSkCA8IKQC2Ws13hF+y5GR9yj5AkBAeUJj/a8wpdxJCufDpoaVUsB/XGK9XCqlGZvSy2TrjWLLBjZ3jiyjTlqIssfqI/IiJ4H2peocDaHXjFT0m+Av";
    public static final String privateKey2 ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJE5lYYWx1AE5E0XaxmQvg2putYOrya+Azd+QCHVA0rE8vUiXAuFia7TgjGQjhFO66SrcOJ3W2xSeHw6F1ApvZ8kfLB3ZsZT1e54QaWKU0ae0fcmk9dgrkpaniLCbd3PP8A+UUjwNeexIDjThYSzzatNGg06qdxgSCMc9bX7jwPlAgMBAAECgYA5Vtcmvk+r1IKfvaNX0MJ5eo5+fgXB8jwq6PpBYW2PU/vptctJ8UvPb0t0bnLpepOnzNkhUacTOezAf988k35+gw9Vrh6rXG4x7cZ65qbQOP+Xh0sx3YElyZKUBJzl5CMjNzT5ANc/QdpCD8LOOiPF4xpcHqKih74NGXc8hQv1AQJBAMNm1i6AC/oJM7XDnXPyswNVyjjIG+wi9xBgMGlt8JLH3c7/HblyAPHS/sFnljroVsHOyvoi45aZlIluhZsh6tUCQQC+QygmlnViImHq+MgL6bWaTLjKyTpH6k7zGQxvOxqJlioeWh73wxHAq/depKi8ElMrkEhMBA05ReCJl4Nb8xzRAkA28/HiS/KSVAot4SCj3iqIEpV3mJd5tm+jNFoJHHke3oS71TWH1M79M2if/cDbOkJD6SNea3d0ACcs6185vLUtAkBtrWLw05z5JB7UB/Oxwli4iO+hnlxlZnF6e38Kg8SpeZHwCz18z8tlCPzBZyQJvnqJS1QR1egVku18A4Zqs/txAkAg5kjdDw0v9QoQMr4oHZSuHxaG9I91SRkCPspN8urIg1Wu7cdTKfPdaAtSoU/xp/qpzfX+CPkhv7DoGlBLeo8e";
    private Block block = new Block();
    private Package currentPackage = new Package();
    private PackContext packContext = new PackContext(currentPackage, block);
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
        coreTx.setSender("rs-test1");
        coreTx.setLockTime(new Date());
        coreTx.setSendTime(new Date());

        currentSignedTransaction = new SignedTransaction();
        currentSignedTransaction.setCoreTx(coreTx);
        currentSignedTransaction.setSignatureList(new ArrayList<>());
        this.transList.add(currentSignedTransaction);
        return this;
    }

    public ActionDataMockBuilder setTransactionPolicyIdIf(String policyId, boolean condition) {
        if (condition) {
            currentSignedTransaction.getCoreTx().setPolicyId(policyId);
        }
        return this;
    }

    public ActionDataMockBuilder setTxId(String txId) {
        if (null != this.currentSignedTransaction) {
            this.currentSignedTransaction.getCoreTx().setTxId(txId);
        }
        return this;
    }
    public ActionDataMockBuilder setBizModel(JSONObject bizModel) {
        this.currentSignedTransaction.getCoreTx().setBizModel(bizModel);
        return this;
    }

    public ActionDataMockBuilder setBlockHeight(long height) {
        this.currentPackage.setHeight(height);
        this.block.getBlockHeader().setHeight(height);
        return this;
    }

    public ActionDataMockBuilder signature(String privateKey) {
        if (null != this.currentSignedTransaction) {
            String data = JSON.toJSONString(this.currentSignedTransaction.getCoreTx());
            try {
                String sign = SignUtils.sign(data, privateKey);
                SignInfo signInfo = new SignInfo();
                signInfo.setOwner("" + privateKey.hashCode());
                signInfo.setSign(sign);
                this.currentSignedTransaction.getSignatureList().add(signInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public ActionDataMockBuilder addAction(Action action) {
        if (action == null) {
            return this;
        }
        this.actions.add(action);
        this.packContext.setCurrentAction(action);
        return this;
    }

    public ActionDataMockBuilder makeBlockHeader() {
        BlockHeader blockHeader = new BlockHeader();
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
        rootHash.setCaRootHash("ca-hash");
        blockHeader.setStateRootHash(rootHash);

        block.setBlockHeader(blockHeader);
        return this;
    }

    public PackContext build() {
        this.packContext.setCurrentPackage(this.currentPackage);
        this.currentPackage.setPackageTime(new Date().getTime());
        this.currentPackage.setSignedTxList(this.transList);
        this.currentPackage.setStatus(PackageStatusEnum.INIT);
        this.packContext.setCurrentBlock(this.block);
        this.packContext.setCurrentTransaction(currentSignedTransaction);
        return packContext;
    }

    public static ActionDataMockBuilder getBuilder() {
        return new ActionDataMockBuilder();
    }
}
