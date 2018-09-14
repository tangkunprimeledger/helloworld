package commands

import com.higgs.trust.common.dao.RocksDBHelper
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
        def helper = beans.getBean(RocksDBHelper.class)
        def result = helper.showTables()
        if (result) {
            out.println("$result")
        } else {
            out.println("is empty")
        }
    }

    @Usage('query by key')
    @Command
    def queryByKey(InvocationContext context,
                   @Usage("tableName") @Required @Argument String tableName,
                   @Usage("keyName") @Required @Argument String keyName) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def helper = beans.getBean(RocksDBHelper.class)
        def result = helper.queryByKey(tableName, keyName)
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
        def helper = beans.getBean(RocksDBHelper.class)
        def result = helper.queryByPrefix(tableName, prefix, limit)
        if (result) {
            result.forEach({ entry -> out.println("$entry") })
        } else {
            out.println("is empty")
        }
    }

    @Usage('query by count and order')
    @Command
    def queryByLimit(InvocationContext context,
                     @Usage("tableName") @Required @Argument String tableName,
                     @Usage("count") @Required @Argument int count,
                     @Usage("order 0:DESC 1:ASC") @Required @Argument int order
    ) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def helper = beans.getBean(RocksDBHelper.class)
        def result = helper.queryByLimit(tableName, count, order)
        if (result) {
            result.forEach({ entry -> out.println("$entry") })
        } else {
            out.println("is empty")
        }
    }


    @Usage('clear tables')
    @Command
    def clear(InvocationContext context,
                     @Usage("tableNames") @Required @Argument String ...tableNames) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def helper = beans.getBean(RocksDBHelper.class)
        def result = helper.clear(tableNames)
        if (result) {
            out.println("clear is success")
        } else {
            out.println("clear is fail")
        }
    }

    @Usage('clear all tables')
    @Command
    def clearAll(InvocationContext context) {
        BeanFactory beans = context.attributes['spring.beanfactory']
        def helper = beans.getBean(RocksDBHelper.class)
        def result = helper.clearAll()
        if (result) {
            out.println("clear all is success")
        } else {
            out.println("clear all is fail")
        }
    }
}
