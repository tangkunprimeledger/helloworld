package commands

import com.higgs.trust.common.dao.RocksDBSearcher
import com.higgs.trust.common.dao.RocksUtils
import com.higgs.trust.rs.core.api.CaService
import com.higgs.trust.slave.api.RocksDbService
import com.higgs.trust.slave.core.service.ca.CaInitService
import lombok.extern.slf4j.Slf4j
import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Required
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.springframework.beans.factory.BeanFactory

/**
 * @desc rocksdb tools
 * @author liuyu
 * @date 2018/09/13 16:16
 */

@Slf4j
@Usage("rocksdb tools")
class rocksdb {

    @Usage('show all table names')
    @Command
    def showTables(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def searcher = beans.getBean(RocksDBSearcher.class)
        def result = searcher.showTables()
        if (result) {
            out.println("$result")
        }else{
            out.println("is empty")
        }
    }

    @Usage('query by key')
    @Command
    def queryByKey(InvocationContext context,
                  @Usage("tableName") @Required @Argument String tableName,@Usage("keyName") @Required @Argument String keyName) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def searcher = beans.getBean(RocksDBSearcher.class)
        def result = searcher.queryByKey(tableName,keyName)
        if (result) {
            out.println("$result")
        } else {
            out.println("is empty")
        }
    }

    @Usage('query by prefix and limit size')
    @Command
    def queryByPrefix(InvocationContext context,
                   @Usage("tableName") @Required @Argument String tableName,
                      @Usage("prefix") @Required @Argument String prefix,
                      @Usage("limit") @Argument int limit
                      ) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def searcher = beans.getBean(RocksDBSearcher.class)
        def result = searcher.queryByPrefix(tableName,prefix,limit)
        if (result) {
            result.forEach({ entry -> out.println("$entry") })
        } else {
            out.println("is empty")
        }
    }
}
