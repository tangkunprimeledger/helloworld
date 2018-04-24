package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.slave.core.repository.contract.AccountContractBindingRepository;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.snapshot.agent.AccountContractBindingSnapshotAgent;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import com.higgs.trust.slave.model.bo.contract.AccountContractBindingAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Date;

/**
 * the account contract binding action handler
 * @author duhongming
 * @date 2018-04-18
 */
@Slf4j @Component public class AccountContractBindingHandler implements ActionHandler {

    @Autowired AccountContractBindingSnapshotAgent snapshotAgent;
    @Autowired AccountContractBindingRepository repository;

    private String getHash(byte[] data) {
        // TODO [duhongming] use common method
        if (null == data) {
            return null;
        }

        byte[] cipherByte;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            cipherByte = md.digest();
            return Hex.encodeHexString(cipherByte);
        } catch (Exception e) {
            log.error("SHA-256: {}", e.getMessage());
        }
        return null;
    }

    private AccountContractBinding getAccountContractBinding(ActionData actionData, AccountContractBindingAction realAction) {
        AccountContractBinding binding = new AccountContractBinding();
        binding.setAccountNo(realAction.getAccountNo());
        binding.setContractAddress(realAction.getContractAddress());
        binding.setArgs(realAction.getArgs());
        binding.setBlockHeight(actionData.getCurrentBlock().getBlockHeader().getHeight());
        binding.setTxId(actionData.getCurrentTransaction().getCoreTx().getTxId());
        binding.setActionIndex(realAction.getIndex());
        binding.setCreateTime(new Date());

        String hashDataStr = binding.getAccountNo() + binding.getContractAddress() + binding.getBlockHeight()
                + binding.getTxId() + binding.getActionIndex();
        String hash = getHash(hashDataStr.getBytes());

        binding.setHash(hash);
        return binding;
    }

    @Override public void validate(ActionData actionData) {
        AccountContractBindingAction action = (AccountContractBindingAction) actionData.getCurrentAction();
        if (null == action) {
            log.error("[AccountContractBinding.validate] convert action to AccountContractBindingAction is error");
        }

        AccountContractBinding contractBinding = getAccountContractBinding(actionData, action);
        snapshotAgent.put(action.getAccountNo(), contractBinding);
    }

    @Override public void persist(ActionData actionData) {
        AccountContractBindingAction action = (AccountContractBindingAction) actionData.getCurrentAction();
        if (null == action) {
            log.error("[AccountContractBinding.validate] convert action to AccountContractBindingAction is error");
        }

        AccountContractBinding contractBinding = getAccountContractBinding(actionData, action);
        repository.add(contractBinding);
    }
}
