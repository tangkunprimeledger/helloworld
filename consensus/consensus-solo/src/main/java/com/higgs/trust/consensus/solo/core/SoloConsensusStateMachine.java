package com.higgs.trust.consensus.solo.core;

import com.higgs.trust.consensus.core.ConsensusStateMachine;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2019/2/20
 */
@Component public class SoloConsensusStateMachine implements ConsensusStateMachine {
    @Override public void start() {
        throw new UnsupportedOperationException();
    }

    @Override public void leaveConsensus() {
        throw new UnsupportedOperationException();
    }

    @Override public void joinConsensus() {
        throw new UnsupportedOperationException();
    }
}
