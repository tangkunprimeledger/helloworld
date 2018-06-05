package com.higgs.trust.consensus.core;

/**
 * @author hanson
 * @Date 2018/5/28
 * @Description:
 */
public interface ConsensusStateMachine {

    /**
     * quit from consensus cluster
     */
    void  leaveConsensus();

    /**
     * join a consensus cluster
     */
    void joinConsensus();


    /**
     * start a node when there is no cluster
     */
    void initStart();

}
