package commands

import com.higgs.trust.consensus.config.NodeState
import com.higgs.trust.consensus.config.NodeStateEnum
import com.higgs.trust.management.failover.service.StandbyService
import lombok.extern.slf4j.Slf4j
import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Required
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

/**
 * standby groovy
 *
 * @author lingchao
 * @create 2018年10月09日10:24
 */
@Slf4j
@Usage("set standby")
class standby {
    @Usage('set Running')
    @Command
    def set(InvocationContext context, @Usage("is standby")
    @Required @Argument boolean isStandby) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def nodeState = beans.getBean(NodeState.class)
        def standbyService = beans.getBean(StandbyService.class)
        NodeStateEnum state = nodeState.state
        if(isStandby){
            if (state != NodeStateEnum.Running){
                out.println("Node state is not Running but $state, can't set standby")
                return
            }
            standbyService.startOrResume()
            nodeState.changeState(NodeStateEnum.Running, NodeStateEnum.Standby)
        }else {
            if (state != NodeStateEnum.Standby){
                out.println("Node state is not Standby but $state, can't set not standby")
                return
            }
            standbyService.pause()
            nodeState.changeState(NodeStateEnum.Standby, NodeStateEnum.Running)
        }
        //refresh registry for the p2p

        out.println("Standby set finish, now the state is : $nodeState.state")
    }
}
