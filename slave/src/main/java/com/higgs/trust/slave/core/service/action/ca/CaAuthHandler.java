package com.higgs.trust.slave.core.service.action.ca;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
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
 * @desc auth ca handler
 * @date 2018/6/6 10:25
 */
@Slf4j @Component public class CaAuthHandler implements ActionHandler {

    @Autowired CaSnapshotHandler caSnapshotHandler;

    @Override public void process(ActionData actionData) {
        // convert action and validate it
        CaAction caAction = (CaAction)actionData.getCurrentAction();
        log.info("[CaAuthHandler.process] is start,params:{}", caAction);
        try {
            BeanValidator.validate(caAction).failThrow();
        } catch (IllegalArgumentException e) {
            log.error("Convert and validate caAction is error .msg={}", e.getMessage());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR, e);
        }

        //validate idempotent
        Ca ca = caSnapshotHandler.getCa(caAction.getUser());
        if (null != ca) {
            log.error("[CaAuthHandler.process] idempotent exception, nodeName={}, pubKey={}", caAction.getUser(),
                caAction.getPubKey());
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }

        Profiler.enter("[DataIdentity.save]");
        ca = new Ca();
        BeanUtils.copyProperties(caAction, ca);
        caSnapshotHandler.authCa(ca);
        Profiler.release();
    }
}
