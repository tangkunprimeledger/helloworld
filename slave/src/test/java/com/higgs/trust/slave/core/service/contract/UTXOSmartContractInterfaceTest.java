package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.action.contract.ContractBaseTest;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.contract.Contract;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.tester.dbunit.DataBaseManager;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

public class UTXOSmartContractInterfaceTest extends BaseTest {

    @Autowired private UTXOSmartContractImpl utxoSmartContract;
    @Autowired private SnapshotService snapshotService;
    @Autowired private ContractSnapshotAgent contractSnapshotAgent;
    @Autowired private ContractRepository contractRepository;

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

    public boolean executeFile(String codeFile, TxProcessTypeEnum processType) {
        String address = createContract(codeFile, processType);
        UTXOExecuteContextData contextData = new UTXOExecuteContextData();
        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setInputList(new ArrayList<TxIn>(0));
        contextData.setAction(utxoAction);
        return utxoSmartContract.execute(address, contextData, TxProcessTypeEnum.VALIDATE);
    }

    public void executeFileWithException(String codeFile, TxProcessTypeEnum processType, Class expectedException, String expectedExceptionMessage) {
        String address = createContract(codeFile, processType);
        UTXOExecuteContextData contextData = new UTXOExecuteContextData();
        try {
            utxoSmartContract.execute(address, contextData, TxProcessTypeEnum.VALIDATE);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex.getClass() == expectedException) {
                Assert.assertEquals(ex.getMessage(), expectedExceptionMessage);
                return;
            }
            Assert.fail();
        }
    }

    private String createContract(String filePath, TxProcessTypeEnum processType) {
        String code = getCodeFromFile(filePath);
        Contract contract = new Contract();
        contract.setBlockHeight(1L);
        contract.setTxId("000000000000000" + System.currentTimeMillis());
        contract.setActionIndex(0);
        contract.setAddress("00000" + System.currentTimeMillis() + System.currentTimeMillis());
        contract.setCode(code);
        contract.setLanguage("javascript");
        contract.setVersion("0.1");
        contract.setCreateTime(new Date());

        if (processType == TxProcessTypeEnum.VALIDATE) {
            contractSnapshotAgent.put(contract.getAddress(), contract);
        } else {
            contractRepository.deploy(contract);
        }
        return contract.getAddress();
    }

    @AfterClass
    public void clearDb() {
        DataBaseManager dataBaseManager = new DataBaseManager();
        Connection conn = dataBaseManager.getMysqlConnection(ContractBaseTest.getDbConnectString());
        dataBaseManager.executeDelete("TRUNCATE TABLE contract;TRUNCATE TABLE contract_state", conn);
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
    public void testExecute_Validate() {
        snapshotService.startTransaction();
        boolean result = executeFile("utxo_normal_return_true.js", TxProcessTypeEnum.VALIDATE);
        Assert.assertTrue(result);

        result = executeFile("utxo_normal_retrun_false.js", TxProcessTypeEnum.VALIDATE);
        Assert.assertFalse(result);

        executeFileWithException("utxo_exception_return_object.js", TxProcessTypeEnum.VALIDATE, ClassCastException.class, "jdk.nashorn.api.scripting.ScriptObjectMirror cannot be cast to java.lang.Boolean");
        executeFileWithException("utxo_exception_return_number.js", TxProcessTypeEnum.VALIDATE, ClassCastException.class, "java.lang.Integer cannot be cast to java.lang.Boolean");
        snapshotService.commit();
    }

    @Test
    public void testExecute_Persist() {
        boolean result = executeFile("utxo_normal_return_true.js", TxProcessTypeEnum.PERSIST);
        Assert.assertTrue(result);

        result = executeFile("utxo_normal_retrun_false.js", TxProcessTypeEnum.PERSIST);
        Assert.assertFalse(result);

        executeFileWithException("utxo_exception_return_object.js", TxProcessTypeEnum.PERSIST, ClassCastException.class, "jdk.nashorn.api.scripting.ScriptObjectMirror cannot be cast to java.lang.Boolean");
        executeFileWithException("utxo_exception_return_number.js", TxProcessTypeEnum.PERSIST, ClassCastException.class, "java.lang.Integer cannot be cast to java.lang.Boolean");
    }
}