package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.ContractException;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.contract.StandardSmartContract;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.Contract;
import com.higgs.trust.slave.model.bo.contract.ContractCreationAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.util.Date;

/**
 * create contract handler
 * @author duhongming
 */
@Slf4j @Component public class ContractCreationHandler implements ActionHandler {

    @Autowired private ContractSnapshotAgent snapshotAgent;
    @Autowired private StandardSmartContract smartContract;

    private byte[] getHexHash(byte[] data) {
        if (null == data) {
            return null;
        }

        byte[] cipherByte;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            cipherByte = md.digest();
            return cipherByte;
        } catch (Exception e) {
            log.error("SHA-256: {}", e.getMessage());
        }
        return null;
    }

    private String getHash(String data) {
        if (StringUtils.isEmpty(data)) {
            return null;
        }
        return Hex.encodeHexString(getHexHash(data.getBytes()));
    }

    private String generateAddress(Long height, String sender, String txId, ContractCreationAction creationAction) {
        String data = height.toString() + sender + txId + creationAction.getLanguage().toString() + creationAction.getCode();
        return getHash(data);
    }

    private void checkPolicy(ActionData actionData) {
        String policyId = actionData.getCurrentTransaction().getCoreTx().getPolicyId();
        if (InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId) != InitPolicyEnum.CONTRACT_ISSUE) {
            log.error("policyId error: {}", policyId);
            throw new ContractException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, String.format("policyId error: %s", policyId));
        }
    }

    private ContractCreationAction getAndCheckAction(ActionData actionData) {
        checkPolicy(actionData);
        verifyParams(actionData.getCurrentAction());
        return (ContractCreationAction) actionData.getCurrentAction();
    }

    @Override public void verifyParams(Action action) throws SlaveException {
        ContractCreationAction creationAction = (ContractCreationAction) action;
        if (creationAction == null) {
            log.error("[ContractCreation] convert action to ContractCreationAction is error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if (StringUtils.isEmpty(creationAction.getCode())) {
            log.error("[ContractCreation] code is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, "code is empty");
        }

        if(StringUtils.isEmpty(creationAction.getLanguage())) {
            log.error("[ContractCreation] language is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, "language is empty");
        }

        if (!"javascript".equals(creationAction.getLanguage())) {
            log.error("[ContractCreation] language is error: {}", creationAction.getLanguage());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, String.format("language is error: %s", creationAction.getLanguage()));
        }
    }

    @Override public void process(ActionData actionData) {
        log.trace("contract creation process start");
        Profiler.enter("ContractCreationHandler process");
        try{
            ContractCreationAction creationAction = getAndCheckAction(actionData);

            Long blockHeight = actionData.getCurrentBlock().getBlockHeader().getHeight();
            String sender = actionData.getCurrentTransaction().getCoreTx().getSender();
            String txId = actionData.getCurrentTransaction().getCoreTx().getTxId();
            String address = generateAddress(blockHeight, sender, txId, creationAction);

            Contract contract = snapshotAgent.get(address);
            if (null != contract) {
                throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
            }
            contract = new Contract();
            contract.setAddress(address);
            contract.setBlockHeight(blockHeight);
            contract.setTxId(txId);
            contract.setActionIndex(actionData.getCurrentAction().getIndex());
            contract.setLanguage(creationAction.getLanguage());
            contract.setCode(creationAction.getCode());
            contract.setCreateTime(new Date());
            contract.setVersion("1.0");

            snapshotAgent.insert(address, contract);
            smartContract.init(address, creationAction.getInitArgs());
        } finally {
            Profiler.release();
        }
    }
}

