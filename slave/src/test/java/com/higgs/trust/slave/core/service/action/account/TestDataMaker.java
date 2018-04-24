package com.higgs.trust.slave.core.service.action.account;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.account.*;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.BlockVersionEnum;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-17
 */
public class TestDataMaker {

    private static final String priKey1 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALhZZQEKaaZOsD4z+m1AoBlVnSZFD9mafaRAt9PVHtgkFJViV8a3s+2bFEkxVIxLSqdyWVCqZCOm1+6jbWmk4pN5yY4AaZBCPM6kHkzl/o5ctGXqkIf9s8WxFAmKpMEi7qP6SCitaoMy1sC+IS/vcOphzJZ3NbP3kG0FyCR5EvGdAgMBAAECgYAf7HMeRARZpWTF0NB8HOXcnUPSfcEp6KP7Tq3GxDBMM6tQ1y/mHKfO7L0Nk7pVdTBfYODwpCElP15DWA+5bLFDl2GzCY3gBoMrgrpIaaD672Qf12ikcVf6q/FSNJAvvDPSpLQKYJKG4Aa8/0mFZB9JU1KM2+wDl4Fgf/Lf+vM/zQJBAN1X5nvjy4nwTXlHWvtB+PG9ptAsGo9baEtGW2UrDhPYObghJpk4Slxo+r7l4fWNnwEP6kMGBlFMH41oxNezYLcCQQDVNqiBJBVUgIVGaLSb3ksyXerOf9w1g7JTpSoZmf6SOMAZQ3kqky9Ik4LGwnlFaolUEA/fcTev2rC8iBFt00RLAkB09I5H5jzVXRFCxQ5w9xIYghKTqso5952rMLj4QwDEQZt2DKY9jb3VCG990TBNNJDQ2dz5n0RVTrjZWoOwSgsPAkBvFcArMIKQeTl22pymzOV+w2HP3tv7YbcqT1Yk6o+w3TJwty/M18x90qUDK1WFriEIlCnA77rku1rzjy0NfFILAkBnbOFY4ECXdFysok3n5AgrdHBr8bRSRvzBpdjUL9eH0Cs/37aqwZynwCg6DX5/I2gIwX78ysnujB4mY6lFOG5f";

    private static final String priKey2 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQwv8kaTmN15Z7c6gz3/7a8wmtuJqgn25uWAkBO5vTs6DpfB0Nf7N5jOH2pMhkgqkdiOlpNpTb+zoJZ+DNy28mHHbpb99GDEoa5zvcXxypU5yrNhmrch1bJbKZQiGoX/5NAia9t/Kltxdcs6EmWuOQB79fLhLDgwHeUDzYOdM13AgMBAAECgYAFWznGe78262+0QQy5o5WKBpppGszUC4jUiI5GPsy2DMx+qv73qbd2gdIj91MVEsW7Um8I5yOOqb1e70RzmTmmSgmIbc7L2ogkEVa/AWdnmFIqVV7EOokc7pExc0UMlIBXCiNynrQic0YtxV65JjaE/JAFomCCAUBbsP9TSs/ZMQJBANRq8rBvR1PCA9pwzqfwalKAAzpwsOs0tavP8XF80xm7XKNZrnOIIiLSj+ME630ECYJZ2XTKF1g/TblIHV8zAYMCQQDF4LQcqKuNbeUeu0Xf3VX0TXPImIdB3ZbbQyPuynhk5D0Fx72q29gRKUZifrm1Kog6fvrwN1IyuoZem3oijEX9AkApkKPckmnKofRPEjPd+NVVP2diUBrOa4oBDLeaFWrZZihCbpIMWV8UoU82hQfvdpLFxv8eM01OH1T+JHZa4ogxAkA2WEs/H7fV5NurQAWlwPUNXoQxEGr9VO1MlLj2qRa9ps13m+7kUPKba/mPrXw1XFQDtMIYXSkvE3k53HuDp4DFAkAxhxi9veGOKa24Fp+4MFSF3L9UdR6MROqIYVGgE0gHj7r+NIuCqk/l9acw9W4E5gAN03P3RAKpjmcqxOkZyj7h";

