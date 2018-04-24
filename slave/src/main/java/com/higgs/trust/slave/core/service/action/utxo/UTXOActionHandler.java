package com.higgs.trust.slave.core.service.action.utxo;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
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
     * action validate
     *
     * @param actionData
     */
    @Override
    public void validate(ActionData actionData) {
        log.info("[UTXOAction.validate] is starting!");
        utxoActionService.process(actionData, TxProcessTypeEnum.VALIDATE);
        log.info("[UTXOAction.validate] is success!");
    }

    /**
     * perisisit
     *
     * @param actionData
     */
    @Override
    public void persist(ActionData actionData) {
        log.info("[UTXOAction.persist] is starting!");
        utxoActionService.process(actionData, TxProcessTypeEnum.PERSIST);
        log.info("[UTXOAction.persist] is success!");
    }

}
