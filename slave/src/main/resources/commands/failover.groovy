package commands

import com.higgs.trust.slave.common.enums.NodeStateEnum
import com.higgs.trust.slave.core.managment.NodeState
import com.higgs.trust.slave.core.scheduler.FailoverSchedule
import com.higgs.trust.slave.core.service.failover.SyncService
import lombok.extern.slf4j.Slf4j
import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Required
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

/*
 * Copyright (c) 2013-2017, suimi
 */

@Slf4j
@Usage("get the node info")
class failover {

    @Usage('sync batch blocks, get the blocks from other node and validate block by raft/b2p channel and execute it')
    @Command
    def sync(InvocationContext context, @Required @Argument String startHeight, @Required @Argument int size) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        if (!nodeState.isState(NodeStateEnum.ArtificialSync)) {
            out.println("Node state is $nodeState.state, not allowed sync block")
            return
        }

        def syncService = beans.getBean(SyncService.class)
        return syncService.sync(Long.parseLong(startHeight), size)
    }

    @Usage('failover the block, which will get the block from other node, transfer to package, validating/persisting the package transaction and validate the result with received consensus validating/persisting block header')
    @Command
    def block(InvocationContext context, @Required @Argument String height) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        if (!nodeState.isState(NodeStateEnum.ArtificialSync)) {
            out.println("Node state is $nodeState.state, not allowed sync block")
            return
        }
        def failoverSchedule = beans.getBean(FailoverSchedule.class)
        return failoverSchedule.failover(Long.parseLong(height))
    }

}
