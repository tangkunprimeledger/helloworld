package commands

import com.higgs.trust.common.utils.SignUtils
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap
import com.higgs.trust.consensus.p2pvalid.core.storage.SyncSendService
import com.higgs.trust.slave.common.enums.NodeStateEnum
import com.higgs.trust.slave.core.managment.NodeState
import com.higgs.trust.slave.core.repository.PackageRepository
import com.higgs.trust.slave.core.service.block.BlockService
import com.higgs.trust.slave.core.service.consensus.cluster.ClusterService
import com.higgs.trust.slave.core.service.failover.SelfCheckingService
import lombok.extern.slf4j.Slf4j
import org.apache.commons.lang3.time.DateFormatUtils
import org.crsh.cli.*
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

import java.util.concurrent.ConcurrentHashMap

/*
 * Copyright (c) 2013-2017, suimi
 */

@Slf4j
@Usage("about node info")
class node {

    @Usage('show the node info')
    @Command
    def info(InvocationContext context, @Usage("show the info until the end") @Option(names = ["t"]) Boolean isTill) {
        if (isTill) {
            while (true) {
                printInfo(context)
                context.flush()
                Thread.sleep(1000)
            }
        } else {
            printInfo(context)
        }
    }

    def printInfo(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        def blockService = beans.getBean(BlockService.class)
        def packageRepository = beans.getBean(PackageRepository.class)
        context.provide([Name: "Name", Value: nodeState.nodeName])
        context.provide([Name: "Master", Value: nodeState.masterName])
        context.provide([Name: "Term", Value: nodeState.getCurrentTerm()])
        context.provide([Name: "Master Heartbeat", Value: nodeState.getMasterHeartbeat().get()])
        context.provide([Name: "isMaster", Value: nodeState.master])
        context.provide([Name: "State", Value: nodeState.state])
        context.provide([Name: "Block Height", Value: blockService.getMaxHeight().toString()])
        context.provide([Name: "Package Height", Value: packageRepository.getMaxHeight().toString()])
        context.provide([Name: "Time", Value: DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss.SSS")])
        out.println("")
    }

    @Usage('show the current state of node')
    @Command
    def state(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        out.println("Node is $nodeState.state")
    }

    @Usage('show the terms of cluster')
    @Command
    def terms(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        nodeState.getTerms().forEach({ t -> context.provide(Term: t.term, StartHeight: t.startHeight, EndHeight: t.endHeight, MasterName: t.masterName) })
        out.println("")
    }

    @Usage('show the current height of node')
    @Command
    def height(InvocationContext context,
               @Usage("will show the cluster height")
               @Option(names = ["c", "cluster"]) Boolean isCluster, @Option(names = ["a", "all"]) Boolean isAll) {

        BeanFactory beans = context.attributes['spring.beanfactory']
        if (isAll) {
            def clusterService = beans.getBean(ClusterService.class)
            def map = clusterService.getAllClusterHeight()
            map.entrySet().forEach({ entry -> context.provide([name: entry.key, value: entry.value]) })
            return
        }
        def height
        if (isCluster) {
            def clusterService = beans.getBean(ClusterService.class)
            height = clusterService.getClusterHeight(1)
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

    @Usage('end the master term')
    @Command
    def endTerm(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        nodeState.endTerm()
        out.println("ended the master term")
    }

}
