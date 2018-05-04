package com.higgs.trust.slave.core.service.failover;

import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.managment.listener.StateChangeListener;
import com.higgs.trust.slave.model.bo.Package;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component @Slf4j public class SyncPackageCache implements StateChangeListener, InitializingBean {

    @Autowired private FailoverProperties properties;

    @Autowired private NodeState nodeState;

    public static final long INIT_HEIGHT = -1L;

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
     * @param pack package
     */
    public synchronized void receive(Package pack) {
        if (!nodeState.isState(NodeStateEnum.AutoSync)) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("sync cache received package:{}", pack);
        }
        long currentHeight = pack.getHeight();
        if (latestHeight == INIT_HEIGHT) {
            latestHeight = currentHeight;
            minHeight = currentHeight;
            return;
        }
        if (currentHeight == latestHeight + 1) {
            latestHeight = currentHeight;
            if (latestHeight - minHeight >= properties.getThreshold()) {
                if (properties.getKeepSize() > properties.getThreshold()) {
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

    /**
     * 清空缓存package
     */
    public void clean() {
        latestHeight = INIT_HEIGHT;
        minHeight = INIT_HEIGHT;
    }

    @Override public void stateChanged(NodeStateEnum from, NodeStateEnum to) {
        if (NodeStateEnum.AutoSync == from) {
            this.clean();
        }
    }

    @Override public void afterPropertiesSet() {
        nodeState.registerStateListener(this);
    }
}
