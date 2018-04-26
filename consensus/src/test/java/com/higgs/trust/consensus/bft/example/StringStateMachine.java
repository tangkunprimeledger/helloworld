package com.higgs.trust.consensus.bft.example;

import com.higgs.trust.consensus.bft.core.ConsensusCommit;
import com.higgs.trust.consensus.bft.core.template.AbstractConsensusStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringStateMachine extends AbstractConsensusStateMachine {
    private static final Logger log = LoggerFactory.getLogger(StringStateMachine.class);

    /**
     * apply method for your command
     * @return
     */
    public String stringApply(ConsensusCommit<StringCommand> commit) {
        try {
            log.warn("command value is {}", commit.operation().get());
            return commit.operation().get();
        } finally {
            System.out.println("close the commit");
            commit.close();
        }
    }
}
