package com.higgs.trust.slave.core.service.action.ca;

import com.higgs.trust.config.p2p.ClusterInfo;
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
 * @desc auth ca handler
 * @date 2018/6/6 10:25
 */
@Slf4j @Component public class CaAuthHandler implements ActionHandler {

    @Autowired CaSnapshotHandler caSnapshotHandler;
    @Autowired CaHelper caHelper;
    @Autowired ClusterInfo clusterInfo;

    @Override public void process(ActionData actionData) {
        CaAction caAction = (CaAction)actionData.getCurrentAction();

        log.info("[CaAuthHandler.process] start to process ca auth action, user={}, pubKey={}", caAction.getUser(),
            caAction.getPubKey());

        if (!caHelper.validate(caAction, ActionTypeEnum.CA_AUTH)) {
            log.error("[CaAuthHandler.process] actionData validate error, user={}, pubKey={}", caAction.getUser(),
                caAction.getPubKey());
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_VALIDATE_ERROR,
                "[CaAuthHandler.process] actionData validate error");
        }

        Profiler.enter("[CaAuthHandler.authCa]");
        Ca ca = new Ca();
        BeanUtils.copyProperties(caAction, ca);
        caSnapshotHandler.authCa(ca);
        Profiler.release();


        // TODO  添加refresh()方法属性集群配置信息
    }
}
