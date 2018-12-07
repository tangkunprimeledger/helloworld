package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.evmcontract.crypto.HashUtil;
import com.higgs.trust.evmcontract.facade.*;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.ContractException;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.Blockchain;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.ContractCreationV2Action;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * create contract handler
 *
 * @author tangkun
 */
@Slf4j
@Component
public class ContractCreationV2Handler implements ActionHandler {

    @Autowired
    private Blockchain blockchain;

    private int nonceI = 0;

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


    private void checkPolicy(ActionData actionData) {
        String policyId = actionData.getCurrentTransaction().getCoreTx().getPolicyId();
        if (InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId) != InitPolicyEnum.CONTRACT_ISSUE) {
            log.error("policyId error: {}", policyId);
            throw new ContractException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, String.format("policyId error: %s", policyId));
        }
    }

    private ContractCreationV2Action getAndCheckAction(ActionData actionData) {
        checkPolicy(actionData);
        verifyParams(actionData.getCurrentAction());
        return (ContractCreationV2Action) actionData.getCurrentAction();
    }

    @Override
    public void verifyParams(Action action) throws SlaveException {
        ContractCreationV2Action creationAction = (ContractCreationV2Action) action;
        if (creationAction == null) {
            log.error("[ContractCreation] convert action to ContractCreationV2Action is error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if (StringUtils.isEmpty(creationAction.getCode())) {
            log.error("[ContractCreation] code is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, "code is empty");
        }


//        if (!"javascript".equals(creationAction.getLanguage())) {
//            log.error("[ContractCreation] language is error: {}", creationAction.getLanguage());
//            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, String.format("language is error: %s", creationAction.getLanguage()));
//        }
    }

    @Override
    public void process(ActionData actionData) {

        ExecutorFactory executorFactory = new ContractExecutorFactory();
        ContractCreationV2Action creationAction = getAndCheckAction(actionData);

        String parentBlockHash = blockchain.getLastBlockHeader().getBlockHash();
        String txId = actionData.getCurrentTransaction().getCoreTx().getTxId();
        long timestamp = actionData.getCurrentBlock().getBlockHeader().getBlockTime();
        long number = actionData.getCurrentBlock().getBlockHeader().getHeight();
        byte[] nonce = new BigInteger(String.valueOf(nonceI++)).toByteArray();
        byte[] senderAddress = "095e7baea6a6c7c4c2dfeb977efac326af552d87".getBytes();
        byte[] receiverAddress = HashUtil.calcNewAddr(senderAddress, nonce);
        byte[] value = new BigInteger("0").toByteArray();
        byte[] data = Hex.decode(creationAction.getCode());
        byte[] parentHash = parentBlockHash.getBytes();
        byte[] minerAddress = new byte[]{};
        ContractExecutionContext contractExecutionContext = new ContractExecutionContext(
                ContractTypeEnum.CONTRACT_CREATION,
                txId.getBytes(),
                nonce,
                senderAddress,
                receiverAddress,
                value,
                data,
                parentHash,
                minerAddress,
                timestamp,
                number,
                blockchain.getBlockStore(),
                blockchain.getRepository()
        );
        Executor<ContractExecutionResult> executor = executorFactory.createExecutor(contractExecutionContext);
        ContractExecutionResult result = executor.execute();
        ContractExecutionResult.setCurrentResult(result);

    }
}

