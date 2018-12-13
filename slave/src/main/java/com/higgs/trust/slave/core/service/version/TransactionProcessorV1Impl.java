package com.higgs.trust.slave.core.service.version;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.evmcontract.core.TransactionResultInfo;
import com.higgs.trust.evmcontract.facade.ContractExecutionResult;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.Blockchain;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.action.account.*;
import com.higgs.trust.slave.core.service.action.ca.CaAuthHandler;
import com.higgs.trust.slave.core.service.action.ca.CaCancelHandler;
import com.higgs.trust.slave.core.service.action.ca.CaUpdateHandler;
import com.higgs.trust.slave.core.service.action.contract.*;
import com.higgs.trust.slave.core.service.action.dataidentity.DataIdentityActionHandler;
import com.higgs.trust.slave.core.service.action.manage.CancelRsHandler;
import com.higgs.trust.slave.core.service.action.manage.RegisterPolicyHandler;
import com.higgs.trust.slave.core.service.action.manage.RegisterRsHandler;
import com.higgs.trust.slave.core.service.action.node.NodeJoinHandler;
import com.higgs.trust.slave.core.service.action.node.NodeLeaveHandler;
import com.higgs.trust.slave.core.service.action.utxo.UTXOActionHandler;
import com.higgs.trust.slave.core.service.contract.StandardExecuteContextData;
import com.higgs.trust.slave.core.service.contract.StandardSmartContract;
import com.higgs.trust.slave.core.service.snapshot.agent.AccountContractBindingSnapshotAgent;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.account.AccountFreeze;
import com.higgs.trust.slave.model.bo.account.AccountOperation;
import com.higgs.trust.slave.model.bo.account.AccountTradeInfo;
import com.higgs.trust.slave.model.bo.account.AccountUnFreeze;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.context.TransactionData;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import com.higgs.trust.slave.model.bo.contract.ContractCreationV2Action;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeV2Action;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author WangQuanzhou
 * @desc transaction processor V1
 * @date 2018/3/28 18:01
 */
@Slf4j
@Component
public class TransactionProcessorV1Impl implements TransactionProcessor, InitializingBean {

    @Autowired
    TxProcessorHolder txProcessorHolder;

    @Autowired
    private OpenAccountHandler openAccountHandler;
    @Autowired
    private AccountOperationHandler accountOperationHandler;
    @Autowired
    private AccountFreezeHandler accountFreezeHandler;
    @Autowired
    private AccountUnFreezeHandler accountUnFreezeHandler;
    @Autowired
    private UTXOActionHandler utxoActionHandler;
    @Autowired
    private RegisterRsHandler registerRsHandler;
    @Autowired
    private RegisterPolicyHandler registerPolicyHandler;
    @Autowired
    private IssueCurrencyHandler issueCurrencyHandler;
    @Autowired
    private DataIdentityActionHandler dataIdentityActionHandler;
    @Autowired
    private ContractCreationHandler contractCreationHandler;
    @Autowired
    private ContractInvokeHandler contractInvokeHandler;
    @Autowired
    private ContractStateMigrationHandler contractStateMigrationHandler;
    @Autowired
    private AccountContractBindingHandler accountContractBindingHandler;
    @Autowired
    private AccountContractBindingSnapshotAgent accountContractBindingSnapshotAgent;
    @Autowired
    private StandardSmartContract standardSmartContract;
    @Autowired
    private CaAuthHandler caAuthHandler;
    @Autowired
    private CaCancelHandler caCancelHandler;
    @Autowired
    private CaUpdateHandler caUpdateHandler;
    @Autowired
    private CancelRsHandler cancelRsHandler;
    @Autowired
    private NodeJoinHandler nodeJoinHandler;
    @Autowired
    private NodeLeaveHandler nodeLeaveHandler;
    @Autowired
    private ContractInvokeV2Handler contractInvokeV2Handler;
    @Autowired
    private ContractCreationV2Handler contractCreationV2Handler;

    @Autowired
    private Blockchain blockchain;


    @Override
    public void afterPropertiesSet() throws Exception {
        txProcessorHolder.registVerisonProcessor(VersionEnum.V1, this);
    }

    @Override
    public void process(TransactionData transactionData) {
        boolean hashEvmContract = false;
        boolean isCreateEvmContract = false;
        CoreTransaction coreTx = transactionData.getCurrentTransaction().getCoreTx();
        log.debug("[process]coreTx:{}", coreTx);
        List<Action> actionList = coreTx.getActionList();
        if (CollectionUtils.isEmpty(actionList)) {
            return;
        }
        //sort by index
        Collections.sort(actionList, new Comparator<Action>() {
            @Override
            public int compare(Action o1, Action o2) {
                return o1.getIndex() > o2.getIndex() ? 1 : -1;
            }
        });
        //for each
        for (Action action : actionList) {
            if (action instanceof ContractCreationV2Action || action instanceof ContractInvokeV2Action) {
                isCreateEvmContract = action instanceof ContractCreationV2Action;
                hashEvmContract = true;
            }
            //set current action
            transactionData.setCurrentAction(action);

            //handle action
            ActionHandler actionHandler = getHandlerByType(action.getType());
            if (actionHandler == null) {
                log.error("[process] get action handler is null by action type:{}", action.getType());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACTION_HANDLER_IS_NOT_EXISTS_EXCEPTION);
            }
            //TODO do not bind account with contract
            //exeContract(action, transactionData.parseActionData());

            //execute action
            actionHandler.process(transactionData.parseActionData());
        }

