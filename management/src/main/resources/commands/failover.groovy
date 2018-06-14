package commands

import com.higgs.trust.config.node.NodeStateEnum
import com.higgs.trust.config.node.NodeState
import com.higgs.trust.config.p2p.ClusterInfo
import com.higgs.trust.management.failover.scheduler.FailoverSchedule
import com.higgs.trust.management.failover.service.SelfCheckingService
import com.higgs.trust.management.failover.service.SyncService
import com.higgs.trust.slave.core.repository.BlockRepository
import com.higgs.trust.slave.core.service.block.BlockService
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum
import lombok.extern.slf4j.Slf4j
import org.apache.commons.lang3.StringUtils
import org.crsh.cli.*
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

/*
 * Copyright (c) 2013-2017, suimi
 */

@Slf4j
@Usage("get the node info")
class failover {

    @Usage('delete the temp header')
    @Command
    def deleteTempHeader(InvocationContext context,
                         @Required @Argument String height,
                         @Usage("block header type") @Required @Argument BlockHeaderTypeEnum type) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def blockRepository = beans.getBean(BlockRepository.class)
        blockRepository.deleteTempHeader(Long.parseLong(height), type)
        out.println("delete successful")
    }

    @Usage('auto sync the batch blocks, get the blocks from other node and validate block by raft/b2p channel and execute it, this option will auto change the node state.')
    @Command
    def autoSync(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def syncService = beans.getBean(SyncService.class)
        def nodeState = beans.getBean(NodeState.class)
        if (!nodeState.isState(NodeStateEnum.AutoSync)) {
            out.println("Node state is $nodeState.state, not allowed auto sync block")
            return
        }
        def selfCheckService = beans.getBean(SelfCheckingService.class)
        def result = selfCheckService.selfCheck(3)
        if (!result) {
            out.println("self check failed, please check the current block")
            return
        }
        syncService.autoSync()
        def blockService = beans.getBean(BlockService.class)
        def height = blockService.getMaxHeight().toString()
        out.println("auto sync blocks successful, current height:$height")
    }


    @Usage('sync batch blocks, get the blocks from other node and validate block by raft/b2p channel and execute it')
    @Command
    def batch(InvocationContext context,
              @Required @Argument String startHeight,
              @Required @Argument int size, @Option(names = ["f", "from"]) String fromNode) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        def blockService = beans.getBean(BlockService.class)
        def syncService = beans.getBean(SyncService.class)
        if (!nodeState.isState(NodeStateEnum.ArtificialSync)) {
            out.println("Node state is $nodeState.state, not allowed sync block")
            return
        }

        if (StringUtils.isBlank(fromNode)) {
            syncService.sync(Long.parseLong(startHeight), size)
            def height = blockService.getMaxHeight().toString()
            out.println("sync blocks successful, current height:$height")
        } else {
            def clusterInfo = beans.getBean(ClusterInfo.class)
            if (!clusterInfo.clusterNodeNames().contains(fromNode)) {
                out.println("The from node: $fromNode not exist")
                return
            }
            syncService.sync(Long.parseLong(startHeight), size, fromNode)
            def height = blockService.getMaxHeight().toString()
            out.println("sync blocks from $fromNode successful, current height:$height")
        }
    }

    @Usage('failover single block, which will get the block from other node, transfer to package, validating/persisting the package transaction and validate the result with received consensus validating/persisting block header')
    @Command
    def single(InvocationContext context, @Required @Argument String height) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        if (!nodeState.isState(NodeStateEnum.ArtificialSync)) {
            out.println("Node state is $nodeState.state, not allowed sync block")
            return
        }
        def failoverSchedule = beans.getBean(FailoverSchedule.class)
        return failoverSchedule.failover(Long.parseLong(height))
    }

    @Usage('check the current block of node')
    @Command
    def selfCheck(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def selfCheckService = beans.getBean(SelfCheckingService.class)
        def result = selfCheckService.selfCheck(1)
        out.println("Self check result: $result")
    }

}
