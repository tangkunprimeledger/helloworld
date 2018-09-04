package com.higgs.trust.slave.core.service.consensus.log;

import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.consensus.config.NodeProperties;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.pack.PackageProcess;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.consensus.BatchPackageCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Description: replicate the sorted package to cluster
 * @author: pengdi
 **/

@Slf4j @Service public class LogReplicateHandlerImpl implements LogReplicateHandler {
    /**
     * client from the log replicate consensus layer
     */
    @Autowired ConsensusClient consensusClient;

    @Autowired PackageService packageService;

    @Autowired ExecutorService packageThreadPool;

    @Autowired PackageProcess packageProcess;

    @Autowired NodeState nodeState;

    @Autowired NodeProperties properties;

    @Autowired IClusterViewManager viewManager;

    /**
     * retry time interval
     */
    private static final String[] retryInterval =
        new String[] {"50", "50", "50", "100", "100", "200", "400", "800", "1000"};

    /**
     * replicate sorted package to the cluster
     *
     * @param packageVOList
     */
    @Override public void replicatePackage(List<PackageVO> packageVOList) {
        // validate param
        if (CollectionUtils.isEmpty(packageVOList)) {
            log.error("[LogReplicateHandler.replicatePackage]param validate failed, cause package is null ");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        Long startHeight = packageVOList.get(0).getHeight();
        int size = packageVOList.size();
        Long endHeight = packageVOList.get(size - 1).getHeight();

        // replicate package to all nodes
        log.info(
            "package starts to distribute to each node through consensus layer package startHeight={}, endHeight={}, size={}",
            startHeight, endHeight, size);
        BatchPackageCommand packageCommand =
            new BatchPackageCommand(nodeState.getCurrentTerm(), viewManager.getCurrentView().getId(),
                nodeState.getMasterName(), packageVOList);
        String signValue = packageCommand.getSignValue();
        packageCommand.setSign(CryptoUtil.getProtocolCrypto().sign(signValue, nodeState.getConsensusPrivateKey()));

        boolean flag = false;

        /**
         * retry times
         */
        int retryTimes = 0;

        while (!flag) {
            if (!nodeState.isMaster()) {
                return;
            }

            CompletableFuture future = consensusClient.submit(packageCommand);
            try {
                future.get(properties.getConsensusWaitTime(), TimeUnit.MILLISECONDS);
                flag = true;
            } catch (Throwable e) {
                log.error("replicate log failed! startHeight={}, endHeight={}, size={}", startHeight, endHeight, size,
                    e);
                //TODO 添加告警

                // wait for a while
                retryTimes++;
                int retryInterval = getRetryInterval(retryTimes);
                try {
                    Thread.sleep(retryInterval);
                } catch (Throwable e1) {
                    log.error("submit consensus sleep failed", e1);
                }
            }
        }

        log.info("package has been sent to consensus layer package startHeight={}, endHeight={}, size={}", startHeight,
            endHeight, size);
    }

    /**
     * get retry interval by retryTimes
     */
    private static int getRetryInterval(int retryTimes) {
        if (retryTimes >= retryInterval.length) {
            return Integer.parseInt(retryInterval[retryInterval.length - 1]);
        } else {
            return Integer.parseInt(retryInterval[retryTimes]);
        }
    }

}
