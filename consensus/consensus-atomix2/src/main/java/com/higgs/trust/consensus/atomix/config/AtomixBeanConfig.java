/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.consensus.atomix.config;

import com.higgs.trust.consensus.atomix.core.AtomixCommitReplicateComposite;
import com.higgs.trust.consensus.atomix.core.primitive.CommandPrimitiveSubmitOperation;
import com.higgs.trust.consensus.atomix.core.primitive.CommandPrimitiveType;
import com.higgs.trust.consensus.core.AbstractCommitReplicateComposite;
import com.higgs.trust.consensus.core.ConsensusSnapshot;
import com.higgs.trust.consensus.core.DefaultConsensusSnapshot;
import io.atomix.primitive.operation.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author suimi
 * @date 2018/7/5
 */
@Configuration @Slf4j public class AtomixBeanConfig {

    @Bean public AbstractCommitReplicateComposite replicateComposite() {
        return new AtomixCommitReplicateComposite();
    }

    @Bean @ConditionalOnMissingBean(ConsensusSnapshot.class) public ConsensusSnapshot snapshot() {
        return new DefaultConsensusSnapshot();
    }

    @Bean public CommandPrimitiveType commandPrimitiveType(AbstractCommitReplicateComposite replicateComposite,
                                                           ConsensusSnapshot snapshot, CommandPrimitiveSubmitOperation commandPrimitiveSubmitOperation) {
        return new CommandPrimitiveType(replicateComposite, snapshot, commandPrimitiveSubmitOperation);
    }

    @Bean public CommandPrimitiveSubmitOperation commandPrimitiveOperations(AbstractCommitReplicateComposite replicateComposite){
        return new CommandPrimitiveSubmitOperation(OperationType.COMMAND,replicateComposite);
    }
}
