package com.higgs.trust.slave.core.service.consensus.p2p;

import com.higgs.trust.config.node.NodeProperties;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.consensus.PersistCommand;
import com.higgs.trust.slave.model.bo.consensus.ValidateCommand;
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: handle p2p message sending and p2p message receiving
 * @author: pengdi
 **/
@Slf4j @Service public class P2pHandlerImpl implements P2pHandler {

    @Autowired private ValidConsensus validConsensus;

    @Autowired private BlockService blockService;

    @Autowired private NodeProperties properties;

    /**
     * send validating result to p2p consensus layer
     *
     * @param header
     */
    @Override public void sendValidating(BlockHeader header) {
        // validate param
        if (null == header) {
            log.error("[P2pReceiver.sendValidating]param validate failed, cause block header is null ");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        BeanValidateResult result = BeanValidator.validate(header);
        if (!result.isSuccess()) {
            log.error("[P2pReceiver.sendValidating]param validate failed, cause: " + result.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // send header to p2p consensus
        ValidateCommand validateCommand = new ValidateCommand(header.getHeight(), header);
        log.info("start send validating command to p2p consensus layer, validateCommand : {}", validateCommand);
        if (properties.isMock()) {
            // store the validated header result
            blockService.storeTempHeader(header, BlockHeaderTypeEnum.CONSENSUS_VALIDATE_TYPE);
        } else {
            validConsensus.submit(validateCommand);
        }
        log.info("end send validating command to p2p consensus layer");
    }

    /**
     * send validating result to p2p consensus layer
     *
     * @param header
     */
    @Override public void sendPersisting(BlockHeader header) {
        // validate param
        if (null == header) {
            log.error("[P2pReceiver.sendPersisting]param validate failed, cause block header is null ");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        BeanValidateResult result = BeanValidator.validate(header);
        if (!result.isSuccess()) {
            log.error("[P2pReceiver.sendPersisting]param validate failed, cause: {}", result.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // send header to p2p consensus
        PersistCommand persistCommand = new PersistCommand(header.getHeight(), header);
        log.info("start send persisting command to p2p consensus layer, persistCommand : {}", persistCommand);
        if (properties.isMock()) {
            // store the persist header result
            blockService.storeTempHeader(header, BlockHeaderTypeEnum.CONSENSUS_PERSIST_TYPE);
        } else {
            validConsensus.submit(persistCommand);
        }
        log.info("end send persisting command to p2p consensus layer");
    }

}
