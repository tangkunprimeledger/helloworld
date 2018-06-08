package com.higgs.trust.slave.contract;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.core.service.contract.UTXOExecuteContextData;
import com.higgs.trust.slave.core.service.contract.UTXOSmartContract;
import com.higgs.trust.slave.core.service.contract.UTXOSmartContractImpl;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UtxoSmartContractTest extends BaseTest {

    @Autowired UTXOSmartContract smartContract;

    protected String loadCodeFromResourceFile(String fileName) {
        try {
            return IOUtils.toString(this.getClass().getResource(fileName), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Test
    public void testVerify() {
        String code = loadCodeFromResourceFile("/utxo.js");
        UTXOSmartContract smartContract = new UTXOSmartContractImpl();

        List<TxOut> outList = new ArrayList<>();
        TxOut out = new TxOut();
        out.setIdentity("dhming");
        JSONObject state = new JSONObject();
        state.put("name", "zhangs");
        out.setState(state);
        outList.add(out);
        UTXOAction action = new UTXOAction();
        action.setOutputList(outList);

        action.setIndex(1);
        ExecuteContextData data = new UTXOExecuteContextData().setAction(action);
        boolean isOk = smartContract.execute(code, data, TxProcessTypeEnum.PERSIST);
        Assert.assertTrue(isOk, "verify result");
    }

    @Test
    public void testVerify2() {
        String code = loadCodeFromResourceFile("/utxo.js");

        List<TxOut> outList = new ArrayList<>();
        TxOut out = new TxOut();
        out.setIdentity("dhming");
        JSONObject state = new JSONObject();
        state.put("name", "zhangs");
        out.setState(state);
        outList.add(out);
        UTXOAction action = new UTXOAction();
        action.setOutputList(outList);

        action.setIndex(1);
        ExecuteContextData data = new UTXOExecuteContextData().setAction(action);
        boolean isOk = smartContract.execute(code, data, TxProcessTypeEnum.PERSIST);
        Assert.assertTrue(isOk, "verify result");
    }
}
