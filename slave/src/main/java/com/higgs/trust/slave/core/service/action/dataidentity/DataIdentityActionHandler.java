package com.higgs.trust.slave.core.service.action.dataidentity;

import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void process(ActionData actionData) {
        log.info("[ DataIdentityAction.process] is starting");
        dataIdentityService.process(actionData);
        log.info("[ DataIdentityAction.process] is end");
    }



}
