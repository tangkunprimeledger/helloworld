package com.higgs.trust.slave.core.service.action.ca;

import com.higgs.trust.slave.api.enums.ActionTypeEnum;
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
        CaPO caPO = caSnapshotHandler.getCa(caAction.getUser());
        if (type == ActionTypeEnum.CA_AUTH) {
            if (null != caPO && StringUtils.equals(caPO.getPubKey(), caAction.getPubKey())) {
                return false;
            } else {
                return true;
            }
        }

        if (type != ActionTypeEnum.CA_AUTH) {
            if (null != caPO && StringUtils.equals(caPO.getPubKey(), caAction.getPubKey())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
