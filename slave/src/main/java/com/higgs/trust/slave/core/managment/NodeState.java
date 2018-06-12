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

    @Getter private NodeStateEnum state = NodeStateEnum.Starting;

    /**
     * has the master heartbeat
     */
    @Getter private AtomicBoolean masterHeartbeat = new AtomicBoolean(false);

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
     * master public key
     */

    @Getter private String masterPubKey;

    /**
     * prefix of node name
     */
    @Getter private String prefix;

    @Getter private long currentTerm = 0;

    @Getter private List<TermInfo> terms = new ArrayList<>();

    public static final String MASTER_NA = "N/A";

    @Override public void afterPropertiesSet() {
        this.nodeName = properties.getNodeName();
        this.privateKey = properties.getPrivateKey();
        this.masterPubKey = properties.getMasterPubKey();
        this.prefix = properties.getPrefix();
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

    /**
     * reset the terms of node, it's called by consensus level, will change the master name and current term
     *
     * @param infos
     */
    synchronized void resetTerms(List<TermInfo> infos) {
        this.terms = infos;
        if (terms == null || terms.isEmpty()) {
            currentTerm = 0;
            changeMaster(MASTER_NA);
        } else {
            TermInfo termInfo = terms.get(infos.size() - 1);
            currentTerm = termInfo.getTerm();
            if (!nodeName.equalsIgnoreCase(termInfo.getMasterName())) {
                changeMaster(termInfo.getMasterName());
            } else {
                if (masterName.equalsIgnoreCase(nodeName)) {
                    changeMaster(termInfo.getMasterName());
                } else {
                    changeMaster(MASTER_NA);
                }
            }
        }
    }

    /**
     * get the terminfo
     *
     * @param term
     * @return
     */
    private Optional<TermInfo> getTermInfo(long term) {
        return terms.stream().filter(termInfo -> term == termInfo.getTerm()).findFirst();
    }

    /**
     * start new term
     *
     * @param term
     * @param masterName
     */
    public void startNewTerm(long term, String masterName) {
        if (term != currentTerm + 1) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_MASTER_TERM_INCORRECT);
        }
        currentTerm = term;
        Optional<TermInfo> optional = getTermInfo(term - 1);
        long startHeight = 2;
        if (optional.isPresent()) {
            TermInfo termInfo = optional.get();
            long endHeight = termInfo.getEndHeight();
            startHeight = endHeight == TermInfo.INIT_END_HEIGHT ? termInfo.getStartHeight() : endHeight + 1;
        }
        TermInfo newTerm = TermInfo.builder().term(term).masterName(masterName).startHeight(startHeight)
            .endHeight(TermInfo.INIT_END_HEIGHT).build();
        terms.add(newTerm);
        changeMaster(masterName);
    }

    /**
     * check if the package height belong the term
     *
     * @param term
     * @param masterName
     * @param packageHeight
     * @return
     */
    public boolean isTermHeight(long term, String masterName, long packageHeight) {
        Optional<TermInfo> optional = getTermInfo(term);
        if (!optional.isPresent()) {
            return false;
        }
        TermInfo termInfo = optional.get();
        if (!termInfo.getMasterName().equalsIgnoreCase(masterName)) {
            return false;
        }
        if (term == currentTerm) {
            return termInfo.getEndHeight() == TermInfo.INIT_END_HEIGHT ? packageHeight == termInfo.getStartHeight() :
                packageHeight == termInfo.getEndHeight() + 1;
        } else {
            return termInfo.getStartHeight() <= packageHeight && termInfo.getEndHeight() >= packageHeight;
        }
    }

    public void resetEndHeight(long packageHeight) {
        Optional<TermInfo> optional = getTermInfo(currentTerm);
        TermInfo termInfo = optional.get();
        boolean verify =
            termInfo.getEndHeight() == TermInfo.INIT_END_HEIGHT ? packageHeight == termInfo.getStartHeight() :
                packageHeight == termInfo.getEndHeight() + 1;
        if (verify) {
            termInfo.setEndHeight(packageHeight);
        } else {
            throw new SlaveException(SlaveErrorEnum.SLAVE_MASTER_TERM_PACKAGE_HEIGHT_INCORRECT);
        }
    }

    public void endTerm() {
        if (isMaster()) {
            changeMaster(MASTER_NA);
        } else {
            throw new SlaveException(SlaveErrorEnum.SLAVE_MASTER_NODE_INCORRECT);
        }
    }

    public void setMasterHeartbeat(boolean hasHeartbeat) {
        masterHeartbeat.getAndSet(hasHeartbeat);
    }

}
