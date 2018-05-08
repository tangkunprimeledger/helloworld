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
        def blockService = beans.getBean(BlockService.class)
        context.provide([name: "Name", value: nodeState.nodeName])
        context.provide([name: "Master", value: nodeState.masterName])
        context.provide([name: "isMaster", value: nodeState.master])
        context.provide([name: "State", value: nodeState.state])
        context.provide([name: "Height", value: blockService.getMaxHeight().toString()])
    }

    @Usage('show the current state of node')
    @Command
    def state(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        out.println("Node is $nodeState.state")
    }

    @Usage('show the current height of node')
    @Command
    def height(InvocationContext context,
               @Usage("will show the cluster height")
               @Option(names = ["c", "cluster"]) Boolean isCluster,
               @Usage("set the waiting time in milliseconds for consensus")
               @Option(names = ["t", "waiting"]) Integer time) {

        BeanFactory beans = context.attributes['spring.beanfactory']
        def height
        if (isCluster) {
            def clusterService = beans.getBean(ClusterService.class)
            if (time == null) {
                time = 2000
            }
            height = clusterService.getClusterHeight(1, time)
            if (height == null) {
                out.println("Failed to get cluster height, please try again")
                return
            }
            out.println("The cluster block height is $height")
        } else {
            def blockService = beans.getBean(BlockService.class)
            height = blockService.getMaxHeight().toString()
            out.println("The block height is $height")
        }
    }

    @Usage('check the current block of node')
    @Command
    def selfCheck(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def selfCheckService = beans.getBean(SelfCheckingService.class)
        def result = selfCheckService.selfCheck(1)
        out.println("Self check result: $result")
    }

    @Usage('change the state of node')
    @Command
    def changeState(InvocationContext context, @Usage("from state")
    @Required @Argument NodeStateEnum from, @Usage("to state") @Required @Argument NodeStateEnum to) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        nodeState.changeState(from, to)
        out.println("State changed to $nodeState.state")
    }

    @Usage('change master')
    @Command
    def changeMaster(InvocationContext context,
                     @Usage("the name of new master") @Required @Argument String masterName) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        nodeState.changeMaster(masterName)
        out.println("Master changed to $nodeState.masterName")
    }

}
