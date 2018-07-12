package com.higgs.trust.slave.core.service.action.manage;

import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.manage.RsSnapshotHandler;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsNode;
import com.higgs.trust.slave.model.enums.biz.RsNodeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author tangfashuang
 * @date 2018/03/28
 * @desc register RS handler
 */
@Slf4j
@Component
public class RegisterRsHandler implements ActionHandler {

    @Autowired
    private RsSnapshotHandler rsSnapshotHandler;

    @Autowired
    private CaRepository caRepository;

    @Override
    public void process(ActionData actionData) {
        log.info("[RegisterRSHandler.process] start, actionData: {} ", actionData);

        RegisterRS bo = (RegisterRS) actionData.getCurrentAction();
        if (null == bo) {
            log.error("[RegisterRSHandler.process] convert to RegisterRS failed");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        //check rsId and sender
        CoreTransaction coreTx = actionData.getCurrentTransaction().getCoreTx();
        String rsId = bo.getRsId();
        if (!StringUtils.equals(rsId, coreTx.getSender())) {
            log.error("[RegisterRSHandler.process] register rsId:{} is not equals transaction sender: {}", rsId, coreTx.getSender());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        //check policy id
        InitPolicyEnum initPolicyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(coreTx.getPolicyId());
        if (!InitPolicyEnum.REGISTER_RS.equals(initPolicyEnum)) {
            log.error("[RegisterRSHandler.process] policy id is not for register rs");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        Ca ca = caRepository.getCa(rsId);
        if (null == ca || !ca.isValid()) {
            log.error("[RegisterRSHandler.process] ca not register or is not valid which rsId={}", rsId);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        RsNode rsNode = rsSnapshotHandler.getRsNode(rsId);
        if (rsNode != null) {
            if (rsNode.getStatus() == RsNodeStatusEnum.COMMON) {
                log.warn("rsNode already exists. rsId={}", rsId);
                throw new SlaveException(SlaveErrorEnum.SLAVE_RS_EXISTS_ERROR);
            } else {
                rsSnapshotHandler.updateRsNode(rsId, RsNodeStatusEnum.COMMON);
            }
        } else {
            rsSnapshotHandler.registerRsNode(bo);
        }

        log.info("[RegisterRSHandler.process] finish");
    }

}
