package com.higgs.trust.slave.core.service.action.ca;

import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.core.service.datahandler.ca.CaSnapshotHandler;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WangQuanzhou
 * @desc cancel ca handler
 * @date 2018/6/6 10:25
 */
@Slf4j @Component public class CaCancelHandler implements ActionHandler {

    @Autowired CaSnapshotHandler caSnapshotHandler;

    @Override
    public void process(ActionData actionData){

    }


}
