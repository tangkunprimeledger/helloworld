package com.higgs.trust.slave.core.managment;

import com.higgs.trust.slave.common.config.NodeProperties;
import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.FailoverExecption;
import com.higgs.trust.slave.core.managment.listener.MasterChangeListener;
import com.higgs.trust.slave.core.managment.listener.StateChangeListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component @Scope("singleton") @Slf4j public class NodeState implements InitializingBean {

    @Autowired private NodeProperties properties;

    private List<MasterChangeListener> masterListeners = new ArrayList<>();

    private List<StateChangeListener> stateListeners = new ArrayList<>();

    //todo:suimi change state to Starting
    @Getter private NodeStateEnum state = NodeStateEnum.Running;

    /**
     * 当前节点是否为master
     */
    @Getter private boolean master;

    /**
     * 节点名
     */
    @Getter private String nodeName;

    /**
     * master名
     */
    @Getter private String masterName;

    /**
     * private key
     */
    @Getter private String privateKey;

    /**
     * master public key
     */

    @Getter private String masterPubKey;

    /**
     * prefix of node name
     */
    @Getter private String prefix;

    @Override public void afterPropertiesSet() {
        this.nodeName = properties.getNodeName();
        this.privateKey = properties.getPrivateKey();
        this.masterPubKey = properties.getMasterPubKey();
        this.prefix = properties.getPrefix();
        this.changeMaster(properties.getMasterName());
    }

    /**
     * register state change listener
     *
     * @param listener
     */
    public void registerStateListener(StateChangeListener listener) {
        stateListeners.add(listener);
    }

    /**
     * register master listener
     *
     * @param listener
     */
    public void registerMasterListener(MasterChangeListener listener) {
        masterListeners.add(listener);
    }

    /**
     * node state change from -> to
     *
     * @param from
     * @param to
     */
    public synchronized void changeState(NodeStateEnum from, NodeStateEnum to) {
        Assert.notNull(from, "from state can't be null");
        Assert.notNull(to, "to state can't be null");

        if (from != state || !checkState(from, to)) {
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_STATE_CHANGE_FAILED);
        }
        state = to;
        log.info("Node state changed from:{} to:{}", from, to);
        stateListeners.forEach(listener -> listener.stateChanged(from, to));
    }

    /**
     * 检查状态迁移是否正确
     *
     * @param from
     * @param to
     * @return
     */
    private boolean checkState(NodeStateEnum from, NodeStateEnum to) {
        boolean result = false;
        switch (from) {
            case Starting:
                result = NodeStateEnum.SelfChecking == to;
                break;
            case SelfChecking:
                result =
                    NodeStateEnum.AutoSync == to || NodeStateEnum.ArtificialSync == to || NodeStateEnum.Running == to
                        || NodeStateEnum.Offline == to;
                break;
            case AutoSync:
            case ArtificialSync:
                result = NodeStateEnum.Running == to || NodeStateEnum.SelfChecking == to || NodeStateEnum.Offline == to;
                break;
            case Running:
                result = NodeStateEnum.SelfChecking == to || NodeStateEnum.Offline == to;
                break;
            case Offline:
                result = NodeStateEnum.SelfChecking == to;
                break;

        }
        return result;
    }

    /**
     * node change the master
     *
     * @param masterName
     */
    public synchronized void changeMaster(String masterName) {
        Assert.isTrue(StringUtils.isNotBlank(masterName), "master name can't be null");
        this.masterName = masterName;
        log.info("Node master changed to {}", masterName);
        master = masterName.equalsIgnoreCase(nodeName);
        masterListeners.forEach(listener -> listener.masterChanged(masterName));
    }

    public boolean isMaster() {
        return master;
    }

    /**
     * 是否给定的其中状态
     *
     * @param state
     * @return
     */
    public boolean isState(NodeStateEnum... state) {
        for (int i = 0; i < state.length; i++) {
            if (this.state == state[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * the regex of node name which exclude me
     *
     * @return
     */
    public String notMeNodeNameReg() {
        return "(?!" + this.nodeName.toUpperCase(Locale.ROOT) + ")" + this.prefix.toUpperCase(Locale.ROOT) + "(\\S)*";
    }

}
