package commands

import com.higgs.trust.config.master.ChangeMasterService
import com.higgs.trust.config.master.INodeInfoService
import com.higgs.trust.config.snapshot.TermManager
import lombok.extern.slf4j.Slf4j
import org.crsh.cli.*
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

/*
 * Copyright (c) 2013-2017, suimi
 */

@Slf4j
@Usage("about term info")
class term {

    @Usage('show the terms of cluster')
    @Command
    def info(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def termManager = beans.getBean(TermManager.class)
        def nodeInfoService = beans.getBean(INodeInfoService.class)
        def changeMasterService = beans.getBean(ChangeMasterService.class)
        context.provide([Name: "Master Heartbeat", Value: changeMasterService.getMasterHeartbeat().get()])
        context.provide([Name: "Master Election", Value: nodeInfoService.isElectionMaster()])
        out.println("")
        termManager.getTerms().forEach({ t -> context.provide(Term: t.term, StartHeight: t.startHeight, EndHeight: t.endHeight, MasterName: t.masterName) })
        out.println("")
    }

    @Usage('end the master term')
    @Command
    def endTerm(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def termManager = beans.getBean(TermManager.class)
        termManager.endTerm()
        out.println("ended the master term")
    }

    @Usage('set election master')
    @Command
    def election(InvocationContext context, @Option(names = ["e", "election"]) Boolean election) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeInfoService = beans.getBean(INodeInfoService.class)
        if (election) {
            nodeInfoService.setElectionMaster(election)
        } else {
            nodeInfoService.setElectionMaster(false)
        }
        context.provide([Name: "Master Election", Value: nodeInfoService.isElectionMaster()])
        out.println("")
    }

    @Usage('start new term')
    @Command
    def startNewTerm(InvocationContext context, @Usage("term")
    @Required @Argument String term, @Usage("term start package height") @Required @Argument String startHeight) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def changeMasterService = beans.getBean(ChangeMasterService.class)
        changeMasterService.artificialChangeMaster(Integer.parseInt(term), Long.parseLong(startHeight))
        out.println("submit artificial change master ")
    }
}
