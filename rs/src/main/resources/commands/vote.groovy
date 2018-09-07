package commands

import com.higgs.trust.rs.core.api.VoteService
import lombok.extern.slf4j.Slf4j
import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Required
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

/**
 * @desc vote
 * @author liuyu
 * @date 2018/09/07 16:54
 */

@Slf4j
@Usage("cluster config")
class vote {

    @Usage('receipt for vote')
    @Command
    def receipt(InvocationContext context,
                @Usage("txId") @Required @Argument String txId, @Usage("agree") @Required @Argument Boolean agree) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def voteService = beans.getBean(VoteService.class)
        voteService.receiptVote(txId, agree)
        out.println("receipt success")
    }

    @Usage('show INIT vote request by page')
    @Command
    def show(InvocationContext context,
             @Usage("row") @Argument int row, @Usage("count") @Argument int count) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def voteService = beans.getBean(VoteService.class)
        def list = voteService.queryAllInitRequest(row,count)
        list.forEach({item->context.provide(name:item.txId,value:sender)})
    }
}