        // TODO 处理结果
        if (hashEvmContract) {
            ContractExecutionResult executionResult = ContractExecutionResult.getCurrentResult();
            ContractExecutionResult.clearCurrentResult();
            if (executionResult != null) {
                long height = transactionData.getCurrentPackage().getHeight();
                TransactionResultInfo resultInfo = new TransactionResultInfo(height, coreTx.getTxId().getBytes(), 1,
                        executionResult.getBloomFilter(), executionResult.getLogInfoList(), executionResult.getResult());
                if (isCreateEvmContract) {
                    resultInfo.setCreatedAddress(executionResult.getReceiverAddress());
                }
                resultInfo.setError(executionResult.getErrorMessage());
                blockchain.putResultInfo(resultInfo);
            }
        }
    }

    /**
     * get action handler by action type
     *
     * @param typeEnum
     * @return
     */
    @Override
    public ActionHandler getHandlerByType(ActionTypeEnum typeEnum) {
        if (null == typeEnum) {
            log.error("[getHandlerByType] action type is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACTION_NOT_EXISTS_EXCEPTION,
                    "[getHandlerByType] action type is null");
        }
        switch (typeEnum) {
            case OPEN_ACCOUNT:
                return openAccountHandler;
            case UTXO:
                return utxoActionHandler;
            case FREEZE:
                return accountFreezeHandler;
            case UNFREEZE:
                return accountUnFreezeHandler;
            case ACCOUNTING:
                return accountOperationHandler;
            case REGISTER_RS:
                return registerRsHandler;
            case RS_CANCEL:
                return cancelRsHandler;
            case REGISTER_POLICY:
                return registerPolicyHandler;
            case ISSUE_CURRENCY:
                return issueCurrencyHandler;
            case CREATE_DATA_IDENTITY:
                return dataIdentityActionHandler;
            case BIND_CONTRACT:
                return accountContractBindingHandler;
            case TRIGGER_CONTRACT:
                return contractInvokeHandler;
            case REGISTER_CONTRACT:
                return contractCreationHandler;
            case CONTRACT_STATE_MIGRATION:
                return contractStateMigrationHandler;
            case CA_AUTH:
                return caAuthHandler;
            case CA_CANCEL:
                return caCancelHandler;
            case CA_UPDATE:
                return caUpdateHandler;
            case NODE_JOIN:
                return nodeJoinHandler;
            case NODE_LEAVE:
                return nodeLeaveHandler;
            case CONTRACT_INVOKED:
                return contractInvokeV2Handler;
            case CONTRACT_CREATION:
                return contractCreationV2Handler;
            default:
        }
        log.error("[getHandlerByType] action type not exist exception, actionType={}", JSON.toJSONString(typeEnum));
        throw new SlaveException(SlaveErrorEnum.SLAVE_ACTION_NOT_EXISTS_EXCEPTION,
                "[getHandlerByType] action type not exist exception");
    }

    private void exeContract(Action action, ActionData actionData) {
        List<String> accountNos = new ArrayList<>();
        switch (action.getType()) {
            case FREEZE:
                AccountFreeze accountFreeze = (AccountFreeze) action;
                accountNos.add(accountFreeze.getAccountNo());
                break;
            case UNFREEZE:
                AccountUnFreeze accountUnFreeze = (AccountUnFreeze) action;
                accountNos.add(accountUnFreeze.getAccountNo());
                break;
            case ACCOUNTING:
                AccountOperation accountOperation = (AccountOperation) action;
                List<AccountTradeInfo> debitTradeInfo = accountOperation.getDebitTradeInfo();
                Map<String, Object> map = new HashMap<>();
                if (!CollectionUtils.isEmpty(debitTradeInfo)) {
                    for (AccountTradeInfo accountTradeInfo : debitTradeInfo) {
                        map.put(accountTradeInfo.getAccountNo(), accountTradeInfo);
                    }
                }
                List<AccountTradeInfo> creditTradeInfo = accountOperation.getCreditTradeInfo();
                if (!CollectionUtils.isEmpty(creditTradeInfo)) {
                    for (AccountTradeInfo accountTradeInfo : creditTradeInfo) {
                        map.put(accountTradeInfo.getAccountNo(), accountTradeInfo);
                    }
                }
                accountNos.addAll(map.keySet());
                break;
            default:
        }
        if (CollectionUtils.isEmpty(accountNos)) {
            log.debug("[exeContract]accountNos is empty");
            return;
        }
        for (String accountNo : accountNos) {
            List<AccountContractBinding> bindingList = null;
            bindingList = accountContractBindingSnapshotAgent.getListByAccount(accountNo);
            if (CollectionUtils.isEmpty(bindingList)) {
                continue;
            }
            //execute contracts
            for (AccountContractBinding binding : bindingList) {
                StandardExecuteContextData standardExecuteContextData = new StandardExecuteContextData();
                standardExecuteContextData.setAction(actionData);
                //execute
                standardSmartContract.execute(binding, standardExecuteContextData);
            }
        }
    }
}
