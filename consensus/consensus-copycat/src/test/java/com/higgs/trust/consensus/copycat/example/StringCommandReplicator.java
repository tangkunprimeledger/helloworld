package com.higgs.trust.consensus.copycat.example;

import com.higgs.trust.consensus.annotation.Replicator;
import com.higgs.trust.consensus.core.ConsensusCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component @Replicator public class StringCommandReplicator {
    private static final Logger log = LoggerFactory.getLogger(StringCommandReplicator.class);

    /**
     * apply method for your command
     *
     * @return
     */
    public String stringApply(ConsensusCommit<StringCommand> commit) throws Exception {
        throw new Exception("test exception");
    }
}
