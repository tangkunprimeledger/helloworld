package com.higgs.trust.rs.core.service;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.ContractService;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.rs.core.bo.ContractMigrationRequest;
import com.higgs.trust.rs.core.bo.ContractQueryRequest;
import com.higgs.trust.rs.core.dao.RequestDao;
import com.higgs.trust.rs.core.dao.po.RequestPO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.ContractVO;
import com.higgs.trust.slave.api.vo.PageVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.contract.StandardSmartContract;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.contract.ContractCreationAction;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeAction;
import com.higgs.trust.slave.model.bo.contract.ContractStateMigrationAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author duhongming
 * @date 2018/5/14
 */
@Service @Slf4j public class ContractServiceImpl implements ContractService {

    @Autowired
    private NodeState nodeState;
    @Autowired
    private SignService signService;
    @Autowired
    private CoreTransactionService coreTransactionService;
    @Autowired
    private RequestDao requestDao;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private StandardSmartContract smartContract;

    private CoreTransaction buildCoreTx(String txId, String code, Object... initArgs) {
        List<Action> actionList = new ArrayList<>(1);
        actionList.add(buildAction(code, initArgs));

        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(txId);
        coreTx.setActionList(actionList);
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setPolicyId(InitPolicyEnum.CONTRACT_ISSUE.getPolicyId());
        return coreTx;
    }

    private CoreTransaction buildCoreTx(String txId, Action action) {
        List<Action> actionList = new ArrayList<>(1);
        actionList.add(action);

        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(txId);
        coreTx.setActionList(actionList);
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setPolicyId(InitPolicyEnum.CONTRACT_ISSUE.getPolicyId());
        return coreTx;
    }

    private ContractCreationAction buildAction(String code, Object... initArgs) {
        ContractCreationAction creationAction = new ContractCreationAction();
        creationAction.setCode(code);
        creationAction.setType(ActionTypeEnum.REGISTER_CONTRACT);
        creationAction.setIndex(0);
        creationAction.setLanguage("javascript");
        creationAction.setInitArgs(initArgs);
        return creationAction;
    }

    private ContractInvokeAction buildInvokeAction(String address, Object... args) {
        ContractInvokeAction invokeAction = new ContractInvokeAction();
        invokeAction.setAddress(address);
        invokeAction.setArgs(args);
        invokeAction.setIndex(0);
        invokeAction.setType(ActionTypeEnum.TRIGGER_CONTRACT);
        return invokeAction;
    }

    private ContractStateMigrationAction buildMigrationAction(ContractMigrationRequest migrationRequest) {
        ContractStateMigrationAction action = new ContractStateMigrationAction();
        action.setFormInstanceAddress(migrationRequest.getFromAddress());
        action.setToInstanceAddress(migrationRequest.getToAddress());
        action.setIndex(0);
        action.setType(ActionTypeEnum.TRIGGER_CONTRACT);
        return action;
    }

    @Override
    public RespData<?> deploy(String txId, String code, Object... initArgs) {
        CoreTransaction coreTx = buildCoreTx(txId, code, initArgs);
        try {
            coreTransactionService.submitTx(coreTx);
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                RequestPO requestPO = requestDao.queryByRequestId(txId);
                return RespData.error(requestPO.getRespCode(), "RS_CORE_IDEMPOTENT", txId);
            }
        }
        com.higgs.trust.slave.api.vo.RespData respData = coreTransactionService.syncWait(txId, true);
        return respData.isSuccess() ? RespData.success(respData.getData()) : RespData.error(respData.getRespCode(), respData.getMsg(), respData.getData());
    }

    @Override
    public PageVO<ContractVO> queryList(Long height, String txId, Integer pageIndex, Integer pageSize) {
        PageVO<ContractVO> result = new PageVO();
        result.setTotal(contractRepository.queryCount(height, txId));
        List<com.higgs.trust.slave.model.bo.contract.Contract> list = contractRepository.query(height, txId, pageIndex, pageSize);
        result.setData(BeanConvertor.convertList(list, ContractVO.class));
        return result;
    }

    @Override
    public RespData invoke(String txId, String address, Object... args) {
        ContractInvokeAction action = buildInvokeAction(address, args);
        CoreTransaction coreTx = buildCoreTx(txId, action);
        try {
            coreTransactionService.submitTx(coreTx);
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                RequestPO requestPO = requestDao.queryByRequestId(txId);
                return RespData.error(requestPO.getRespCode(), "RS_CORE_IDEMPOTENT", txId);
            }
        }
        com.higgs.trust.slave.api.vo.RespData respData = coreTransactionService.syncWait(txId, true);
        return respData.isSuccess() ? RespData.success(respData.getData()) : RespData.error(respData.getRespCode(), respData.getMsg(), respData.getData());
    }

    @Override
    public RespData migration(ContractMigrationRequest migrationRequest) {
        ContractStateMigrationAction action = buildMigrationAction(migrationRequest);
        CoreTransaction coreTx = buildCoreTx(migrationRequest.getTxId(), action);
        try {
            coreTransactionService.submitTx(coreTx);
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                RequestPO requestPO = requestDao.queryByRequestId(migrationRequest.getTxId());
                return RespData.error(requestPO.getRespCode(), "RS_CORE_IDEMPOTENT", migrationRequest.getTxId());
            }
        }
        com.higgs.trust.slave.api.vo.RespData respData = coreTransactionService.syncWait(migrationRequest.getTxId(), true);
        return respData.isSuccess() ? RespData.success(respData.getData()) : RespData.error(respData.getRespCode(), respData.getMsg(), respData.getData());
    }

    @Override
    public Object query(ContractQueryRequest request) {
        return smartContract.executeQuery(request.getAddress(), request.getMethodName(), request.getBizArgs());
    }
}
