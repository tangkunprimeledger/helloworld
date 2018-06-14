package commands

import com.higgs.trust.config.term.TermManager
import lombok.extern.slf4j.Slf4j
import org.crsh.cli.Command
import org.crsh.cli.Usage
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
        context.provide([Name: "Master Heartbeat", Value: termManager.getMasterHeartbeat().get()])
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

}
