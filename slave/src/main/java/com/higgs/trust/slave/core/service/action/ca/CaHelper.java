package com.higgs.trust.slave.core.service.action.ca;

import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.datahandler.ca.CaSnapshotHandler;
import com.higgs.trust.slave.dao.po.ca.CaPO;
import com.higgs.trust.slave.model.bo.ca.CaAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j @Component public class CaHelper {
    @Autowired CaSnapshotHandler caSnapshotHandler;

    public boolean validate(CaAction caAction, ActionTypeEnum type) {
        // convert action and validate it
        log.info("[CaHelper.process] is start,params:{}", caAction);

        //validate idempotent
        CaPO caPO = caSnapshotHandler.getCa(caAction.getUser(),caAction.getUsage());
        if (type == ActionTypeEnum.CA_AUTH) {
            if (null != caPO && StringUtils.equals(caPO.getPubKey(), caAction.getPubKey()) && caPO.isValid()) {
                return false;
            } else {
                return true;
            }
        }

        if (type == ActionTypeEnum.CA_UPDATE) {
            if (null != caPO && !StringUtils.equals(caPO.getPubKey(), caAction.getPubKey())) {
                return true;
            } else {
                return false;
            }
        }

        if (type == ActionTypeEnum.CA_CANCEL) {
            if (null != caPO && StringUtils.equals(caPO.getPubKey(), caAction.getPubKey())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * param verify
     *
     * @param caAction
     * @throws SlaveException
     */
    public void verifyParams(CaAction caAction) throws SlaveException {
        if(StringUtils.isEmpty(caAction.getUser())){
            log.error("[verifyParams] user is null or illegal param:{}",caAction);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(StringUtils.isEmpty(caAction.getPubKey())){
            log.error("[verifyParams] pubKey is null or illegal param:{}",caAction);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(StringUtils.isEmpty(caAction.getUsage())){
            log.error("[verifyParams] usage is null or illegal param:{}",caAction);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
    }
}
