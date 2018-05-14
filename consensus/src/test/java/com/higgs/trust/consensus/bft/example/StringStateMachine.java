package com.higgs.trust.consensus.bft.example;

import com.higgs.trust.consensus.bft.core.ConsensusCommit;
import com.higgs.trust.consensus.bft.core.template.AbstractConsensusStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StringStateMachine extends AbstractConsensusStateMachine {
    private static final Logger log = LoggerFactory.getLogger(StringStateMachine.class);

    /**
     * apply method for your command
     * @return
     */
    public String stringApply(ConsensusCommit<StringCommand> commit) throws Exception {
        throw new Exception("test exception");
//        try {
//            log.warn("command value is {}", commit.operation().get());
//            return commit.operation().get();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        } finally {
//            System.out.println("close the commit");
//        }
    }
}
