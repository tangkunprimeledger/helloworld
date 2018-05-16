package com.higgs.trust.rs.core.service;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.ContractService;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.rs.custom.dao.RequestDao;
import com.higgs.trust.rs.custom.dao.po.RequestPO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.ContractVO;
import com.higgs.trust.slave.api.vo.PageVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.contract.ContractRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.contract.ContractCreationAction;
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

    @Autowired private NodeState nodeState;
    @Autowired private SignService signService;
    @Autowired private CoreTransactionService coreTransactionService;
    @Autowired private RequestDao requestDao;
    @Autowired private ContractRepository contractRepository;

    private CoreTransaction buildCoreTx(String txId, String code) {
        List<Action> actionList = new ArrayList<>(1);
        actionList.add(buildAction(code));

        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(txId);
        coreTx.setActionList(actionList);
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setPolicyId(InitPolicyEnum.CONTRACT_ISSUE.getPolicyId());
        return coreTx;
    }

    private ContractCreationAction buildAction(String code) {
        ContractCreationAction creationAction = new ContractCreationAction();
        creationAction.setCode(code);
        creationAction.setType(ActionTypeEnum.REGISTER_CONTRACT);
        creationAction.setIndex(0);
        creationAction.setLanguage("javascript");
        return creationAction;
    }

    @Override
    public RespData<?> deploy(String txId, String code) {
        CoreTransaction coreTx = buildCoreTx(txId, code);
        try {
            coreTransactionService.submitTx(BizTypeEnum.CREATING_CONTRACT, coreTx);
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
}
