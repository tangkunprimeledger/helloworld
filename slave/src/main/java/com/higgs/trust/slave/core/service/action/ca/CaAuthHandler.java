package com.higgs.trust.slave.core.service.action.ca;

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
 * @desc auth ca handler
 * @date 2018/6/6 10:25
 */
@Slf4j @Component public class CaAuthHandler implements ActionHandler {

    @Autowired CaSnapshotHandler caSnapshotHandler;
    @Autowired CaHelper caHelper;

    @Override public void process(ActionData actionData) {
        CaAction caAction = (CaAction)actionData.getCurrentAction();

        caHelper.validate(caAction);

        Profiler.enter("[DataIdentity.save]");
        Ca ca = new Ca();
        BeanUtils.copyProperties(caAction, ca);
        caSnapshotHandler.authCa(ca);
        Profiler.release();
    }
}
