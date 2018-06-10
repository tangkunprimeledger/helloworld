package com.higgs.trust.slave.core.service.action.ca;

import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.ca.CaSnapshotHandler;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.ca.CaAction;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author WangQuanzhou
 * @desc update ca handler
 * @date 2018/6/6 10:25
 */
@Slf4j @Component public class CaUpdateHandler implements ActionHandler {

    @Autowired CaSnapshotHandler caSnapshotHandler;
    @Autowired CaHelper caHelper;


    /**
     * the storage for the action
     *
     * @param actionData
     */
    @Override public void process(ActionData actionData) {
        // convert action and validate it
        CaAction caAction = (CaAction)actionData.getCurrentAction();

        if (!caHelper.validate(caAction, ActionTypeEnum.CA_UPDATE)) {
            log.error("[CaUpdateHandler.process] actionData validate error, user={}, pubKey={}", caAction.getUser(),
                caAction.getPubKey());
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_VALIDATE_ERROR,
                "[CaUpdateHandler.process] actionData validate error");
        }

        Profiler.enter("[CaUpdateHandler.updateCa]");
        Ca ca = new Ca();
        BeanUtils.copyProperties(caAction, ca);
        caSnapshotHandler.updateCa(ca);
        Profiler.release();
    }
}