    private static final String priKey3 = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANDzTWjIRJ6Y3dKT4Z08/QuUMjj3OFSgt8qD9ZFgT3TXik44olP7O0gVJiL+tBtCuqsW6nU2BWt2S/1/SmGVq1dxco1VSCU/Dk7ReBTMRyZBOxfzdMnaTWMbiO+ETodJl3eQbK1miJyVbg7hLe7s/8xiH7AGsKkppW6GC7Kpb4zJAgMBAAECgYBORbYLuGmsF4uQ5ICxjDUmbz9ZA5MAcKwomsIU0UUyecN/hcuZNhWA7Rs6JLuHMroGeTEe8zuYg9n3fgV5BL4H96z3SBSrY+BsCf1CxYGXEVCHzlt6g8575MqtxIlqPXnpKr9S1663EtsCCJ93t5rZmMA7z8bUbFRTcrUsajYzAQJBAPynP0a6Pk5JlF0TW5vbzusZb3CsEdPTp39NxlHEx9v/2xuREti1CSVMhdm8ZDdC5hDoETZn4DTiBAF0Z5it6pkCQQDTt9uSFv16v+62yJIz0KE9EUZrLua1BlfTIyvgBZQ6Lp5ORS2S9iVzfOS77mufysbfGSpmD6Oc5ElY2coUy8GxAkAlFB5zMM4IC0Bc0IR3QTECy77RGE+deMhyJGXghjKWlNwBFa9gYmEvOiXCqKVEfurovEYaZ/A9kpXn6L9zZsKxAkAuym+IdfRHcKu9Uc6eDPnVmT/K6G6si15Vl2xW8mS0ByGNgtRzqlrUj0GuFx9KDXKuU81/CO3L+tgK/vceaXnBAkEAk+OjzXA0KXZGKm+O8/Vl8yiJQpuvpuO4cxy4E7nEAjevFip88p4tO03DVxjyq2Az7457q/T+C/Ohr1X9uS/v/Q==";


    public static Action makeOpenAccountAction(String accountNo,FundDirectionEnum fundDirectionEnum){
        OpenAccount action = new OpenAccount();
        action.setType(ActionTypeEnum.OPEN_ACCOUNT);
        action.setIndex(0);
        action.setAccountNo(accountNo);
        action.setChainOwner("chain_owner");
        action.setDataOwner("rs-test1");
        action.setCurrency("CNY");
        action.setFundDirection(fundDirectionEnum);
        return action;
    }

    public static Action makeOpertionAction(String debitAccountNo,String creditAccountNo,BigDecimal happenAmount){
        AccountOperation action = new AccountOperation();
        action.setType(ActionTypeEnum.ACCOUNTING);
        action.setIndex(0);

        List<AccountTradeInfo> debitTradeInfo = new ArrayList<>();
        debitTradeInfo.add(new AccountTradeInfo(debitAccountNo,happenAmount));
        List<AccountTradeInfo> creditTradeInfo = new ArrayList<>();
        creditTradeInfo.add(new AccountTradeInfo(creditAccountNo,happenAmount));

        action.setBizFlowNo("biz_flow_no_" + System.currentTimeMillis());
        action.setDebitTradeInfo(debitTradeInfo);
        action.setCreditTradeInfo(creditTradeInfo);
        action.setAccountDate(new Date());

        return action;
    }

    public static Action makeFreezeAction(String accountNo,int index){
        AccountFreeze action = new AccountFreeze();
        action.setType(ActionTypeEnum.FREEZE);
        action.setAccountNo(accountNo);
        action.setAmount(new BigDecimal("0.50"));
        action.setBizFlowNo("freeze_flow_no_1_" + index);
        action.setIndex(0);
        return action;
    }

    public static Action makeUnFreezeAction(String accountNo,String bizFlowNo){
        AccountUnFreeze action = new AccountUnFreeze();
        action.setType(ActionTypeEnum.UNFREEZE);
        action.setAccountNo(accountNo);
        action.setAmount(new BigDecimal("0.10"));
        action.setBizFlowNo(bizFlowNo);
        action.setIndex(0);
        return action;
    }

    public static Action makeCurrencyAction(String currencyName) {
        IssueCurrency action = new IssueCurrency();
        action.setType(ActionTypeEnum.ISSUE_CURRENCY);
        action.setIndex(0);
        action.setCurrencyName(currencyName);
        action.setRemark("this is test");
        return action;
    }

    public static CoreTransaction makeCoreTx(List<Action> actions,int index,InitPolicyEnum policyEnum){
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setPolicyId(policyEnum.getPolicyId());
        coreTx.setTxId("tx_id_" +index + "_"+ System.currentTimeMillis());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setActionList(actions == null ? new ArrayList<>():actions);
        coreTx.setBizModel(new JSONObject());
        coreTx.setSender("rs-test1");
        coreTx.setLockTime(new Date());
        return coreTx;
    }

    public static CoreTransaction makeCoreTx(List<Action> actions,int index,String policyId,JSONObject bizModel){
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setPolicyId(policyId);
        coreTx.setTxId("tx_id_" + actions.get(0).getType().getCode() + "_" + index + "_"+ System.currentTimeMillis());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setActionList(actions == null ? new ArrayList<>():actions);
        coreTx.setBizModel(bizModel);
        coreTx.setSender("rs-test1");
        coreTx.setLockTime(new Date());
        return coreTx;
    }


    public static SignedTransaction makeSignedTx(CoreTransaction coreTransaction)
        throws Exception {
        SignedTransaction signedTransaction = new SignedTransaction();
        signedTransaction.setCoreTx(coreTransaction);
        String sign = SignUtils.sign(JSON.toJSONString(coreTransaction), priKey1);
        List<String> signedList = new ArrayList<>();
        signedList.add(sign);
        signedTransaction.setSignatureList(signedList);
        return  signedTransaction;
    }

    public static BlockHeader makeBlockHeader(){
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
        return blockHeader;
    }
}
