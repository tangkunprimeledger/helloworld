package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;

public class UTXOSmartContractInterfaceTest extends BaseTest {

    @Autowired private UTXOSmartContractImpl utxoSmartContract;

    private String getCodeFromFile(String fileName) {
        String path = String.format("/java/com/higgs/trust/slave/core/service/contract/code/%s", fileName);
        String code = null;
        try {
            code = IOUtils.toString(this.getClass().getResource(path), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return code;
    }

    public boolean executeFile(String codeFile) {
        UTXOExecuteContextData contextData = new UTXOExecuteContextData();
        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setInputList(new ArrayList<TxIn>(0));
        contextData.setAction(utxoAction);
        return utxoSmartContract.execute(getCodeFromFile(codeFile), contextData, TxProcessTypeEnum.VALIDATE);
    }

    public void executeFileWithException(String codeFile, Class expectedException, String expectedExceptionMessage) {
        UTXOExecuteContextData contextData = new UTXOExecuteContextData();
        try {
            utxoSmartContract.execute(getCodeFromFile(codeFile), contextData, TxProcessTypeEnum.VALIDATE);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex.getClass() == expectedException) {
                Assert.assertEquals(ex.getMessage(), expectedExceptionMessage);
                return;
            }
            Assert.fail();
        }
    }

    @Test
    public void testExecute_code_empty() {
        ExecuteContextData contextData = new UTXOExecuteContextData();
        try {
            utxoSmartContract.execute(null, contextData, TxProcessTypeEnum.VALIDATE);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "argument code is empty");
        }
    }

    @Test
    public void testExecute_contextData_null() {
        ExecuteContextData contextData = new UTXOExecuteContextData();
        try {
            utxoSmartContract.execute("function verify() {}", null, TxProcessTypeEnum.VALIDATE);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "contextData is null");
        }
    }

    @Test
    public void testExecute_processType_null() {
        ExecuteContextData contextData = new UTXOExecuteContextData();
        try {
            utxoSmartContract.execute("function verify() {}", contextData, null);
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals(ex.getMessage(), "processType is null");
        }
    }

    @Test
    public void testExecute() {
        boolean result = executeFile("utxo_normal_return_true.js");
        Assert.assertTrue(result);

        result = executeFile("utxo_normal_retrun_false.js");
        Assert.assertFalse(result);

        executeFileWithException("utxo_exception_return_object.js", ClassCastException.class, "jdk.nashorn.api.scripting.ScriptObjectMirror cannot be cast to java.lang.Boolean");
        executeFileWithException("utxo_exception_return_number.js", ClassCastException.class, "java.lang.Integer cannot be cast to java.lang.Boolean");
    }
}