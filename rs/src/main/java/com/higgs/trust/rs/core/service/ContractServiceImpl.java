package com.higgs.trust.rs.core.service;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.evmcontract.crypto.ECKey;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.common.utils.CoreTransactionConvertor;
import com.higgs.trust.rs.core.api.ContractService;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.bo.ContractCreateV2Request;
import com.higgs.trust.rs.core.bo.ContractMigrationRequest;
import com.higgs.trust.rs.core.bo.ContractQueryRequest;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.TxTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.ContractVO;
import com.higgs.trust.slave.api.vo.PageVO;
import com.higgs.trust.slave.api.vo.QueryContractVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.core.service.contract.StandardSmartContract;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.contract.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author duhongming
 * @date 2018/5/14
 */
@Service @Slf4j public class ContractServiceImpl implements ContractService {

    @Autowired private NodeState nodeState;
    @Autowired private CoreTransactionService coreTransactionService;
    @Autowired private ContractRepository contractRepository;
    @Autowired private StandardSmartContract smartContract;
    @Autowired private CoreTransactionConvertor convertor;

    private CoreTransaction buildCoreTx(String txId, String code, Object... initArgs) {
        List<Action> actionList = new ArrayList<>(1);
        actionList.add(buildAction(code, initArgs));

        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(txId);
        coreTx.setActionList(actionList);
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setSendTime(new Date());
        coreTx.setPolicyId(InitPolicyEnum.CONTRACT_ISSUE.getPolicyId());
        coreTx.setTxType(TxTypeEnum.CONTRACT.getCode());
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
        coreTx.setSendTime(new Date());
        coreTx.setPolicyId(InitPolicyEnum.CONTRACT_INVOKE.getPolicyId());
        coreTx.setTxType(TxTypeEnum.CONTRACT.getCode());
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

    private ContractInvokeV2Action buildInvokeV2Action(String from, String to, BigDecimal value, String methodSignature,
        Object... args) {
        ContractInvokeV2Action invokeV2Action = new ContractInvokeV2Action();
        invokeV2Action.setFrom(from);
        invokeV2Action.setTo(to);
        invokeV2Action.setArgs(args);
        invokeV2Action.setIndex(0);
        invokeV2Action.setType(ActionTypeEnum.CONTRACT_INVOKED);
        invokeV2Action.setValue(value);
        invokeV2Action.setMethodSignature(methodSignature);
        return invokeV2Action;
    }

    private ContractStateMigrationAction buildMigrationAction(ContractMigrationRequest migrationRequest) {
        ContractStateMigrationAction action = new ContractStateMigrationAction();
        action.setFormInstanceAddress(migrationRequest.getFromAddress());
        action.setToInstanceAddress(migrationRequest.getToAddress());
        action.setIndex(0);
        action.setType(ActionTypeEnum.TRIGGER_CONTRACT);
        return action;
    }

    @Override public RespData<?> deploy(String txId, String code, Object... initArgs) {
        CoreTransaction coreTx = buildCoreTx(txId, code, initArgs);
        try {
            coreTransactionService.submitTx(coreTx);
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                RsCoreTxVO rsCoreTxVO = coreTransactionService.queryCoreTx(txId);
                if (rsCoreTxVO.getExecuteResult() != CoreTxResultEnum.SUCCESS) {
                    return RespData.error(rsCoreTxVO.getErrorCode(), rsCoreTxVO.getErrorMsg(), txId);
                } else {
                    return RespData.success(txId);
                }
            }
        }
        com.higgs.trust.slave.api.vo.RespData respData = coreTransactionService.syncWait(txId, true);
        return respData.isSuccess() ? RespData.success(respData.getData()) :
            RespData.error(respData.getRespCode(), respData.getMsg(), respData.getData());
    }

