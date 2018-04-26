package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
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
        long blockHeight = actionData.getCurrentBlock().getBlockHeader().getHeight();
        String txId = actionData.getCurrentTransaction().getCoreTx().getTxId();
        return getAccountContractBinding(realAction, blockHeight, txId);
    }

    private AccountContractBinding getAccountContractBinding(AccountContractBindingAction realAction, long blockHeight, String txId) {
        AccountContractBinding binding = new AccountContractBinding();
        binding.setAccountNo(realAction.getAccountNo());
        binding.setContractAddress(realAction.getContractAddress());
        binding.setArgs(realAction.getArgs());
        binding.setBlockHeight(blockHeight);
        binding.setTxId(txId);
        binding.setActionIndex(realAction.getIndex());
        binding.setCreateTime(new Date());

        String hashDataStr = binding.getAccountNo() + binding.getContractAddress() + binding.getBlockHeight()
                + binding.getTxId() + binding.getActionIndex();
        String hash = getHash(hashDataStr.getBytes());

        binding.setHash(hash);
        return binding;
    }

    private void process(ActionData actionData, TxProcessTypeEnum processType) {
        AccountContractBindingAction action = (AccountContractBindingAction) actionData.getCurrentAction();
        if (action == null) {
            log.error("[AccountContractBinding.validate] convert action to AccountContractBindingAction is error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, "convert action to AccountContractBindingAction is error");
        }

        long blockHeight = actionData.getCurrentBlock().getBlockHeader().getHeight();
        String txId = actionData.getCurrentTransaction().getCoreTx().getTxId();
        process(action, blockHeight, txId, processType);
    }

    private boolean exist(String hash, TxProcessTypeEnum processType) {
        if (processType == TxProcessTypeEnum.VALIDATE) {
            return snapshotAgent.getBinding(hash) != null;
        }
        return repository.queryByHash(hash) != null;
    }

    /**
     * process account contract binding
     * @param action
     * @param blockHeight
     * @param txId
     * @param processType
     * @return
     */
    public AccountContractBinding process(final AccountContractBindingAction action, long blockHeight, String txId, final TxProcessTypeEnum processType) {
        if (action == null) {
            log.error("action is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, "action is null");
        }

        AccountContractBinding contractBinding = getAccountContractBinding(action, blockHeight, txId);
        if (this.exist(contractBinding.getHash(), processType)) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, String.format("AccountContractBinding already exist: %s", contractBinding));
        }

        if (processType == TxProcessTypeEnum.VALIDATE) {
            snapshotAgent.put(contractBinding);
        } else {
            repository.add(contractBinding);
        }
        return contractBinding;
    }

    @Override public void validate(ActionData actionData) {
        process(actionData, TxProcessTypeEnum.VALIDATE);
    }

    @Override public void persist(ActionData actionData) {
        process(actionData, TxProcessTypeEnum.PERSIST);
    }
}
