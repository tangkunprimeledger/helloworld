package com.higgs.trust.slave.core.service.version;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.contract.AccountContractBindingRepository;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.action.account.*;
import com.higgs.trust.slave.core.service.action.contract.AccountContractBindingHandler;
import com.higgs.trust.slave.core.service.action.contract.ContractCreationHandler;
import com.higgs.trust.slave.core.service.action.contract.ContractInvokeHandler;
import com.higgs.trust.slave.core.service.action.dataidentity.DataIdentityActionHandler;
import com.higgs.trust.slave.core.service.action.manage.RegisterPolicyHandler;
import com.higgs.trust.slave.core.service.action.manage.RegisterRsHandler;
import com.higgs.trust.slave.core.service.action.utxo.UTXOActionHandler;
import com.higgs.trust.slave.core.service.contract.StandardExecuteContextData;
import com.higgs.trust.slave.core.service.contract.StandardSmartContract;
import com.higgs.trust.slave.core.service.contract.UTXOExecuteContextData;
import com.higgs.trust.slave.core.service.snapshot.agent.AccountContractBindingSnapshotAgent;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.account.AccountFreeze;
import com.higgs.trust.slave.model.bo.account.AccountOperation;
import com.higgs.trust.slave.model.bo.account.AccountUnFreeze;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.context.TransactionData;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import com.higgs.trust.slave.model.bo.contract.ContractCreationAction;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author WangQuanzhou
 * @desc transaction processor V1
 * @date 2018/3/28 18:01
 */
@Slf4j @Component public class TransactionProcessorV1Impl implements TransactionProcessor, InitializingBean {

    @Autowired TxProcessorHolder txProcessorHolder;

    @Autowired private OpenAccountHandler openAccountHandler;
    @Autowired private AccountOperationHandler accountOperationHandler;
    @Autowired private AccountFreezeHandler accountFreezeHandler;
    @Autowired private AccountUnFreezeHandler accountUnFreezeHandler;
    @Autowired private UTXOActionHandler utxoActionHandler;
    @Autowired private RegisterRsHandler registerRsHandler;
    @Autowired private RegisterPolicyHandler registerPolicyHandler;
    @Autowired private IssueCurrencyHandler issueCurrencyHandler;
    @Autowired private DataIdentityActionHandler dataIdentityActionHandler;
    @Autowired private ContractCreationHandler contractCreationHandler;
    @Autowired private ContractInvokeHandler contractInvokeHandler;
    @Autowired private AccountContractBindingHandler accountContractBindingHandler;
    @Autowired AccountContractBindingSnapshotAgent accountContractBindingSnapshotAgent;
    @Autowired AccountContractBindingRepository accountContractBindingRepository;
    @Autowired StandardSmartContract standardSmartContract;

    @Override public void afterPropertiesSet() throws Exception {
        txProcessorHolder.registVerisonProcessor(VersionEnum.V1, this);
    }

    @Override public void process(TransactionData transactionData, TxProcessTypeEnum processTypeEnum) {
        CoreTransaction coreTx = transactionData.getCurrentTransaction().getCoreTx();
        log.info("[process]coreTx:{}", coreTx);
        List<Action> actionList = coreTx.getActionList();
        //sort by index
        Collections.sort(actionList, new Comparator<Action>() {
            @Override public int compare(Action o1, Action o2) {
                return o1.getIndex() > o2.getIndex() ? 1 : -1;
            }
        });
        //for each
        for (Action action : actionList) {
            //set current action
            transactionData.setCurrentAction(action);

            //handle action
            ActionHandler actionHandler = getHandlerByType(action.getType());
            if (actionHandler == null) {
                log.error("[process] get action handler is null by action type:{}", action.getType());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACTION_HANDLER_IS_NOT_EXISTS_EXCEPTION);
            }
            if (processTypeEnum == TxProcessTypeEnum.VALIDATE) {
                actionHandler.validate(transactionData.getActionData());
            } else if (processTypeEnum == TxProcessTypeEnum.PERSIST) {
                actionHandler.persist(transactionData.getActionData());
            }
            //execute contract
            exeContract(action,processTypeEnum,transactionData.getActionData());
        }
    }

    /**
     * get action handler by action type
     *
     * @param typeEnum
     * @return
     */
    private ActionHandler getHandlerByType(ActionTypeEnum typeEnum) {
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
            case REGISTER_POLICY:
                return registerPolicyHandler;
            case ISSUE_CURRENCY:
                return issueCurrencyHandler;
            case CREATE_DATA_IDENTITY:
                return dataIdentityActionHandler;
            case BIND_CONTRACT:
                return contractCreationHandler;
            case TRIGGER_CONTRACT:
                return contractInvokeHandler;
            case REGISTER_CONTRACT:
                return accountContractBindingHandler;
            default:
        }
        log.error("[getHandlerByType] action type not exist exception, actionType={}", JSON.toJSONString(typeEnum));
        throw new SlaveException(SlaveErrorEnum.SLAVE_ACTION_NOT_EXISTS_EXCEPTION,
            "[getHandlerByType] action type not exist exception");
    }

    private void exeContract(Action action,TxProcessTypeEnum processTypeEnum,ActionData actionData){
        String accountNo = null;
        switch (action.getType()) {
            case FREEZE:
                AccountFreeze accountFreeze = (AccountFreeze)action;
                accountNo = accountFreeze.getAccountNo();
                break;
            case UNFREEZE:
                AccountUnFreeze accountUnFreeze = (AccountUnFreeze)action;
                accountNo = accountUnFreeze.getAccountNo();
                break;
            case ACCOUNTING:
                AccountOperation accountOperation = (AccountOperation)action;
//                accountNo = accountOperation.
                break;
            default:
        }
        if(StringUtils.isEmpty(accountNo)){
            log.info("[exeContract]accountNo is empty");
            return;
        }
        List<AccountContractBinding> bindingList = null;
        if (processTypeEnum == TxProcessTypeEnum.VALIDATE) {
            bindingList = accountContractBindingSnapshotAgent.get(accountNo);
        } else if (processTypeEnum == TxProcessTypeEnum.PERSIST) {
            bindingList = accountContractBindingRepository.queryListByAccountNo(accountNo);
        }
        if(CollectionUtils.isEmpty(bindingList)){
            return;
        }
        //execute contracts
        for(AccountContractBinding binding : bindingList){
            StandardExecuteContextData standardExecuteContextData = new StandardExecuteContextData();
            standardExecuteContextData.setAction(actionData);
            //execute
            standardSmartContract.execute(binding,standardExecuteContextData,processTypeEnum);
        }
    }
}
