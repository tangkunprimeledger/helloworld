package commands

import com.higgs.trust.slave.common.enums.NodeStateEnum
import com.higgs.trust.slave.core.managment.NodeState
import com.higgs.trust.slave.core.service.block.BlockService
import com.higgs.trust.slave.core.service.consensus.cluster.ClusterService
import com.higgs.trust.slave.core.service.failover.SelfCheckingService
import lombok.extern.slf4j.Slf4j
import org.crsh.cli.*
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

/*
 * Copyright (c) 2013-2017, suimi
 */

@Slf4j
@Usage("about node info")
class node {

    @Usage('show the node info')
    @Command
    def info(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        nodeState.nodeName
        out << "node name:  " << magenta << nodeState.nodeName << reset << "\n"
        out << "master name:    " << magenta << nodeState.masterName << reset << "\n"
        out << "is master:    " << magenta << nodeState.master << reset << "\n"
        out << "node state:   " << magenta << nodeState.state << reset << "\n"
        out << "block height:   "
        def blockService = beans.getBean(BlockService.class)
        out.println(blockService.getMaxHeight().toString(), magenta)
    }

    @Usage('show the current state of node')
    @Command
    def state(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        return nodeState.state
    }

    @Usage('show the current height of node')
    @Command
    def height(InvocationContext context, @Usage("will show the cluster height")
    @Required(false) @Option(names = ["c", "cluster"]) boolean isCluster) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        if (isCluster) {
            def clusterService = beans.getBean(ClusterService.class)
            def height = clusterService.getClusterHeight(1, 2000)
            if (height == null) {
                return null
            }
            return height.toString()
        } else {
            def blockService = beans.getBean(BlockService.class)
            return blockService.getMaxHeight().toString()

        }
    }

    @Usage('self check')
    @Command
    def selfCheck(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def selfCheckService = beans.getBean(SelfCheckingService.class)
        return selfCheckService.selfCheck()
    }

    @Usage('change the state of node')
    @Command
    def changeState(InvocationContext context, @Usage("from state")
    @Required @Argument NodeStateEnum from, @Usage("to state") @Required @Argument NodeStateEnum to) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        return nodeState.changeState(from, to)
    }

    @Usage('change master')
    @Command
    def changeMaster(InvocationContext context,
                     @Usage("the name of new master") @Required @Argument String masterName) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        return nodeState.changeMaster(masterName)
    }

}
