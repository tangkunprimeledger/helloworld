package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.evmcontract.facade.*;
import com.higgs.trust.evmcontract.solidity.Abi;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.contract.StandardExecuteContextData;
import com.higgs.trust.slave.core.service.contract.StandardSmartContract;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeAction;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeV2Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * @author kongyu
 * @date 2018/11/30
 */
@Slf4j
@Component
public class ContractInvokeV2Handler implements ActionHandler {

    private void processCustomerContractInvocation(ActionData actionData) {
        if (!(actionData.getCurrentAction() instanceof ContractInvokeV2Action)) {
            throw new IllegalArgumentException("action need a type of ContractInvokeV2Action");
        }
        ContractInvokeV2Action invokeAction = (ContractInvokeV2Action) actionData.getCurrentAction();
        this.verifyParams(invokeAction);

        Long blockHeight = actionData.getCurrentBlock().getBlockHeader().getHeight();
        String parentBlockHash = actionData.getCurrentBlock().getBlockHeader().getPreviousHash();
        String senderAddress = actionData.getCurrentTransaction().getCoreTx().getSender();
        String txId = actionData.getCurrentTransaction().getCoreTx().getTxId();
        String receiverAddress = invokeAction.getAddress();
        long timestamp = actionData.getCurrentBlock().getBlockHeader().getBlockTime();
        long nonce = invokeAction.getNonce();
        BigDecimal value = invokeAction.getValue();

        //方法签名（包含返回类型，传入实参列表）
        String callContract = invokeAction.getMethod();
        Abi.Function func = Abi.Function.of(callContract);
        byte[] invokeFuncData = func.encode();

        ContractExecutionContext contractExecutionContext = buildContractExecutionContext(ContractTypeEnum.CUSTOMER_CONTRACT_INVOCATION,
                txId.getBytes(),
                String.valueOf(nonce).getBytes(),
                senderAddress.getBytes(),
                receiverAddress.getBytes(),
                value.toString().getBytes(),
                invokeFuncData,
                parentBlockHash.getBytes(),
                new byte[]{},
                timestamp,
                blockHeight);

        ContractExecutorFactory executorFactory = new ContractExecutorFactory();

        Executor<ContractExecutionResult> executor = executorFactory.createExecutor(contractExecutionContext);
        ContractExecutionResult result = executor.execute();
        ContractExecutionResult.setCurrentResult(result);
    }

    private ContractExecutionContext buildContractExecutionContext(
            ContractTypeEnum contractType, byte[] transactionHash, byte[] nonce, byte[] senderAddress,
            byte[] receiverAddress, byte[] value, byte[] data, byte[] parentHash, byte[] minerAddress,
            long timestamp, long number) {
        return new ContractExecutionContext(contractType, transactionHash, nonce, senderAddress, receiverAddress,
                value, data, parentHash, minerAddress, timestamp, number, null, null);
    }

    @Override
    public void verifyParams(Action action) throws SlaveException {
        ContractInvokeV2Action invokeAction = (ContractInvokeV2Action)action;
        if (StringUtils.isEmpty(invokeAction.getAddress())) {
            log.error("invokeContract validate: address is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if (invokeAction.getAddress().length() > 64) {
            log.error("invokeContract validate: address is too long");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        if (StringUtils.isEmpty(invokeAction.getMethod())){
            log.error("invokeContract validate: method is empty");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
    }

    @Override
    public void process(ActionData actionData) {
        log.debug("contract invoke start");
        Profiler.enter("contract invoke");
        try {
            processCustomerContractInvocation(actionData);
        } finally {
            Profiler.release();
        }
    }
}
