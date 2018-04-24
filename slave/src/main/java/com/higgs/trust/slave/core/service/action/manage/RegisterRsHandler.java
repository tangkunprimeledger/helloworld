package com.higgs.trust.slave.core.service.action.manage;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.manage.RsDBHandler;
import com.higgs.trust.slave.core.service.datahandler.manage.RsHandler;
import com.higgs.trust.slave.core.service.datahandler.manage.RsSnapshotHandler;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author tangfashuang
 * @date 2018/03/28
 * @desc register RS datahandler
 */
@Slf4j @Component public class RegisterRsHandler implements ActionHandler {

    @Autowired private RsDBHandler rsDBHandler;

    @Autowired private RsSnapshotHandler rsSnapshotHandler;

    @Override public void validate(ActionData actionData) {
        process(actionData, TxProcessTypeEnum.VALIDATE);
    }

    @Override public void persist(ActionData actionData) {
        process(actionData, TxProcessTypeEnum.PERSIST);
    }

    private void process(ActionData actionData, TxProcessTypeEnum processTypeEnum) {
        log.info("[RegisterRSHandler.process] start, actionData: {}, processType: {} ", actionData, processTypeEnum);

        RegisterRS bo = (RegisterRS)actionData.getCurrentAction();
        if (null == bo) {
            log.error("[RegisterRSHandler.process] convert to RegisterRS failed");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // validate param
        if (!BeanValidator.validate(bo).isSuccess()) {
            log.error("[RegisterRSHandler.process] param validate is fail");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        //check policy id
        CoreTransaction coreTx = actionData.getCurrentTransaction().getCoreTx();
        InitPolicyEnum initPolicyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(coreTx.getPolicyId());
        if (!InitPolicyEnum.REGISTER.equals(initPolicyEnum)) {
            log.error("[RegisterRSHandler.process] policy id is not for register rs");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        RsHandler rsHandler = null;
        if (TxProcessTypeEnum.VALIDATE == processTypeEnum) {
            rsHandler = rsSnapshotHandler;
        } else if (TxProcessTypeEnum.PERSIST == processTypeEnum) {
            rsHandler = rsDBHandler;
        }

        RsPubKey rsPubKey = rsHandler.getRsPubKey(bo.getRsId());
        if (rsPubKey != null) {
            log.warn("rsPubKey already exists. {}", rsPubKey);
            throw new SlaveException(SlaveErrorEnum.SLAVE_RS_EXISTS_ERROR);
        }

        rsHandler.registerRsPubKey(bo);
        log.info("[RegisterRSHandler.process] finish");
    }

}
