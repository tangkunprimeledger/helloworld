package com.higgs.trust.slave.core.service.action.contract;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.repository.contract.AccountContractBindingRepository;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.snapshot.agent.AccountContractBindingSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.model.bo.Contract;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import com.higgs.trust.slave.model.bo.contract.AccountContractBindingAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
    @Autowired ContractRepository contractRepository;
    @Autowired ContractSnapshotAgent contractSnapshotAgent;

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

    private boolean addressIsExist(String address, TxProcessTypeEnum processType) {
        Contract contract = processType == TxProcessTypeEnum.VALIDATE
                ? contractSnapshotAgent.get(address)
                : contractRepository.queryByAddress(address);
        return contract != null;
    }

    private void check(AccountContractBindingAction action, TxProcessTypeEnum processType) {
        BeanValidateResult validateResult = BeanValidator.validate(action);
        if (!validateResult.isSuccess()) {
            log.error("ContractBinding param validate is fail,first msg:{}", validateResult.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, validateResult.getFirstMsg());
        }

        if (!addressIsExist(action.getContractAddress(), processType)) {
            log.error("ContractBinding contract not exist {}", action.getContractAddress());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, String.format("contract not exist %s", action.getContractAddress()));
        }

        try {
            if (!StringUtils.isEmpty(action.getArgs())) {
                JSON.parse(action.getArgs());
            }
        } catch (Exception ex) {
            log.error("ContractBinding args parse error: {}", ex.getMessage());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, String.format("ContractBinding args parse error: %s", ex.getMessage()));
        }
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
        if (!(actionData.getCurrentAction() instanceof AccountContractBindingAction)) {
            throw new IllegalArgumentException("action need a type of AccountContractBindingAction");
        }
        AccountContractBindingAction action = (AccountContractBindingAction) actionData.getCurrentAction();
        check(action, processType);
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
