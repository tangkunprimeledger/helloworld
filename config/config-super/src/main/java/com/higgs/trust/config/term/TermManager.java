/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.term;

import com.higgs.trust.config.exception.ConfigError;
import com.higgs.trust.config.exception.ConfigException;
import com.higgs.trust.consensus.config.NodeState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author suimi
 * @date 2018/6/12
 */
@Slf4j @Component public class TermManager {

    /**
     * has the master heartbeat
     */
    @Getter private AtomicBoolean masterHeartbeat = new AtomicBoolean(false);

    @Getter private List<TermInfo> terms = new ArrayList<>();

    @Autowired private NodeState nodeState;

    /**
     * reset the terms of node, it's called by consensus level, will change the master name and current term
     *
     * @param infos
     */
    synchronized void resetTerms(List<TermInfo> infos) {
        this.terms = infos;
        if (terms == null || terms.isEmpty()) {
            nodeState.setCurrentTerm(0);
            nodeState.changeMaster(NodeState.MASTER_NA);
        } else {
            String nodeName = nodeState.getNodeName();
            TermInfo termInfo = terms.get(infos.size() - 1);
            String masterName = termInfo.getMasterName();
            nodeState.setCurrentTerm(termInfo.getTerm());
            if (!nodeName.equalsIgnoreCase(masterName)) {
                nodeState.changeMaster(masterName);
            } else {
                //if the node is old master
                if (nodeState.getMasterName().equalsIgnoreCase(nodeName)) {
                    nodeState.changeMaster(masterName);
                } else {
                    nodeState.changeMaster(NodeState.MASTER_NA);
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
    public Optional<TermInfo> getTermInfo(long term) {
        return terms.stream().filter(termInfo -> term == termInfo.getTerm()).findFirst();
    }

    /**
     * start new term
     *
     * @param term
     * @param masterName
     */
    public void startNewTerm(long term, String masterName) {
        if (term != nodeState.getCurrentTerm() + 1) {
            throw new ConfigException(ConfigError.CONFIG_NODE_MASTER_TERM_INCORRECT);
        }
        nodeState.setCurrentTerm(term);
        Optional<TermInfo> optional = getTermInfo(term - 1);
        long startHeight = 2;
        if (optional.isPresent()) {
            TermInfo termInfo = optional.get();
            long endHeight = termInfo.getEndHeight();
            startHeight = endHeight == TermInfo.INIT_END_HEIGHT ? termInfo.getStartHeight() : endHeight + 1;
        }
        TermInfo newTerm = TermInfo.builder().term(term).masterName(masterName).startHeight(startHeight)
            .endHeight(TermInfo.INIT_END_HEIGHT).build();
        log.debug("start new term:{}", newTerm);
        terms.add(newTerm);
        nodeState.changeMaster(masterName);
    }

    /**
     * start new term
     *
     * @param term
     * @param masterName
     */
    public void startNewTerm(long term, String masterName, long startHeight) {
        nodeState.setCurrentTerm(term);
        TermInfo newTerm = TermInfo.builder().term(term).masterName(masterName).startHeight(startHeight)
            .endHeight(TermInfo.INIT_END_HEIGHT).build();
        log.debug("start new term:{}", newTerm);
        terms.add(newTerm);
        nodeState.changeMaster(masterName);
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
        if (term == nodeState.getCurrentTerm()) {
            return termInfo.getEndHeight() == TermInfo.INIT_END_HEIGHT ? packageHeight == termInfo.getStartHeight() :
                packageHeight >= termInfo.getStartHeight() && packageHeight <= termInfo.getEndHeight() + 1;
        } else {
            return termInfo.getStartHeight() <= packageHeight && termInfo.getEndHeight() >= packageHeight;
        }
    }

    public void resetEndHeight(long packageHeight) {
        Optional<TermInfo> optional = getTermInfo(nodeState.getCurrentTerm());
        TermInfo termInfo = optional.get();
        boolean verify =
            termInfo.getEndHeight() == TermInfo.INIT_END_HEIGHT ? packageHeight == termInfo.getStartHeight() :
                packageHeight >= termInfo.getStartHeight() && packageHeight <= termInfo.getEndHeight() + 1;
        if (!verify) {
            throw new ConfigException(ConfigError.CONFIG_NODE_MASTER_TERM_PACKAGE_HEIGHT_INCORRECT);
        }
        if ((packageHeight == termInfo.getStartHeight() && termInfo.getEndHeight() == TermInfo.INIT_END_HEIGHT)
            || packageHeight == termInfo.getEndHeight() + 1) {
            log.debug("reset term end height:{}", packageHeight);
            termInfo.setEndHeight(packageHeight);
        } else {
            log.warn("set incorrect end height:{}, termInfo:{}", packageHeight, termInfo);
        }
    }

    public void endTerm() {
        if (nodeState.isMaster()) {
            nodeState.changeMaster(nodeState.MASTER_NA);
        } else {
            throw new ConfigException(ConfigError.CONFIG_NODE_MASTER_NODE_INCORRECT);
        }
    }

    public void setMasterHeartbeat(boolean hasHeartbeat) {
        masterHeartbeat.getAndSet(hasHeartbeat);
    }
}
