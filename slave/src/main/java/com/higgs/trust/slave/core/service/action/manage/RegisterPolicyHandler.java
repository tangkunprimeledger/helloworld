package com.higgs.trust.slave.core.service.action.manage;

import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.manage.PolicySnapshotHandler;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author tangfashuang
 * @date 2018/03/28
 * @desc register policy handler
 */
@Slf4j @Component public class RegisterPolicyHandler implements ActionHandler {

    @Autowired
    private PolicySnapshotHandler policySnapshotHandler;

    @Override public void verifyParams(Action action) throws SlaveException {
        RegisterPolicy bo = (RegisterPolicy)action;
        if(StringUtils.isEmpty(bo.getPolicyId()) || bo.getPolicyId().length() > 32){
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(StringUtils.isEmpty(bo.getPolicyName()) || bo.getPolicyName().length() > 64){
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(bo.getDecisionType() == null){
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(CollectionUtils.isEmpty(bo.getRsIds())){
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
    }

    @Override
    public void process(ActionData actionData) {
        RegisterPolicy bo = (RegisterPolicy)actionData.getCurrentAction();

        log.info("[RegisterPolicyHandler.process] start, actionData:{}", bo);

        if (null == bo) {
            log.error("[RegisterPolicyHandler.process] convert to RegisterPolicy error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // validate rs ids
        Set<String> rsIdSet = new HashSet<>();
        CollectionUtils.addAll(rsIdSet, bo.getRsIds());

        if (rsIdSet.size() != bo.getRsIds().size()) {
            log.error("[RegisterRSHandler.process] rs ids have duplicate.");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        //check policy id
        CoreTransaction coreTx = actionData.getCurrentTransaction().getCoreTx();
        InitPolicyEnum register = InitPolicyEnum.getInitPolicyEnumByPolicyId(coreTx.getPolicyId());
        if (!InitPolicyEnum.REGISTER_POLICY.equals(register)) {
            log.error("[RegisterRSHandler.process] policy id is not for register policy");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        if (!rsIdSet.contains(coreTx.getSender())) {
            log.error("[RegisterRSHandler.process] rsIds must have sender self");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // check if default policyId
        InitPolicyEnum initPolicyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(bo.getPolicyId());
        if (null != initPolicyEnum) {
            log.error("[RegisterPolicyHandler.process] cannot register default policy");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        Policy policy = policySnapshotHandler.getPolicy(bo.getPolicyId());
        if (policy != null) {
            log.error("policy already exists. {}", policy);
            throw new SlaveException(SlaveErrorEnum.SLAVE_POLICY_EXISTS_ERROR);
        }
        policySnapshotHandler.registerPolicy(bo);

        log.info("[RegisterPolicyHandler.process] finish");
    }

}
