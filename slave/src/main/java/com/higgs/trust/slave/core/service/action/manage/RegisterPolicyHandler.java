package com.higgs.trust.slave.core.service.action.manage;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.manage.PolicyDBHandler;
import com.higgs.trust.slave.core.service.datahandler.manage.PolicyHandler;
import com.higgs.trust.slave.core.service.datahandler.manage.PolicySnapshotHandler;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author tangfashuang
 * @date 2018/03/28
 * @desc register policy datahandler
 */
@Slf4j @Component public class RegisterPolicyHandler implements ActionHandler {

    @Autowired
    private PolicyDBHandler policyDBHandler;

    @Autowired
    private PolicySnapshotHandler policySnapshotHandler;

    @Override public void validate(ActionData actionData) {
        process(actionData, TxProcessTypeEnum.VALIDATE);
    }

    @Override public void persist(ActionData actionData) {
        process(actionData, TxProcessTypeEnum.PERSIST);
    }

    private void process(ActionData actionData, TxProcessTypeEnum processTypeEnum) {
        log.info("[RegisterPolicyHandler.process] start, actionData:{}, processType: {}",
            actionData, processTypeEnum);

        RegisterPolicy bo = (RegisterPolicy)actionData.getCurrentAction();
        if (null == bo) {
            log.error("[RegisterPolicyHandler.process] convert to RegisterPolicy error");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // validate param
        if (!BeanValidator.validate(bo).isSuccess()) {
            log.error("[RegisterPolicyHandler.process] param validate failed");
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
        if (!InitPolicyEnum.REGISTER.equals(register)) {
            log.error("[RegisterRSHandler.process] policy id is not for register policy");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // check if default policyId
        InitPolicyEnum initPolicyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(bo.getPolicyId());
        if (null != initPolicyEnum) {
            log.error("[RegisterPolicyHandler.process] cannot register default policy");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        PolicyHandler policyHandler = null;
        if (TxProcessTypeEnum.VALIDATE == processTypeEnum) {
            policyHandler = policySnapshotHandler;
        } else if (TxProcessTypeEnum.PERSIST == processTypeEnum) {
            policyHandler = policyDBHandler;
        }

        Policy policy = policyHandler.getPolicy(bo.getPolicyId());
        if (policy != null) {
            log.error("policy already exists. {}", policy);
            throw new SlaveException(SlaveErrorEnum.SLAVE_POLICY_EXISTS_ERROR);
        }
        policyHandler.registerPolicy(bo);

        log.info("[RegisterPolicyHandler.process] finish");
    }

}
