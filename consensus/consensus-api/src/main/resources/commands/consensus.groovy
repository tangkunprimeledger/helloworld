package commands

import com.higgs.trust.consensus.core.ConsensusStateMachine
import lombok.extern.slf4j.Slf4j
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

/*
 * Copyright (c) 2013-2017, hanson
 */

@Slf4j
@Usage("operate the consensus cluster")
class term {

    @Usage('join a consensus cluster')
    @Command
    def joinConsensus(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def consensusStateMachine = beans.getBean(ConsensusStateMachine.class)
        consensusStateMachine.joinConsensus()
        out.println("join the current consensus cluster")
    }

    @Usage('leave a consensus cluster')
    @Command
    def leaveConsensus(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def consensusStateMachine = beans.getBean(ConsensusStateMachine.class)
        consensusStateMachine.leaveConsensus()
        out.println("leave the current consensus cluster")
    }

}
