package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.evmcontract.facade.*;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
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

    private ContractCreationV2Action getAndCheckAction(ActionData actionData) {
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
        log.info("create contract transaction txId:{}", txId);
        long timestamp = actionData.getCurrentBlock().getBlockHeader().getBlockTime() / 1000;
        long number = actionData.getCurrentBlock().getBlockHeader().getHeight();
        byte[] senderAddress = Hex.decode(creationAction.getFrom());
        byte[] receiverAddress = Hex.decode(creationAction.getTo());
        log.info("contract address:{}", Hex.toHexString(receiverAddress));
        byte[] value = new BigInteger("0").toByteArray();
        byte[] data = Hex.decode(creationAction.getCode());
        byte[] parentHash = Hex.decode(parentBlockHash);
        //TODO tangKun address just for test ? 2018-12-12
        byte[] minerAddress = Hex.decode("095e7baea6a6c7c4c2dfeb977efac326af552d87");
        ContractExecutionContext contractExecutionContext = new ContractExecutionContext(
                ContractTypeEnum.CONTRACT_CREATION,
                txId.getBytes(),
                null,
                senderAddress,
                receiverAddress,
                value,
                data,
                parentHash,
                minerAddress,
                timestamp,
                number,
                blockchain.getBlockStore(),
                blockchain.getRepositorySnapshot()
        );
        Executor<ContractExecutionResult> executor = executorFactory.createExecutor(contractExecutionContext);
        ContractExecutionResult result = executor.execute();
        result.setResult(null);
        ContractExecutionResult.setCurrentResult(result);

    }
}

