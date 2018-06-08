package com.higgs.trust.slave.core.service.action.utxo;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.core.service.contract.UTXOExecuteContextData;
import com.higgs.trust.slave.core.service.contract.UTXOSmartContract;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXODBHandler;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXOHandler;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXOSnapshotHandler;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UTXOActionServiceTest extends BaseTest {
    @Autowired
    private UTXOActionService utxoActionService;
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private UTXOSmartContract utxoSmartContract;
    @Autowired
    private UTXOSnapshotHandler utxoSnapshotHandler;
    @Autowired
    private UTXODBHandler utxoDBHandler;

    @Test
    public void testProcess() throws Exception {
    }

    @Test
    public void testValidateUTXOActionType() throws Exception {
        UTXOAction utxoAction = new UTXOAction();
        List<TxIn> inputList = new ArrayList<>();
        List<TxOut> outputList = new ArrayList<>();
        TxIn txIn = new TxIn();
        inputList.add(txIn);
        TxOut txOut = new TxOut();
        outputList.add(txOut);
        utxoAction.setInputList(inputList);
        //utxoAction.setOutputList(outputList);
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.ISSUE);
        System.out.println(validateUTXOActionType(utxoAction, "000002"));

    }


    @Test
    public void testIsLegalContractAddress() throws Exception {
        List<TxIn> inputList = new ArrayList<>();
        TxIn txIn = new TxIn();
        txIn.setTxId("123123222");
        txIn.setIndex(0);
        txIn.setActionIndex(0);
        inputList.add(txIn);
        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setContractAddress("qqqqq");

        System.out.println("isLegalContractAddress validate:" + isLegalContractAddress(utxoAction.getContractAddress(), inputList, TxProcessTypeEnum.VALIDATE));
        System.out.println("isLegalContractAddress persisted:" + isLegalContractAddress(utxoAction.getContractAddress(), inputList, TxProcessTypeEnum.PERSIST));
    }


    /**
     * check whether the contractAddress is legal
     *
     * @param contractAddress
     * @param inputList
     * @return
     */
    private boolean isLegalContractAddress(String contractAddress, List<TxIn> inputList, TxProcessTypeEnum processTypeEnum) {
        log.info("Start to validate Contract Address for UTXO action");
        //data operate type
        UTXOHandler utxoHandler = null;
        if (TxProcessTypeEnum.VALIDATE.equals(processTypeEnum)) {
            utxoHandler = utxoSnapshotHandler;
        }
        if (TxProcessTypeEnum.PERSIST.equals(processTypeEnum)) {
            utxoHandler = utxoDBHandler;
        }

        //check whether the contract is existed in tht block chain
        boolean isExist = utxoSmartContract.isExist(contractAddress, processTypeEnum);
        if (!isExist) {
            return false;
        }

        if (CollectionUtils.isEmpty(inputList)) {
            return true;
        }

        List<UTXO> utxoList = utxoHandler.queryUTXOList(inputList);
        for (UTXO utxo : utxoList) {
            if (!StringUtils.equals(contractAddress, utxo.getContractAddress())) {
                log.error("utxoAction contract address:{} is not the same with contract address: {} for UTXO:{}", contractAddress, utxo.getContractAddress(), utxo);
                return false;
            }
        }

        return true;
    }


    /**
     * validate weather the UTXOActionType is legal
     *
     * @param utxoAction
     * @param policyId
     * @return
     */
    private boolean validateUTXOActionType(UTXOAction utxoAction, String policyId) {
        log.info("Start to validate UTXOActionType for UTXO action");
        // inputs and outputs can not be null or empty together
        if (CollectionUtils.isEmpty(utxoAction.getInputList()) && CollectionUtils.isEmpty(utxoAction.getOutputList())) {
            log.error("The inputs and outputs are null in utxoAction");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        //it is only in memory
        String policyType = policyRepository.getPolicyType(policyId);
        int inputSize = utxoAction.getInputList() == null ? 0 : utxoAction.getInputList().size();
        int outputSize = utxoAction.getOutputList() == null ? 0 : utxoAction.getOutputList().size();
        UTXOActionTypeEnum utxoActionTypeEnum = utxoAction.getUtxoActionType();

        // validate issue UTXO action
        if (utxoActionTypeEnum.equals(UTXOActionTypeEnum.ISSUE)) {
            boolean isIssueUTXO = isUTXOIssueAction(inputSize, outputSize, policyType);
            if (!isIssueUTXO) {
                return false;
            }
        }

        //validate destruction UTXO action,
        if (utxoActionTypeEnum.equals(UTXOActionTypeEnum.DESTRUCTION)) {
            boolean isDestructionUTXO = isUTXODestructionAction(inputSize, outputSize, policyType);
            if (!isDestructionUTXO) {
                return false;
            }
        }

        //validate normal UTXO action,
        // 1.the action type should be NORMAL
        // 2.inputSize should be bigger than 0, outputSize  be bigger than 0 .
        // 3.policy type should be UTXO_DESTROY
        if (utxoActionTypeEnum.equals(UTXOActionTypeEnum.NORMAL)) {
            if (inputSize == 0 || outputSize == 0) {
                log.error("The normal UTXO action , inputSize should be bigger than 0 " + ",in fact it is :{}, outputSize should be bigger than 0  ,in fact it is :{}.", inputSize, outputSize);
                return false;
            }
        }
        return true;
    }

    @Test
    public void testisUTXOIssueAction() throws Exception {
        System.out.println(isUTXOIssueAction(2, 1, InitPolicyEnum.UTXO_ISSUE.getType()));
        System.out.println(isUTXOIssueAction(0, 0, InitPolicyEnum.UTXO_ISSUE.getType()));
        System.out.println(isUTXOIssueAction(1, 0, InitPolicyEnum.UTXO_ISSUE.getType()));
        System.out.println(isUTXOIssueAction(0, 1, InitPolicyEnum.UTXO_DESTROY.getType()));
    }

    @Test
    public void testisisUTXODestructionAction() throws Exception {
        System.out.println(isUTXODestructionAction(2, 1, InitPolicyEnum.UTXO_DESTROY.getType()));
        System.out.println(isUTXODestructionAction(0, 0, InitPolicyEnum.UTXO_DESTROY.getType()));
        System.out.println(isUTXODestructionAction(0, 1, InitPolicyEnum.UTXO_DESTROY.getType()));
        System.out.println(isUTXODestructionAction(0, 1, InitPolicyEnum.UTXO_ISSUE.getType()));
        System.out.println(isUTXODestructionAction(1, 0, InitPolicyEnum.UTXO_DESTROY.getType()));
    }

    /**
     * validate issue utxo action,
     * 1.the action type should be ISSUE
     * 2.inputSize should be bigger than 0, outputSize should be 0  .
     * 3.policy type should be UTXO_ISSUE
     *
     * @param inputSize
     * @param outputSize
     * @param policyType
     * @return
     */
    private boolean isUTXOIssueAction(int inputSize, int outputSize, String policyType) {
        log.info("Start to validate issue UTXO action");
        if (!StringUtils.equals(policyType, InitPolicyEnum.UTXO_ISSUE.getType())) {
            log.error("The issue UTXO action , policyType should be UTXO_ISSUE ,in fact it is {}", policyType);
            return false;
        }
        if (inputSize > 0 || outputSize == 0) {
            log.error("The issue UTXO action , inputSize should be    0 " + ",in fact it is :{}, outputSize should be bigger than 0 ,in fact it is :{}.", inputSize, outputSize);
            return false;
        }
        log.info("end of validate issue UTXO action");
        return true;
    }

    /**
     * validate destruction UTXO action,
     * 1.the action type should be DESTRUCTION
     * 2.inputSize should be bigger than 0, outputSize should be 0  .
     * 3.policy type should be UTXO_DESTROY
     *
     * @param inputSize
     * @param outputSize
     * @param policyType
     * @return
     */
    private boolean isUTXODestructionAction(int inputSize, int outputSize, String policyType) {
        log.info("Start to validate destruction UTXO action");
        if (!StringUtils.equals(policyType, InitPolicyEnum.UTXO_DESTROY.getType())) {
            log.error("The destruction UTXO action , policyType should be UTXO_DESTROY ,in fact it is {}", policyType);
            return false;
        }
        if (inputSize == 0 || outputSize > 0) {
            log.error("The destruction UTXO action , inputSize should be bigger than 0 " + ",in fact it is :{}, outputSize should be 0 ,in fact it is :{}.", inputSize, outputSize);
            return false;
        }
        log.info("End of validate destruction UTXO action");
        return true;
    }


    @Test
    public void testUtxoContract() throws Exception {

        UTXOAction utxoAction = new UTXOAction();
        List<TxIn> inputList = new ArrayList<>();
        TxIn txIn = new TxIn();
        txIn.setTxId("123");
        txIn.setIndex(0);
        txIn.setActionIndex(0);
        inputList.add(txIn);

        TxIn txIn1 = new TxIn();
        txIn1.setTxId("123");
        txIn1.setIndex(1);
        txIn1.setActionIndex(0);
        inputList.add(txIn1);

        List<TxOut> outputList = new ArrayList<>();
        TxOut txOut = new TxOut();
        JSONObject state = new JSONObject();
        state.put("amount", 1);
        state.put("billId", "1234");
        state.put("dueDate", "2018-05-14 00:00:00");
        state.put("finalPayerId", "lingchao");
        txOut.setState(state);
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        txOut.setIdentity("lingchao");

        TxOut txOut1 = new TxOut();
        JSONObject state1 = new JSONObject();
        state1.put("amount", 2);
        state1.put("billId", "1234");
        state1.put("dueDate", "2018-05-14 00:00:00");
        state1.put("finalPayerId", "lingchao");
        txOut1.setState(state1);
        txOut1.setIndex(1);
        txOut1.setActionIndex(0);
        txOut1.setIdentity("lingchao");

        outputList.add(txOut);
        outputList.add(txOut1);

        String contractAddress = "12345";
                //execute contract
        utxoAction.setContractAddress(contractAddress);
/*        utxoAction.setOutputList(outputList);
        utxoAction.setInputList(new ArrayList<>());
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.ISSUE);
        ExecuteContextData data = new UTXOExecuteContextData().setAction(utxoAction);*/
/*
        boolean contractIssueProcessSuccess = utxoSmartContract.execute(utxoAction.getContractAddress(), data, TxProcessTypeEnum.VALIDATE);

        System.out.println("Issue true===============================" + contractIssueProcessSuccess);
*/



/*       utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setInputList(new ArrayList<>());
        utxoAction.setOutputList(outputList);
        ExecuteContextData data1 =new UTXOExecuteContextData().setAction(utxoAction);
        boolean contractNormalProcessSuccess = utxoSmartContract.execute(utxoAction.getContractAddress(), data1, TxProcessTypeEnum.VALIDATE);

        System.out.println("Normal false==============================="+contractNormalProcessSuccess);*/


        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(outputList);
        ExecuteContextData data2 = new UTXOExecuteContextData().setAction(utxoAction);
        boolean contractNormalProcessSuccess2 = utxoSmartContract.execute(utxoAction.getContractAddress(), data2, TxProcessTypeEnum.VALIDATE);

        System.out.println("Normal true===============================" + contractNormalProcessSuccess2);


    /*    utxoAction.setUtxoActionType(UTXOActionTypeEnum.DESTRUCTION);
        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(new ArrayList<>());
        ExecuteContextData data3 =  new UTXOExecuteContextData().setAction(utxoAction);
        boolean contractDESTRUCTIONProcessSuccess2 = utxoSmartContract.execute(utxoAction.getContract(), data3, TxProcessTypeEnum.VALIDATE);

        System.out.println("destoy true==============================="+contractDESTRUCTIONProcessSuccess2);
*/
    }

    @Test
    public void test() throws Exception {
        JSONObject state = new JSONObject();
        state.put("amount", 1);
        System.out.println("state:" + state);
        JSONObject state1 = new JSONObject();
        state1.put("amount", 2);
        System.out.println("state1:" + state1);
    }


}