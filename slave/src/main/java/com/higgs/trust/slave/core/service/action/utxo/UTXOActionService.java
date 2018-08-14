package com.higgs.trust.slave.core.service.action.utxo;

import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOStatusEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.core.service.action.dataidentity.DataIdentityService;
import com.higgs.trust.slave.core.service.contract.UTXOExecuteContextData;
import com.higgs.trust.slave.core.service.contract.UTXOSmartContract;
import com.higgs.trust.slave.core.service.datahandler.dataidentity.DataIdentityHandler;
import com.higgs.trust.slave.core.service.datahandler.dataidentity.DataIdentitySnapshotHandler;
import com.higgs.trust.slave.core.service.datahandler.manage.PolicyHandler;
import com.higgs.trust.slave.core.service.datahandler.manage.PolicySnapshotHandler;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXOHandler;
import com.higgs.trust.slave.core.service.datahandler.utxo.UTXOSnapshotHandler;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import com.higgs.trust.slave.model.bo.DataIdentity;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
import com.higgs.trust.slave.model.convert.UTXOConvert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * UTXO repository
 *
 * @author lingchao
 * @create 2018年03月28日19:17
 */
@Slf4j
@Service
public class UTXOActionService {

    @Autowired
    private DataIdentityService dataIdentityService;
    @Autowired
    private UTXOSnapshotHandler utxoSnapshotHandler;
    @Autowired
    private DataIdentitySnapshotHandler dataIdentitySnapshotHandler;
    @Autowired
    private PolicySnapshotHandler policySnapshotHandler;
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private UTXOSmartContract utxoSmartContract;


    /**
     * deal action with different TxProcessTypeEnum (data from db of snapshot)
     *
     * @param actionData
     */
    public void process(ActionData actionData) {
        // convert action and validate it
        UTXOAction utxoAction = (UTXOAction) actionData.getCurrentAction();
        String policyId = actionData.getCurrentTransaction().getCoreTx().getPolicyId();
        log.info("[Start to deal with utxoAction,params:{}", utxoAction);

        // validate UTXOActionType
        boolean isLegalUTXOActionType = validateUTXOActionType(utxoAction, policyId);
        if (!isLegalUTXOActionType) {
            log.error("UTXOActionType is not legal!");
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_UTXO_ACTION_TYPE_NOT_LEGAL_EXCEPTION.getMonitorTarget(), 1);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        //validate utxoAction contract address is the same with all the txIn's contract address
        boolean isLegalContractAddress = isLegalContractAddress(utxoAction.getContractAddress(), utxoAction.getInputList());
        if (!isLegalContractAddress) {
            log.error("utxoAction contract address is not legal!");
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_UTXO_CONTRACT_ADDRESS_NOT_LEGAL_EXCEPTION.getMonitorTarget(), 1);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // validate utxoAction's index is the same with txOut's actionIndex
        if (CollectionUtils.isNotEmpty(utxoAction.getOutputList())) {
            for (TxOut txOut : utxoAction.getOutputList()) {
                if (!txOut.getActionIndex().equals(utxoAction.getIndex())) {
                    log.error("One of txOut {} actionIndex :{}  in  outputs is not the same with index: {} in utxoAction", txOut, txOut.getActionIndex(), utxoAction.getIndex());
                    MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_UTXO_ACTION_INDEX_NOT_LEGAL_EXCEPTION.getMonitorTarget(), 1);
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
                }
            }
        }

        // validate data attribution
        Profiler.enter("[UTXO.validateIdentity]");
        boolean validateIdentitySuccess = validateIdentity(utxoAction, policyId);
        if (!validateIdentitySuccess) {
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_UTXO_DATA_IDENTITY_NOT_LEGAL_EXCEPTION.getMonitorTarget(), 1);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        Profiler.release();

