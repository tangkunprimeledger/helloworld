package commands

import com.higgs.trust.slave.core.service.datahandler.manage.SystemPropertyHandler
import lombok.extern.slf4j.Slf4j
import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Required
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

/*
 * lingchao
 */

@Slf4j
@Usage("about system property operation")
class property {

    @Usage('get system property')
    @Command
    def get(InvocationContext context, @Usage("property key")
    @Required @Argument String key) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def systemPropertyHandler = beans.getBean(SystemPropertyHandler.class)
        def msg = systemPropertyHandler.get(key)
        out.println(msg)
    }

    @Usage('add system property')
    @Command
    def add(InvocationContext context, @Usage("property key")
    @Required @Argument String key,
            @Usage("property value") @Required @Argument String value, @Usage("property desc") @Argument String desc) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def systemPropertyHandler = beans.getBean(SystemPropertyHandler.class)
        def msg = systemPropertyHandler.add(key, value, desc)
        out.println(msg)
    }

    @Usage('update system property')
    @Command
    def update(InvocationContext context, @Usage("property key")
    @Required @Argument String key, @Usage("property value") @Required @Argument String value) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def systemPropertyHandler = beans.getBean(SystemPropertyHandler.class)
        def msg = systemPropertyHandler.update(key, value)
        out.println(msg)
    }
}
