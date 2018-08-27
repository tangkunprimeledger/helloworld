package com.higgs.trust.rs.core.dao;

import com.google.common.collect.Lists;
import com.higgs.trust.common.utils.CollectionBean;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lingchao
 * @description
 * @date 2018-08-21
 */
@Component
@Slf4j
public class CoreTxProcessJDBCDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * batch insert
     *
     * @param list
     * @return
     */
    public int batchInsert(List<CoreTransactionProcessPO> list) {
        StringBuilder sql = new StringBuilder("INSERT INTO core_transaction_process " + " (tx_id,status,create_time)" + "  VALUES");
        String template = "(:c[${i}].txId,:c[${i}].status,NOW(3)),";
        int size = list.size();
        for (int i = 0; i < size; i++) {
            sql.append(template.replaceAll("\\$\\{i\\}", String.valueOf(i)));
        }
        sql.deleteCharAt(sql.length() - 1);
        return jdbc.update(sql.toString(), new BeanPropertySqlParameterSource(new CollectionBean(list)));
    }

    /**
     * batch update
     *
     * @param list
     * @return
     */
    public int batchUpdate(List<CoreTransactionProcessPO> list, CoreTxStatusEnum from, CoreTxStatusEnum to) {
        StringBuilder sql = new StringBuilder("UPDATE core_transaction_process SET ");
        String updateStatusSql = "`status`='" + to.getCode() + "'";
        String updateTimeSql = ", `update_time`=NOW(3)";
        StringBuilder whereSql = new StringBuilder();
        List<Object> txIdList = Lists.newLinkedList();
        for (CoreTransactionProcessPO po : list) {
            String txId = po.getTxId();
            txIdList.add(txId);
            whereSql.append(",?");
        }
        sql.append(updateStatusSql).append(updateTimeSql).append(" WHERE `tx_id` in (" + whereSql.substring(1) + ")  AND `status`='" + from.getCode() + "'");
        List<Object> params = Lists.newLinkedList();
        params.addAll(txIdList);
        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

}