        // double spend check
        Profiler.enter("[UTXO.doubleSpendCheck]");
        boolean isDoubleSpend = doubleSpendCheck(utxoAction.getInputList());
        if (isDoubleSpend) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_UTXO_IS_DOUBLE_SPEND_ERROR);
        }
        Profiler.release();

        //execute contract
        ExecuteContextData data = new UTXOExecuteContextData().setAction(utxoAction);

        Profiler.enter("[UTXO.doubleSpendCheck]");
        boolean contractProcessSuccess = utxoSmartContract.execute(utxoAction.getContractAddress(), data);
        if (!contractProcessSuccess) {
            log.error("UTXO contract process fail!");
            throw new SlaveException(SlaveErrorEnum.SLAVE_UTXO_CONTRACT_PROCESS_FAIL_ERROR);
        }
        Profiler.release();

        //persist data in memory or in DB
        Profiler.enter("[UTXO.persistData]");
        persistData(actionData);
        Profiler.release();

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

    /**
     * check whether the contractAddress is legal
     *
     * @param contractAddress
     * @param inputList
     * @return
     */
    private boolean isLegalContractAddress(String contractAddress, List<TxIn> inputList) {
        log.info("Start to validate Contract Address for UTXO action");
        //data operate type
        UTXOHandler utxoHandler = utxoSnapshotHandler;
        //check whether the contract is existed in tht block chain
        boolean isExist = utxoSmartContract.isExist(contractAddress);
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
            log.error("The issue UTXO action , inputSize should be  0 " + ",in fact it is :{}, outputSize should be bigger than 0 ,in fact it is :{}.", inputSize, outputSize);
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

    /**
     * validate data attribution
     *
     * @param action
     * @param policyId
     * @return
     */
    private boolean validateIdentity(UTXOAction action, String policyId) {
        log.info("Start to validate identity for UTXO action");

        //data operate type
        UTXOHandler utxoHandler = utxoSnapshotHandler;
        PolicyHandler policyHandler = policySnapshotHandler;
        DataIdentityHandler dataIdentityHandler = dataIdentitySnapshotHandler;

        // policy rsList
        Policy policy = policyHandler.getPolicy(policyId);
        if (null == policy) {
            log.error("Validate identity failed ,because policy is not exist for policy id :{}", policyId);
            throw new SlaveException(SlaveErrorEnum.SLAVE_POLICY_IS_NOT_EXISTS_EXCEPTION);
        }

        // dataIdentityList
        List<DataIdentity> dataIdentityList = new ArrayList<>();

        // add inputs identities into dataIdentityPOList
        List<TxIn> inputList = action.getInputList();
        addInputIdentities(inputList, dataIdentityList, utxoHandler, dataIdentityHandler);

        // add outputs identities into dataIdentityPOList
        List<TxOut> outputList = action.getOutputList();
        addOutputIdentities(outputList, dataIdentityList, dataIdentityHandler);

        // validate validateIdentity
        boolean isSuccess = dataIdentityService.validate(policy.getRsIds(), dataIdentityList);
        return isSuccess;
    }

    /**
     * add inputs identities into dataIdentityPOList
     *
     * @param inputList
     * @param dataIdentityList
     * @return
     */
    private void addInputIdentities(List<TxIn> inputList, List<DataIdentity> dataIdentityList, UTXOHandler utxoHandler, DataIdentityHandler dataIdentityHandler) {
        if (CollectionUtils.isEmpty(inputList)) {
            log.info("InputList is empty");
            return;
        }
        for (TxIn txIn : inputList) {
            UTXO utxo = utxoHandler.queryUTXO(txIn.getTxId(), txIn.getIndex(), txIn.getActionIndex());
            if (null == utxo) {
                log.error("Validate identity fail! The input: {} is not existed in UTXO s", txIn);
                throw new SlaveException(SlaveErrorEnum.SLAVE_TX_OUT_NOT_EXISTS_ERROR);
            }
            DataIdentity dataIdentity = dataIdentityHandler.getDataIdentity(utxo.getIdentity());
            if (null == dataIdentity) {
                log.error("Validate identity fail! The input's identityId : {} for dataidentity  is not existed!", utxo.getIdentity());
                throw new SlaveException(SlaveErrorEnum.SLAVE_DATA_IDENTITY_NOT_EXISTS_ERROR);
            }
            dataIdentityList.add(dataIdentity);
        }
    }

    /**
     * add outputs identities into dataIdentityPOList
     *
     * @param outputList
     * @param dataIdentityList
     */
    private void addOutputIdentities(List<TxOut> outputList, List<DataIdentity> dataIdentityList, DataIdentityHandler dataIdentityHandler) {
        if (CollectionUtils.isEmpty(outputList)) {
            log.info("outputList is empty");
            return;
        }
        for (TxOut txOut : outputList) {
            DataIdentity dataIdentity = dataIdentityHandler.getDataIdentity(txOut.getIdentity());
            if (null == dataIdentity) {
                log.error("Validate identity fail! The output's identityId : {} for dataidentity is not existed!", txOut.getIdentity());
                throw new SlaveException(SlaveErrorEnum.SLAVE_DATA_IDENTITY_NOT_EXISTS_ERROR);
            }
            dataIdentityList.add(dataIdentity);
        }
    }

    /**
     * double spend check for tx inputList
     *
     * @param inputList
     * @return
     */
    private boolean doubleSpendCheck(List<TxIn> inputList) {
        log.info("Do double Spend Check for inputList:{}", inputList);
        if (CollectionUtils.isEmpty(inputList)) {
            return false;
        }

        // ValidateIdentity has validate if there is txOut,so we don't need to validate it again.
        for (TxIn txIn : inputList) {

            //check whether there is double spend in the inputList
            boolean isDoubleSpend = doubleSpendInInputList(txIn, inputList);
            if (isDoubleSpend) {
                log.error("TxIn is double spend in inputList. txIn: {} , inputList:{}", txIn, inputList);
                return true;
            }

            //data operate type
            UTXOHandler utxoHandler = utxoSnapshotHandler;

            //check whether txIn is double spend in the all the txOut
            log.info("check whether txIn is double spend in the all the txOut");
            UTXO utxo = utxoHandler.queryUTXO(txIn.getTxId(), txIn.getIndex(), txIn.getActionIndex());
            if (null == utxo) {
                log.error("Validate identity fail! The input: {} is not existed in txOutPO", txIn);
                throw new SlaveException(SlaveErrorEnum.SLAVE_TX_OUT_NOT_EXISTS_ERROR);
            }
            if (StringUtils.equals(utxo.getStatus(), UTXOStatusEnum.SPENT.getCode())) {
                log.error("TxIn is double spend. txIn: {}, STXO: {}", txIn, utxo);
                return true;
            }
        }
        return false;
    }

    /**
     * check whether there is double spend in the inputList
     *
     * @param inputList
     * @param txIn
     * @return
     */
    private boolean doubleSpendInInputList(TxIn txIn, List<TxIn> inputList) {
        log.info("check whether there is double spend in the inputList");
        int sameTxInNum = 0;
        for (TxIn txInCheck : inputList) {
            if (StringUtils.equals(txIn.getTxId(), txInCheck.getTxId()) && txIn.getIndex().equals(txInCheck.getIndex()) && txIn.getActionIndex().equals(txInCheck.getActionIndex())) {
                sameTxInNum++;
            }
            if (sameTxInNum > 1) {
                log.info("TxIn is double spend in inputList. txIn: {} , inputList:{}", txIn, inputList);
                return true;
            }
        }
        return false;
    }


    /**
     * data operation
     *
     * @param actionData
     */
    private void persistData(ActionData actionData) {
        // batch insert UTXO
        log.info("Begin to batch insert UTXO!");
        batchInsertUTXO(actionData);
        // batch update STXO
        log.info("Begin to batch update STXO!");
        batchUpdateSTXO(actionData);
    }

    /**
     * batch insert UTXO
     *
     * @param actionData
     */
    private void batchInsertUTXO(ActionData actionData) {
        log.info("Start to batchInsert UTXO");
        UTXOAction utxoAction = (UTXOAction) actionData.getCurrentAction();
        List<TxOut> outputList = utxoAction.getOutputList();
        if (CollectionUtils.isEmpty(outputList)) {
            log.info("There is not UTXO need to insert");
            return;
        }
        List<TxOutPO> txOutPOList = new ArrayList<>();
        for (TxOut txOut : outputList) {
            TxOutPO txOutPO = UTXOConvert.UTXOBuilder(txOut, actionData);
            txOutPOList.add(txOutPO);
        }

        //BachInsert data
        log.info("BachInsert data txOutPOList: {}", txOutPOList);
        UTXOHandler utxoHandler = utxoSnapshotHandler;

        utxoHandler.batchInsert(txOutPOList);

        log.info("End of batchInsert UTXO");
    }

    /**
     * batch update STXO
     *
     * @param actionData
     */
    private void batchUpdateSTXO(ActionData actionData) {
        log.info("Start to bachUpdate STXO");
        UTXOAction utxoAction = (UTXOAction) actionData.getCurrentAction();
        List<TxIn> inputList = utxoAction.getInputList();
        if (CollectionUtils.isEmpty(inputList)) {
            log.info("There is not STXO need to update");
            return;
        }
        List<TxOutPO> txOutList = new ArrayList<>();
        for (TxIn txIn : inputList) {
            TxOutPO txOutPO = STXOBuilder(txIn, actionData);
            if (null == txOutPO) {
                log.error("STXO  not existed exception！");
                MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_DATA_NOT_EXIST_EXCEPTION.getMonitorTarget(), 1);
                throw new SlaveException(SlaveErrorEnum.SLAVE_TX_OUT_NOT_EXISTS_ERROR);
            }
            txOutList.add(txOutPO);
        }

        //data operation
        UTXOHandler utxoHandler = utxoSnapshotHandler;

        boolean isUpdate = utxoHandler.batchUpdate(txOutList);
        if (!isUpdate) {
            log.error("STXO  not update exception for txOutList:{}", txOutList);
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_DATA_NOT_UPDATED_EXCEPTION.getMonitorTarget(), 1);
            throw new SlaveException(SlaveErrorEnum.SLAVE_DATA_NOT_UPDATE_EXCEPTION);
        }
        log.info("End of  bachUpdate STXO");
    }

    /**
     * STXO builder just for snapshot
     *
     * @param txIn
     * @param actionData
     * @return
     */
    private TxOutPO STXOBuilder(TxIn txIn, ActionData actionData) {
        UTXO utxo = utxoSnapshotHandler.queryUTXO(txIn.getTxId(), txIn.getIndex(), txIn.getActionIndex());
        TxOutPO txOutPO = BeanConvertor.convertBean(utxo, TxOutPO.class);
        txOutPO.setSTxId(actionData.getCurrentTransaction().getCoreTx().getTxId());
        txOutPO.setStatus(UTXOStatusEnum.SPENT.getCode());
        txOutPO.setState(utxo.getState().toJSONString());
        txOutPO.setUpdateTime(new Date());
        return txOutPO;
    }

}
