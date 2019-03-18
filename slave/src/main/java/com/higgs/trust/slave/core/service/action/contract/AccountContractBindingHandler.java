package com.higgs.trust.slave.core.service.action.contract;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.contract.AccountContractBindingRepository;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.snapshot.agent.AccountContractBindingSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import com.higgs.trust.slave.model.bo.contract.AccountContractBindingAction;
import com.higgs.trust.slave.model.bo.contract.Contract;
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

    private boolean addressIsExist(String address) {
        Contract contract =  contractSnapshotAgent.get(address);
        return contract != null;
    }

    private void check(AccountContractBindingAction action) {
        if (!addressIsExist(action.getContractAddress())) {
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

    @Override public void verifyParams(Action action) throws SlaveException {
        AccountContractBindingAction bo = (AccountContractBindingAction) action;
        if(StringUtils.isEmpty(bo.getAccountNo()) || bo.getAccountNo().length() > 64){
            log.error("[verifyParams] accountNo is null or illegal param:{}",bo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(StringUtils.isEmpty(bo.getContractAddress()) || bo.getContractAddress().length() != 64){
            log.error("[verifyParams] ContractAddress is null or illegal param:{}",bo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
    }

    @Override public void process(ActionData actionData) {
        if (!(actionData.getCurrentAction() instanceof AccountContractBindingAction)) {
            throw new IllegalArgumentException("action need a type of AccountContractBindingAction");
        }
        AccountContractBindingAction action = (AccountContractBindingAction) actionData.getCurrentAction();
        check(action);
        long blockHeight = actionData.getCurrentBlock().getBlockHeader().getHeight();
        String txId = actionData.getCurrentTransaction().getCoreTx().getTxId();
        process(action, blockHeight, txId);
    }

    private boolean exist(String hash) {
        return snapshotAgent.getBinding(hash) != null;
    }

    /**
     * process account contract binding
     * @param action
     * @param blockHeight
     * @param txId
     * @return
     */
    public AccountContractBinding process(final AccountContractBindingAction action, long blockHeight, String txId) {
        if (action == null) {
            log.error("action is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, "action is null");
        }

        AccountContractBinding contractBinding = getAccountContractBinding(action, blockHeight, txId);
        if (this.exist(contractBinding.getHash())) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, String.format("AccountContractBinding already exist: %s", contractBinding));
        }
        snapshotAgent.put(contractBinding);
        return contractBinding;
    }
}
