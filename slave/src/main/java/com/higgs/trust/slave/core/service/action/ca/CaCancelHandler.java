package com.higgs.trust.slave.core.service.action.ca;

import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
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
 * @desc cancel ca handler
 * @date 2018/6/6 10:25
 */
@Slf4j @Component public class CaCancelHandler implements ActionHandler {

    @Autowired CaSnapshotHandler caSnapshotHandler;
    @Autowired CaHelper caHelper;
    @Autowired private ClusterInfo clusterInfo;
    @Autowired private ConsensusStateMachine consensusStateMachine;

    /**
     * @param
     * @return
     * @desc process ca cancel action
     */
    @Override public void process(ActionData actionData) {

        // convert action and validate it
        CaAction caAction = (CaAction)actionData.getCurrentAction();
        log.info("[CaCancelHandler.process] start to process ca cancel action, user={}, pubKey={}", caAction.getUser(),
            caAction.getPubKey());

        if (!caHelper.validate(caAction, ActionTypeEnum.CA_CANCEL)) {
            log.error("[CaCancelHandler.process] actionData validate error, user={}, pubKey={}", caAction.getUser(),
                caAction.getPubKey());
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_VALIDATE_ERROR,
                "[CaCancelHandler.process] actionData validate error");
        }

        log.info("[CaCancelHandler.process] start to leave consensus,user ={}", caAction.getUser());
        consensusStateMachine.leaveConsensus();
        log.info("[CaCancelHandler.process] end leave consensus,user ={}", caAction.getUser());

        Profiler.enter("[CaCancelHandler.process]");
        Ca ca = new Ca();
        BeanUtils.copyProperties(caAction, ca);
        caSnapshotHandler.cancelCa(ca);

        clusterInfo.refresh();
        Profiler.release();

    }

}
