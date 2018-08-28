package com.higgs.trust.slave.core.service.action.dataidentity;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * dataidentity action handler
 *
 * @author lingchao
 * @create 2018年03月30日17:58
 */
@Slf4j
@Component
public class DataIdentityActionHandler implements ActionHandler {
    @Autowired
    private DataIdentityService dataIdentityService;

    @Override public void verifyParams(Action action) throws SlaveException {
        DataIdentityAction bo = (DataIdentityAction) action;
        if(StringUtils.isEmpty(bo.getIdentity()) || bo.getIdentity().length() > 64){
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(StringUtils.isEmpty(bo.getDataOwner()) || bo.getDataOwner().length() > 24){
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if(StringUtils.isEmpty(bo.getChainOwner()) || bo.getChainOwner().length() > 24){
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
    }

    @Override
    public void process(ActionData actionData) {
        log.debug("[ DataIdentityAction.process] is starting");
        dataIdentityService.process(actionData);
        log.debug("[ DataIdentityAction.process] is end");
    }



}
