package com.higgs.trust.config.node;

import com.higgs.trust.config.exception.ConfigError;
import com.higgs.trust.config.exception.ConfigException;
import com.higgs.trust.config.node.listener.MasterChangeListener;
import com.higgs.trust.config.node.listener.StateChangeListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;

import static com.higgs.trust.config.node.NodeStateEnum.*;

@Component @Scope("singleton") @Slf4j public class NodeState implements InitializingBean {

    public static final String MASTER_NA = "N/A";

    @Autowired private NodeProperties properties;

    @Autowired private ApplicationContext applicationContext;

    private Set<MasterChangeListener> masterListeners = new HashSet<>();

    private Set<StateChangeListener> stateListeners = new HashSet<>();

    @Getter private NodeStateEnum state = Starting;

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
    @Getter private String masterName = MASTER_NA;

    /**
     * private key
     */
    @Getter private String privateKey;

    /**
     * prefix of node name
     */
    @Getter private String prefix;

    @Getter @Setter private long currentTerm = 0;

    @Override public void afterPropertiesSet() {
        this.nodeName = properties.getNodeName();
        this.privateKey = properties.getPrivateKey();
        this.prefix = properties.getPrefix();
        applicationContext.getBeansOfType(StateChangeListener.class).values().forEach(this::registerStateListener);
        applicationContext.getBeansOfType(MasterChangeListener.class).values().forEach(this::registerMasterListener);
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
            throw new ConfigException(ConfigError.CONFIG_NODE_STATE_CHANGE_FAILED);
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
                result = SelfChecking == to;
                break;
            case SelfChecking:
                result = AutoSync == to || ArtificialSync == to || Running == to || Offline == to;
                break;
            case AutoSync:
            case ArtificialSync:
                result = Running == to || SelfChecking == to || Offline == to;
                break;
            case Running:
                result = SelfChecking == to || Offline == to;
                break;
            case Offline:
                result = SelfChecking == to;
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
