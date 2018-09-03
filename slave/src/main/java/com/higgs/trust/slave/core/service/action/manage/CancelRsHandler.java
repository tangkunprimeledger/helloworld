package com.higgs.trust.slave.core.service.action.manage;

import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.manage.RsSnapshotHandler;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.manage.CancelRS;
import com.higgs.trust.slave.model.bo.manage.RsNode;
import com.higgs.trust.slave.model.enums.biz.RsNodeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author tangfashuang
 * @date 2018/06/07 20:38
 * @desc cancel rs handler
 */
@Slf4j
@Component
public class CancelRsHandler implements ActionHandler {

    @Autowired private RsSnapshotHandler rsSnapshotHandler;

    @Autowired private CaRepository caRepository;

    @Override public void verifyParams(Action action) throws SlaveException {
        CancelRS bo = (CancelRS)action;
        if(StringUtils.isEmpty(bo.getRsId())){
            log.error("[verifyParams] rsId is null or illegal param:{}",bo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
    }

    @Override public void process(ActionData actionData) {
        CancelRS bo = (CancelRS)actionData.getCurrentAction();
        log.info("[CancelRSHandler.process] start, actionData: {} ", bo);

        if (null == bo) {
            log.error("[CancelRSHandler.process] convert to CancelRS failed");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        //check rsId and sender
        CoreTransaction coreTx = actionData.getCurrentTransaction().getCoreTx();
        String rsId = bo.getRsId();
        if (!StringUtils.equals(rsId, coreTx.getSender())) {
            log.error("[CancelRSHandler.process] cancel rsId:{} is not equals transaction sender: {}", bo.getRsId(), coreTx.getSender());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        //check policy id
        InitPolicyEnum initPolicyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(coreTx.getPolicyId());
        if (!InitPolicyEnum.CANCEL_RS.equals(initPolicyEnum)) {
            log.error("[CancelRSHandler.process] policy id is not for cancel rs");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        Ca ca = caRepository.getCaForBiz(rsId);
        if (null == ca || !ca.isValid()) {
            log.error("[CancelRSHandler.process] ca not register or is not valid which rsId={}", rsId);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        RsNode rsNode = rsSnapshotHandler.getRsNode(rsId);
        if (null == rsNode) {
            log.error("[CancelRSHandler.process] rsNode not exist, rsId={}", rsId);
            throw new SlaveException(SlaveErrorEnum.SLAVE_RS_NOT_EXISTS_ERROR);
        }

        if (rsNode.getStatus() != RsNodeStatusEnum.COMMON) {
            log.warn("rs status is not common. rsId={}", rsId);
            throw new SlaveException(SlaveErrorEnum.SLAVE_RS_ALREADY_CANCELED_ERROR);
        }

        rsSnapshotHandler.updateRsNode(rsId, RsNodeStatusEnum.CANCELED);
        log.info("[CancelRSHandler.process] finish");
    }
}
