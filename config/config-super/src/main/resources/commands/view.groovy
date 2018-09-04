package commands


import com.higgs.trust.config.view.ClusterViewManager
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
@Usage("about cluster view info")
class view {

    @Usage('show the terms of cluster')
    @Command
    def info(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def viewManager = beans.getBean(ClusterViewManager.class)
        viewManager.getViews().forEach({ v -> context.provide(View: v.id, FaultNum: v.faultNum, StartHeight: v.startHeight, EndHeight: v.endHeight) })
        out.println("")
    }

    @Usage('load the view from db')
    @Command
    def loadViews(InvocationContext context, @Usage("view id")
    @Required @Argument String view, @Usage("view start package height") @Required @Argument String startHeight) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def viewManager = beans.getBean(ClusterViewManager.class)
        viewManager.initViews(view, startHeight)
        info(context)
    }
}
