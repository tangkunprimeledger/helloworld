package com.higgs.trust.slave.core.service.action.utxo;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.core.service.contract.SmartContractUtil;
import com.higgs.trust.slave.core.service.contract.UTXOExecuteContextData;
import com.higgs.trust.slave.core.service.contract.UTXOSmartContract;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
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
    private  UTXOSmartContract utxoSmartContract;
    @Test
    public void testProcess() throws Exception {
    }

    @Test
    public void testValidateUTXOActionType() throws Exception {
        UTXOAction utxoAction = new UTXOAction();
        List<TxIn> inputList = new ArrayList<>();
        List<TxOut> outputList = new ArrayList<>();
        TxIn  txIn = new TxIn();
        inputList.add(txIn);
        TxOut txOut= new TxOut();
        outputList.add(txOut);
        utxoAction.setInputList(inputList);
        //utxoAction.setOutputList(outputList);
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.ISSUE);
        System.out.println(validateUTXOActionType(utxoAction, "000002"));

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
        System.out.println(isUTXOIssueAction(2,1, InitPolicyEnum.UTXO_ISSUE.getType()));
        System.out.println(isUTXOIssueAction(0,0, InitPolicyEnum.UTXO_ISSUE.getType()));
        System.out.println(isUTXOIssueAction(1,0, InitPolicyEnum.UTXO_ISSUE.getType()));
        System.out.println(isUTXOIssueAction(0,1, InitPolicyEnum.UTXO_DESTROY.getType()));
    }

    @Test
    public void testisisUTXODestructionAction() throws Exception {
        System.out.println(isUTXODestructionAction(2,1, InitPolicyEnum.UTXO_DESTROY.getType()));
        System.out.println(isUTXODestructionAction(0,0, InitPolicyEnum.UTXO_DESTROY.getType()));
        System.out.println(isUTXODestructionAction(0,1, InitPolicyEnum.UTXO_DESTROY.getType()));
        System.out.println(isUTXODestructionAction(0,1, InitPolicyEnum.UTXO_ISSUE.getType()));
        System.out.println(isUTXODestructionAction(1,0, InitPolicyEnum.UTXO_DESTROY.getType()));
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
        if (inputSize >0 || outputSize == 0) {
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
        TxOut  txOut = new TxOut();
        JSONObject state = new JSONObject();
        state.put("amount",1);
        txOut.setState(state);
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        txOut.setIdentity("lingchao");

        TxOut  txOut1 = new TxOut();
        JSONObject state1 = new JSONObject();
        state1.put("amount",2);
        txOut1.setState(state1);
        txOut1.setIndex(1);
        txOut1.setActionIndex(0);
        txOut1.setIdentity("lingchao");

        outputList.add(txOut);
        outputList.add(txOut1);

        String code ="function verify() {\n" + "    var action = ctx.getAction();\n" + "\tvar actionType = action.utxoActionType;\n" + "\tvar issueActionType = ctx.getUTXOActionType('ISSUE');\n" + "\tvar normalActionType = ctx.getUTXOActionType('NORMAL');\n" + "\tvar destructionActionType = ctx.getUTXOActionType('DESTRUCTION');\n" + "\n" + "\t//issue utxo  action\n" + "\tif(actionType == issueActionType){\n" + "\t\treturn true;\n" + "\t}\n" + "\n" + "    //notmal utxo  action\n" + "    if(actionType == normalActionType){\n" + "\t  if(action.inputList.length == 0 || action.getOutputList().length == 0){\n" + "\t \treturn false;\n" + "\t  }\n" + "\tvar utxoList = ctx.queryUTXOList(action.inputList);\n" + "\tvar inputsAmount = 0;\n" + "\tvar outputsAmount = 0;\n" + "\tutxoList.forEach(function (input) {inputsAmount += input.getState().amount;});\n" + "\taction.getOutputList().forEach(function (input) {outputsAmount += input.getState().amount;});\n" + "\treturn inputsAmount == outputsAmount;\n" + "\t}\n" + "\n" + "\t //destruction utxo  action\n" + "\tif(actionType == destructionActionType){\n" + "\n" + "\t\treturn true;\n" + "\t}\n" + "\n" + "\treturn false;\n" + "}";
        //execute contract
        utxoAction.setContract(code);
        utxoAction.setOutputList(outputList);
        utxoAction.setInputList(new ArrayList<>());
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.ISSUE);
        ExecuteContextData data = new UTXOExecuteContextData().setAction(utxoAction);
        boolean contractIssueProcessSuccess = utxoSmartContract.execute(utxoAction.getContract(), data, TxProcessTypeEnum.VALIDATE);

        System.out.println("Issue true==============================="+contractIssueProcessSuccess);



     /*   utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setInputList(new ArrayList<>());
        utxoAction.setOutputList(outputList);
        ExecuteContextData data1 =new UTXOExecuteContextData().setAction(utxoAction);
        boolean contractNormalProcessSuccess = utxoSmartContract.execute(utxoAction.getContract(), data1, TxProcessTypeEnum.VALIDATE);

        System.out.println("Normal false==============================="+contractNormalProcessSuccess);
*/

        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(outputList);
        ExecuteContextData data2 = new UTXOExecuteContextData().setAction(utxoAction);
        boolean contractNormalProcessSuccess2 = utxoSmartContract.execute(utxoAction.getContract(), data2, TxProcessTypeEnum.VALIDATE);

        System.out.println("Normal true==============================="+contractNormalProcessSuccess2);


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
        state.put("amount",1);
        System.out.println("state:"+state);
        JSONObject state1 = new JSONObject();
        state1.put("amount",2);
        System.out.println("state1:"+state1);
    }




}