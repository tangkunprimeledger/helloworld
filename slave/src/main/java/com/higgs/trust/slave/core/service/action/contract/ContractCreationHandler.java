package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.model.bo.Contract;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.ContractCreationAction;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.util.Date;

@Slf4j @Component public class ContractCreationHandler implements ActionHandler {

    @Autowired private ContractRepository contractRepository;
    @Autowired private ContractSnapshotAgent snapshotAgent;

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

    @Override public void validate(ActionData actionData) {
        log.trace("validate... start process contract creation");
        ContractCreationAction creationAction = (ContractCreationAction) actionData.getCurrentAction();
        if (creationAction == null) {
            log.error("[ContractCreation.validate] convert action to ContractCreationAction is error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        Long blockHeight = actionData.getCurrentBlock().getBlockHeader().getHeight();
        String sender = actionData.getCurrentTransaction().getCoreTx().getSender();
        String txId = actionData.getCurrentTransaction().getCoreTx().getTxId();
        String address = generateAddress(blockHeight, sender, txId, creationAction);

        Contract contract = snapshotAgent.get(address);
        if (null != contract) {
            // TODO [duhongming] custom SlaveErrorEnum
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_BLOCK_HEIGHT_UNEQUAL_ERROR);
        }

        contract = new Contract();
        contract.setAddress(address);
        contract.setLanguage(creationAction.getLanguage());
        contract.setCode(creationAction.getCode());
        contract.setCreateTime(new Date());
        snapshotAgent.put(address, contract);
    }

    @Override public void persist(ActionData actionData) {
        log.trace("persist... start process contract creation");
        ContractCreationAction creationAction = (ContractCreationAction) actionData.getCurrentAction();
        if (creationAction == null) {
            log.error("[ContractCreation.validate] convert action to ContractCreationAction is error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        Long blockHeight = actionData.getCurrentBlock().getBlockHeader().getHeight();
        String sender = actionData.getCurrentTransaction().getCoreTx().getSender();
        String txId = actionData.getCurrentTransaction().getCoreTx().getTxId();
        String address = generateAddress(blockHeight, sender, txId, creationAction);

        Contract contract = contractRepository.queryByAddress(address);
        if (null != contract) {
            // TODO [duhongming] custom SlaveErrorEnum
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_BLOCK_HEIGHT_UNEQUAL_ERROR);
        }
        contract = new Contract();
        contract.setAddress(address);
        contract.setLanguage(creationAction.getLanguage());
        contract.setCode(creationAction.getCode());
        contract.setCreateTime(new Date());

        contractRepository.deploy(contract);
    }
}

