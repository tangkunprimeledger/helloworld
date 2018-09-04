/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.snapshot;

import com.higgs.trust.config.exception.ConfigError;
import com.higgs.trust.config.exception.ConfigException;
import com.higgs.trust.consensus.config.NodeState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author suimi
 * @date 2018/6/12
 */
@Slf4j @Component public class TermManager {



    @Getter private List<TermInfo> terms = new CopyOnWriteArrayList<>();

    @Autowired private NodeState nodeState;

    @Autowired private TermProperties properties;

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
        log.info("start new term:{}", newTerm);
        terms.add(newTerm);
        nodeState.changeMaster(masterName);
        partiallyTermsClean();
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
        log.info("start new term:{}", newTerm);
        terms.add(newTerm);
        nodeState.changeMaster(masterName);
        partiallyTermsClean();
    }

    /**
     * clean term list
     */
    private synchronized void partiallyTermsClean(){
        int maxSize = properties.getMaxTermsSize();
        if(terms.size()>= maxSize){
            log.info("term list size reach max size:{}, do clean",maxSize);
            terms=terms.stream().sorted(Comparator.comparingLong(TermInfo::getTerm)).collect(Collectors.toList());
            terms.removeAll(terms.subList(0,maxSize/2));
        }
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

    public void resetEndHeight(Long[] packageHeights) {
        Optional<TermInfo> optional = getTermInfo(nodeState.getCurrentTerm());
        TermInfo termInfo = optional.get();
        int len = packageHeights.length;
        boolean verify =
            termInfo.getEndHeight() == TermInfo.INIT_END_HEIGHT ? packageHeights[0] == termInfo.getStartHeight() :
                packageHeights[0] >= termInfo.getStartHeight() && packageHeights[len-1] <= termInfo.getEndHeight() + len;
        if (!verify) {
            throw new ConfigException(ConfigError.CONFIG_NODE_MASTER_TERM_PACKAGE_HEIGHT_INCORRECT);
        }
        if ((packageHeights[0] == termInfo.getStartHeight() && termInfo.getEndHeight() == TermInfo.INIT_END_HEIGHT)
            || packageHeights[len-1] == termInfo.getEndHeight() + len) {
            log.debug("reset term end height:{}", packageHeights[len-1]);
            termInfo.setEndHeight(packageHeights[len-1]);
        } else {
            log.warn("set incorrect end height:{}, termInfo:{}", packageHeights[len-1], termInfo);
        }
    }

    public void endTerm() {
        if (nodeState.isMaster()) {
            nodeState.changeMaster(nodeState.MASTER_NA);
        } else {
            throw new ConfigException(ConfigError.CONFIG_NODE_MASTER_NODE_INCORRECT);
        }
    }
}
