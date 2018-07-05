package com.higgs.trust.consensus.config;

import com.higgs.trust.consensus.config.listener.MasterChangeListener;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.consensus.config.listener.StateChangeListenerAdaptor;
import com.higgs.trust.consensus.exception.ConsensusError;
import com.higgs.trust.consensus.exception.ConsensusException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.higgs.trust.consensus.config.NodeStateEnum.*;

@Component @Scope("singleton") @Slf4j public class NodeState implements InitializingBean {

    public static final String MASTER_NA = "N/A";

    @Autowired private NodeProperties properties;

    @Autowired private ApplicationContext applicationContext;

    private Set<MasterChangeListener> masterListeners = new LinkedHashSet<>();

    private Map<NodeStateEnum, LinkedHashSet<StateChangeListenerAdaptor>> stateListeners = new ConcurrentHashMap<>();

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
    @Getter @Setter private String privateKey;

    /**
     * cluster name, as the prefix of cluster nodes
     */
    @Getter private String clusterName;

    @Getter @Setter private long currentTerm = 0;

    @Override public void afterPropertiesSet() {
        this.nodeName = properties.getNodeName();
        this.privateKey = properties.getPrivateKey();
        this.clusterName = properties.getPrefix();

        registerStateListener();

        List<MasterChangeListener> masterChangeListeners = new ArrayList<>();
        masterChangeListeners.addAll(applicationContext.getBeansOfType(MasterChangeListener.class).values());
        AnnotationAwareOrderComparator.sort(masterChangeListeners);
        masterListeners.addAll(masterChangeListeners);
    }

    /**
     * register state change listener
     */
    private void registerStateListener() {
        Map<NodeStateEnum, List<StateChangeListenerAdaptor>> stateListeners = new ConcurrentHashMap<>();
        Arrays.stream(applicationContext.getBeanDefinitionNames()).forEach(beanName -> {
            Object bean = applicationContext.getBean(beanName);
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
            Map<Method, StateChangeListener> methods = MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<StateChangeListener>)method -> AnnotatedElementUtils
                    .findMergedAnnotation(method, StateChangeListener.class));
            if (methods == null || methods.isEmpty()) {
                return;
            }
            methods.forEach((method, value) -> {
                NodeStateEnum[] stateEnums = value.value();
                Arrays.stream(stateEnums).forEach(state -> {
                    List<StateChangeListenerAdaptor> stateChangeListenerAdaptors =
                        stateListeners.computeIfAbsent(state, e -> new ArrayList<>());
                    stateChangeListenerAdaptors.add(new StateChangeListenerAdaptor(bean, method));
                });
            });
        });
        stateListeners.forEach((state, listeners) -> {
            AnnotationAwareOrderComparator.sort(listeners);
            this.stateListeners.put(state, new LinkedHashSet<>(listeners));
        });
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
        try {
            if (from != state || !checkState(from, to)) {
                throw new ConsensusException(ConsensusError.CONFIG_NODE_STATE_CHANGE_FAILED);
            }
            LinkedHashSet<StateChangeListenerAdaptor> stateChangeListenerAdaptors = stateListeners.get(to);
            if (stateChangeListenerAdaptors != null) {
                stateChangeListenerAdaptors.stream().filter(StateChangeListenerAdaptor::isBefore)
                    .forEach(StateChangeListenerAdaptor::invoke);
            }
            state = to;
            log.info("Node state changed from:{} to:{}", from, to);
            if (stateChangeListenerAdaptors != null) {
                stateChangeListenerAdaptors.stream().filter(adaptor -> !adaptor.isBefore())
                    .forEach(StateChangeListenerAdaptor::invoke);
            }
        } catch (Exception e) {
            log.error("change state error", e);
            throw e;
        }
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
        masterListeners.forEach(listener -> listener.beforeChange(masterName));
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
        return "(?!" + this.nodeName.toUpperCase(Locale.ROOT) + ")" + this.clusterName.toUpperCase(Locale.ROOT)
            + "(\\S)*";
    }
}