    @Override
    public RespData<?> deployV2(ContractCreateV2Request request) {
        String txId = request.getTxId();
        try {
            if (StringUtils.isEmpty(request.getContractAddress())) {
                ECKey ecKey = new ECKey();
                String address = Hex.toHexString(ecKey.getAddress());
                request.setContractAddress(address);
            }
            String hexCode = convertor
                .buildContractCode(request.getSourceCode(), request.getContractor(),
                    request.getInitArgs());
            ContractCreationV2Action createAction = convertor
                .buildContractCreationV2Action(request.getFromAddr(), request.getContractAddress(), hexCode, 0);
            CoreTransaction coreTx = buildCoreTx(txId, createAction);
            coreTransactionService.submitTx(coreTx);
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                RsCoreTxVO rsCoreTxVO = coreTransactionService.queryCoreTx(txId);
                if (rsCoreTxVO.getExecuteResult() != CoreTxResultEnum.SUCCESS) {
                    return RespData.error(rsCoreTxVO.getErrorCode(), rsCoreTxVO.getErrorMsg(), txId);
                } else {
                    return RespData.success(txId);
                }
            }
        }
        com.higgs.trust.slave.api.vo.RespData respData = coreTransactionService.syncWait(txId, true);
        return respData.isSuccess() ? RespData.success(respData.getData()) :
            RespData.error(respData.getRespCode(), respData.getMsg(), respData.getData());
    }

    @Override public PageVO<ContractVO> queryList(Long height, String txId, Integer pageIndex, Integer pageSize) {
        PageVO<ContractVO> result = new PageVO();
        result.setTotal(contractRepository.queryCount(height, txId));
        List<com.higgs.trust.slave.model.bo.contract.Contract> list =
            contractRepository.query(height, txId, pageIndex, pageSize);
        result.setData(BeanConvertor.convertList(list, ContractVO.class));
        return result;
    }

    @Override public List<ContractVO> queryContractsByPage(QueryContractVO req) {
        if (null == req) {
            return null;
        }
        //less than minimum，use default value
        Integer minNo = 0;
        if (null == req.getPageNo() || req.getPageNo().compareTo(minNo) <= 0) {
            req.setPageNo(1);
        }
        //over the maximum，use default value
        Integer maxSize = 100;
        if (null == req.getPageSize() || req.getPageSize().compareTo(maxSize) == 1) {
            req.setPageSize(20);
        }
        List<com.higgs.trust.slave.model.bo.contract.Contract> list =
            contractRepository.query(req.getHeight(), req.getTxId(), req.getPageNo(), req.getPageSize());
        List<ContractVO> result = BeanConvertor.convertList(list, ContractVO.class);
        return result;
    }

    @Override public RespData invoke(String txId, String address, Object... args) {
        ContractInvokeAction action = buildInvokeAction(address, args);
        CoreTransaction coreTx = buildCoreTx(txId, action);
        try {
            coreTransactionService.submitTx(coreTx);
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                RsCoreTxVO rsCoreTxVO = coreTransactionService.queryCoreTx(txId);
                if (rsCoreTxVO.getExecuteResult() != CoreTxResultEnum.SUCCESS) {
                    return RespData.error(rsCoreTxVO.getErrorCode(), rsCoreTxVO.getErrorMsg(), txId);
                } else {
                    return RespData.success(txId);
                }
            }
        }
        com.higgs.trust.slave.api.vo.RespData respData = coreTransactionService.syncWait(txId, true);
        return respData.isSuccess() ? RespData.success(respData.getData()) :
            RespData.error(respData.getRespCode(), respData.getMsg(), respData.getData());
    }

    /**
     * invoke v2 contract
     *
     * @param txId
     * @param address
     * @param nonce
     * @param value
     * @param methodSignature
     * @param args
     * @return
     */
    @Override public RespData invokeV2(String txId, String from, String to, BigDecimal value, String methodSignature,
        Object... args) {
        ContractInvokeV2Action action = buildInvokeV2Action(from, to, value, methodSignature, args);
        CoreTransaction coreTx = buildCoreTx(txId, action);
        try {
            coreTransactionService.submitTx(coreTx);
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                RsCoreTxVO rsCoreTxVO = coreTransactionService.queryCoreTx(txId);
                if (rsCoreTxVO.getExecuteResult() != CoreTxResultEnum.SUCCESS) {
                    return RespData.error(rsCoreTxVO.getErrorCode(), rsCoreTxVO.getErrorMsg(), txId);
                } else {
                    return RespData.success(txId);
                }
            }
        }
        com.higgs.trust.slave.api.vo.RespData respData = coreTransactionService.syncWait(txId, true);
        return respData.isSuccess() ? RespData.success(respData.getData()) :
            RespData.error(respData.getRespCode(), respData.getMsg(), respData.getData());
    }

    @Override public RespData migration(ContractMigrationRequest migrationRequest) {
        ContractStateMigrationAction action = buildMigrationAction(migrationRequest);
        CoreTransaction coreTx = buildCoreTx(migrationRequest.getTxId(), action);
        try {
            coreTransactionService.submitTx(coreTx);
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                RsCoreTxVO rsCoreTxVO = coreTransactionService.queryCoreTx(migrationRequest.getTxId());
                if (rsCoreTxVO.getExecuteResult() != CoreTxResultEnum.SUCCESS) {
                    return RespData
                        .error(rsCoreTxVO.getErrorCode(), rsCoreTxVO.getErrorMsg(), migrationRequest.getTxId());
                } else {
                    return RespData.success(migrationRequest.getTxId());
                }
            }
        }
        com.higgs.trust.slave.api.vo.RespData respData =
            coreTransactionService.syncWait(migrationRequest.getTxId(), true);
        return respData.isSuccess() ? RespData.success(respData.getData()) :
            RespData.error(respData.getRespCode(), respData.getMsg(), respData.getData());
    }

    @Override public Object query(ContractQueryRequest request) {
        return smartContract.executeQuery(request.getAddress(), request.getMethodName(), request.getBizArgs());
    }

    @Override public ContractVO queryByTxId(String txId, int actionIndex) {
        return contractRepository.queryByTxId(txId, actionIndex);
    }
}
