package com.higgs.trust.slave.dao.transaction;

import com.higgs.trust.common.utils.CollectionBean;
import com.higgs.trust.slave.dao.po.transaction.TransactionPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-06-20
 */
@Component @Slf4j public class TransactionJDBCDao {
    @Autowired private NamedParameterJdbcTemplate jdbc;

    /**
     * batch insert
     *
     * @param list
     * @return
     */
    public int batchInsertTransaction(List<TransactionPO> list) {
        StringBuilder sql = new StringBuilder("INSERT INTO TRANSACTION "
            + " ( `tx_id`, `biz_model`, `policy_id`, `lock_time`, `sender`, `version`, `block_height`, `block_time`, `send_time`, `action_datas`, `sign_datas`, `execute_result`, `error_code`)"
            + "  VALUES");
        String template = "(:c[${i}].txId,:c[${i}].bizModel,:c[${i}].policyId,:c[${i}].lockTime,:c[${i}].sender,"
            + ":c[${i}].version,:c[${i}].blockHeight,:c[${i}].blockTime,:c[${i}].sendTime,:c[${i}].actionDatas,:c[${i}].signDatas,:c[${i}].executeResult,:c[${i}].errorCode),";
        int size = list.size();
        for (int i = 0; i < size; i++) {
            sql.append(template.replaceAll("\\$\\{i\\}", String.valueOf(i)));
        }
        sql.deleteCharAt(sql.length() - 1);
        return jdbc.update(sql.toString(), new BeanPropertySqlParameterSource(new CollectionBean(list)));
    }
}
