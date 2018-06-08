package com.higgs.trust.slave.core.service.action.utxo;

import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.model.bo.context.ActionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * UTXO action Handler
 *
 * @author lingchao
 * @create 2018年03月27日16:53
 */
@Slf4j
@Component
public class UTXOActionHandler implements ActionHandler {

    @Autowired
    private UTXOActionService utxoActionService;


    /**
     * action process
     *
     * @param actionData
     */
    @Override
    public void process(ActionData actionData) {
        log.info("[UTXOAction.validate] is starting!");
        utxoActionService.process(actionData);
        log.info("[UTXOAction.validate] is success!");
    }


}
