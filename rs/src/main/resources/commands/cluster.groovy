package commands

import com.higgs.trust.rs.core.service.NodeConsensusService
import lombok.extern.slf4j.Slf4j
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

/**
 * @desc ca information
 * @author WangQuanzhou
 * @date 2018/5/29 16:54    
 */

@Slf4j
@Usage("cluster config")
class cluster {
    @Usage('join consensus layer')
    @Command
    def joinConsensus(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeService = beans.getBean(NodeConsensusService.class)
        def result = nodeService.joinConsensus()
        out.println("join consensus layer result= $result")
    }

    @Usage('leave consensus layer')
    @Command
    def leaveConsensus(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeService = beans.getBean(NodeConsensusService.class)
        def result = nodeService.leaveConsensus()
        out.println("leave consensus layer result= $result")
    }

    @Usage('join consensus request layer')
    @Command
    def joinRequest(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeService = beans.getBean(NodeConsensusService.class)
        def result = nodeService.joinRequest()
        out.println("join request result= $result")
    }
}
