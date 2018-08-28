package com.higgs.trust.slave.core.service.action.utxo;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.action.ActionHandler;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.util.List;

/**
 * UTXO action Handler
 *
 * @author lingchao
 * @create 2018年03月27日16:53
 */
@Slf4j @Component public class UTXOActionHandler implements ActionHandler {

    @Autowired private UTXOActionService utxoActionService;

    @Override public void verifyParams(Action action) throws SlaveException {
        UTXOAction bo = (UTXOAction)action;
        if (StringUtils.isEmpty(bo.getStateClass()) || bo.getStateClass().length() > 255) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if (StringUtils.isEmpty(bo.getContractAddress()) || bo.getContractAddress().length() > 64) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if (bo.getUtxoActionType() == null) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if (CollectionUtils.isEmpty(bo.getInputList())) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        if (CollectionUtils.isEmpty(bo.getOutputList())) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }
        for (TxIn in : bo.getInputList()) {
            if (StringUtils.isEmpty(in.getTxId()) || in.getTxId().length() > 64) {
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }
            if (in.getActionIndex() == null || in.getIndex() == null) {
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }
        }
        for (TxOut out : bo.getOutputList()) {
            if (StringUtils.isEmpty(out.getIdentity()) || out.getIdentity().length() > 64) {
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }
            if (out.getActionIndex() == null || out.getIndex() == null) {
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }
            if (out.getState() == null) {
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }
        }
    }

    /**
     * action process
     *
     * @param actionData
     */
    @Override public void process(ActionData actionData) {
        log.info("[UTXOAction.process] is starting!");
        utxoActionService.process(actionData);
        log.info("[UTXOAction.process] is success!");
    }

}
