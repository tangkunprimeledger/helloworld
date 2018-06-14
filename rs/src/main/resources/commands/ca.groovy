package commands

import com.higgs.trust.rs.core.api.CaService
import com.higgs.trust.slave.core.service.ca.CaInitService
import com.higgs.trust.slave.model.bo.ca.Ca
import lombok.extern.slf4j.Slf4j
import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Required
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

/**
 * @desc ca information
 * @author WangQuanzhou
 * @date 2018/5/29 16:54    
 */

@Slf4j
@Usage("ca information")
class ca {

    @Usage('query CA')
    @Command
    def acquireCA(InvocationContext context,
                  @Usage("user") @Required @Argument String user) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def caService = beans.getBean(CaService.class)
        def ca = caService.getCa(user);
        out.println("acquire CA successful, user= $ca.user, pubKey= $ca.pubKey")
    }


    @Usage('auth CA')
    @Command
    def authCA(InvocationContext context,
               @Usage("user") @Required @Argument String user) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def caService = beans.getBean(CaService.class)
        caService.authKeyPair(user)
        out.println("send CA auth tx successful, user= $user")
    }

    @Usage('update CA')
    @Command
    def updateCA(InvocationContext context,
                 @Usage("user") @Required @Argument String user) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def caService = beans.getBean(CaService.class)
        caService.updateKeyPair(user)
        out.println("send CA update tx successful, user= $user")
    }

    @Usage('cancel CA')
    @Command
    def cancelCA(InvocationContext context,
                 @Usage("user") @Required @Argument String user) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def caService = beans.getBean(CaService.class)
        caService.cancelKeyPair(user)
        out.println("send CA cancel tx successful, user= $user")
    }

    @Usage('init CA')
    @Command
    def initCA(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def caInitService = beans.getBean(CaInitService.class)
        caInitService.initStart()
        out.println("send CA init tx successful")
    }

}
