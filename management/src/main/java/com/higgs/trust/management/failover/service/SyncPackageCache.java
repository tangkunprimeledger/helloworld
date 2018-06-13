package com.higgs.trust.management.failover.service;

import com.higgs.trust.config.node.NodeStateEnum;
import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.management.failover.config.FailoverProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component @Slf4j public class SyncPackageCache {

    @Autowired private FailoverProperties properties;

    @Autowired private NodeState nodeState;

    private static final long INIT_HEIGHT = -1L;

    private Long clusterHeight = INIT_HEIGHT;

    /**
     * package最新高度
     */
    @Getter private long latestHeight = INIT_HEIGHT;

    @Getter private long minHeight = INIT_HEIGHT;

    /**
     * 接收package
     * 1.验证package
     * 2.高度是否连续，不连续丢弃之前package
     * 3.更新高度
     * 4.是否超过阈值，超过阈值保留部分
     *
     * @param currentHeight package height
     */
    void receivePackHeight(long currentHeight) {
        if (!nodeState.isState(NodeStateEnum.AutoSync)) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("sync cache received package height:{}", currentHeight);
        }
        synchronized (clusterHeight) {
            if (currentHeight <= clusterHeight) {
                return;
            }
            if (currentHeight == latestHeight + 1) {
                latestHeight = currentHeight;
                if (latestHeight - minHeight >= properties.getThreshold()) {
                    if (properties.getKeepSize() >= properties.getThreshold()) {
                        minHeight = currentHeight;
                    } else {
                        minHeight = currentHeight - properties.getKeepSize();
                    }
                }
            } else if (currentHeight > latestHeight + 1) {
                latestHeight = currentHeight;
                minHeight = currentHeight;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("sync cache  minHeight:{}, latestHeight:{}", minHeight, latestHeight);
        }
    }

    /**
     * 重置缓存package
     */
    void reset(long clusterHeight) {
        synchronized (this.clusterHeight) {
            this.clusterHeight = clusterHeight;
            latestHeight = clusterHeight;
            minHeight = clusterHeight;
        }
    }

}
