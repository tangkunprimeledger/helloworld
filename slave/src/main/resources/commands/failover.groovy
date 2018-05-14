package commands

import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo
import com.higgs.trust.slave.common.enums.NodeStateEnum
import com.higgs.trust.slave.core.managment.NodeState
import com.higgs.trust.slave.core.repository.BlockRepository
import com.higgs.trust.slave.core.scheduler.FailoverSchedule
import com.higgs.trust.slave.core.service.block.BlockService
import com.higgs.trust.slave.core.service.failover.SyncService
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum
import lombok.extern.slf4j.Slf4j
import org.apache.commons.lang3.StringUtils
import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Option
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
            out.println("sync blocks from $fromNode successful, current height:$height")
        } else {
            def clusterInfo = beans.getBean(ClusterInfo.class)
            if (!clusterInfo.clusterNodeNames().contains(fromNode)) {
                out.println("The from node: $fromNode not exist")
                return
            }
            syncService.sync(Long.parseLong(startHeight), size, fromNode)
            def height = blockService.getMaxHeight().toString()
            out.println("sync blocks successful, current height:$height")
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

}
