package com.higgs.trust.slave.core.service.consensus.p2p;

<<<<<<< HEAD
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.slave.common.config.PropertiesConfig;
=======
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.*;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.common.enums.NodeStateEnum;
>>>>>>> dev_0610_ca
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.block.BlockService;
<<<<<<< HEAD
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.consensus.PersistCommand;
import com.higgs.trust.slave.model.bo.consensus.ValidateCommand;
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum;
=======
import com.higgs.trust.slave.core.service.consensus.cluster.ClusterService;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.consensus.*;
>>>>>>> dev_0610_ca
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
=======
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

>>>>>>> dev_0610_ca
/**
 * @Description: handle p2p message sending and p2p message receiving
 * @author: pengdi
 **/
<<<<<<< HEAD
@Slf4j @Service public class P2pHandlerImpl implements P2pHandler {

    @Autowired private ValidConsensus validConsensus;

    @Autowired private PropertiesConfig propertiesConfig;

    @Autowired private BlockService blockService;

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
        if (propertiesConfig.isMock()) {
            // store the validated header result
            blockService.storeTempHeader(header, BlockHeaderTypeEnum.CONSENSUS_VALIDATE_TYPE);
        } else {
            validConsensus.submit(validateCommand);
        }
        log.info("end send validating command to p2p consensus layer");
    }
=======
@Slf4j @Service public class P2pHandlerImpl extends ValidConsensus implements P2pHandler, ClusterService {

    @Autowired private BlockService blockService;

    @Autowired private NodeState nodeState;

    private static final String DEFAULT_CLUSTER_HEIGHT_ID = "cluster_height_id";

    @Autowired private BlockRepository blockRepository;

    @Autowired private P2pConsensusClient p2pConsensusClient;

    @Autowired private ClusterInfo clusterInfo;

    @Autowired private PackageService packageService;
>>>>>>> dev_0610_ca

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
<<<<<<< HEAD
        if (propertiesConfig.isMock()) {
            // store the persist header result
            blockService.storeTempHeader(header, BlockHeaderTypeEnum.CONSENSUS_PERSIST_TYPE);
        } else {
            validConsensus.submit(persistCommand);
        }
        log.info("end send persisting command to p2p consensus layer");
    }

=======
        this.submit(persistCommand);
        log.info("end send persisting command to p2p consensus layer");
    }

    /**
     * majority of this cluster has persisted this command
     *
     * @param commit
     */
    public void receivePersisted(ValidCommit<PersistCommand> commit) {
        // validate param
        BeanValidateResult result = BeanValidator.validate(commit);
        if (!result.isSuccess()) {
            log.error("[P2pReceiver.validated]param persist failed, cause: " + result.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // get validated block p2p and package
        BlockHeader header = commit.operation().get();
        log.info("the persisted p2p result is {}", header);

        doReceive(commit, header);
    }

    /**
     * async start package process, maybe the right package is waiting for consensus
     *
     * @param commit
     * @param header
     */
    private void doReceive(ValidCommit commit, BlockHeader header) {

        if (!nodeState.isState(NodeStateEnum.Running)) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_FAILOVER_STATE_NOT_ALLOWED);
        }

        packageService.persisted(header);

        commit.close();
    }

    /**
     * handle the consensus result of cluster height
     *
     * @param commit
     */
    public List<ValidClusterHeightCmd> handleClusterHeight(ValidSyncCommit<ClusterHeightCmd> commit) {
        ClusterHeightCmd operation = commit.operation();
        List<Long> maxHeights = blockRepository.getMaxHeight(operation.get());
        List<ValidClusterHeightCmd> cmds = new ArrayList<>();
        maxHeights.forEach(height -> cmds.add(new ValidClusterHeightCmd(operation.getRequestId(), height)));
        return cmds;
    }

    /**
     * handle the consensus result of validating block header
     *
     * @param commit
     */
    public ValidBlockHeaderCmd handleValidHeader(ValidSyncCommit<BlockHeaderCmd> commit) {
        BlockHeaderCmd operation = commit.operation();
        BlockHeader header = operation.get();
        BlockHeader blockHeader = blockRepository.getBlockHeader(header.getHeight());
        boolean result = blockHeader != null && blockService.compareBlockHeader(header, blockHeader);
        return new ValidBlockHeaderCmd(operation.getRequestId(), header, result);
    }

    /**
     * get the cluster height through consensus, the default request id will be set. if timeout, null will be return
     *
     * @param size the size of height will be consensus
     * @return
     */
    @Override public Long getClusterHeight(int size) {
        return getClusterHeight(DEFAULT_CLUSTER_HEIGHT_ID + Constant.SPLIT_SLASH + System.currentTimeMillis(), size);
    }

    /**
     * get the cluster height through consensus, if timeout, null will be return
     *
     * @param requestId the id of request
     * @param size      the size of height will be consensus
     * @return
     */
    @Override public Long getClusterHeight(String requestId, int size) {
        ResponseCommand<?> responseCommand = this.submitSync(new ClusterHeightCmd(requestId, size));
        return responseCommand == null ? null : (Long)responseCommand.get();
    }

    @Override public Map<String, Long> getAllClusterHeight() {
        List<String> nodeNames = clusterInfo.clusterNodeNames();
        Map<String, Long> heightMap = new HashMap<>();
        String requestId = DEFAULT_CLUSTER_HEIGHT_ID + Constant.SPLIT_SLASH + System.currentTimeMillis();
        ClusterHeightCmd cmd = new ClusterHeightCmd(requestId, 1);
        ValidCommandWrap validCommandWrap = new ValidCommandWrap();
        validCommandWrap.setCommandClass(cmd.getClass());
        validCommandWrap.setFromNode(clusterInfo.myNodeName());
        validCommandWrap.setSign(SignUtils.sign(cmd.getMessageDigestHash(), clusterInfo.privateKey()));
        validCommandWrap.setValidCommand(cmd);
        nodeNames.forEach((nodeName) -> {
            Long height = null;
            try {
                ValidResponseWrap<? extends ResponseCommand> validResponseWrap =
                    p2pConsensusClient.syncSend(nodeName, validCommandWrap);
                Object response = validResponseWrap.getResult();
                if (response != null) {
                    if (response instanceof List) {
                        List<ResponseCommand> commands = (List)response;
                        if (!commands.isEmpty() && commands.get(0).get() != null) {
                            height = (Long)commands.get(0).get();
                        }
                    }
                }

            } catch (Throwable throwable) {
                log.error("{}", throwable);
            }
            heightMap.put(nodeName, height);
        });
        return heightMap;
    }

    /**
     * validating the block header through consensus, if timeout, null will be return
     *
     * @param header block header
     * @return
     */
    @Override public Boolean validatingHeader(BlockHeader header) {
        BlockHeaderCmd command = new BlockHeaderCmd(header);
        ResponseCommand<?> responseCommand = this.submitSync(command);
        return responseCommand == null ? null : (Boolean)responseCommand.get();
    }

>>>>>>> dev_0610_ca
}
