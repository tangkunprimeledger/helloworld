package commands

import com.higgs.trust.rs.core.repository.BizTypeRepository
import lombok.extern.slf4j.Slf4j
import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Required
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

@Slf4j
@Usage("for biz type operation")
class bizType {
    @Usage('get biz type')
    @Command
    def get(InvocationContext context, @Usage("policy id") @Required @Argument String policyId) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def bizTypeRepository = beans.getBean(BizTypeRepository.class)
        def bizType = bizTypeRepository.getByPolicyId(policyId)
        out.println(bizType)
    }

    @Usage('add biz type')
    @Command
    def add(InvocationContext context, @Usage("policy id") @Required @Argument String policyId,
            @Usage("biz type") @Required @Argument String bizType) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def bizTypeRepository = beans.getBean(BizTypeRepository.class)
        def msg = bizTypeRepository.add(policyId, bizType)
        out.println(msg)
    }

    @Usage('update biz type')
    @Command
    def update(InvocationContext context, @Usage("policy id") @Required @Argument String policyId,
            @Usage("biz type") @Required @Argument String bizType) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def bizTypeRepository = beans.getBean(BizTypeRepository.class)
        def msg = bizTypeRepository.update(policyId, bizType)
        out.println(msg)
    }
}
