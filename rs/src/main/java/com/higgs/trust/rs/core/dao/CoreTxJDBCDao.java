package com.higgs.trust.rs.core.dao;

import com.google.common.collect.Lists;
import com.higgs.trust.common.utils.CollectionBean;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CoreTxJDBCDao {

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
    public int batchInsert(List<CoreTransactionPO> list) {
        StringBuilder sql = new StringBuilder("INSERT INTO core_transaction " + " (tx_id,policy_id,lock_time,sender,version,biz_model,action_datas,sign_datas,execute_result,error_code,error_msg,send_time,block_height,tx_type,create_time)" + "  VALUES");
        String template = "(:c[${i}].txId,:c[${i}].policyId,:c[${i}].lockTime,:c[${i}].sender,:c[${i}].version,:c[${i}].bizModel,:c[${i}].actionDatas," + ":c[${i}].signDatas,:c[${i}].executeResult,:c[${i}].errorCode,:c[${i}].errorMsg,:c[${i}].sendTime,:c[${i}].blockHeight,:c[${i}].txType,NOW(3)),";
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
    public int batchUpdate(List<CoreTransactionPO> list, Long blockHeight) {
        StringBuilder sql = new StringBuilder("UPDATE core_transaction SET ");
        StringBuilder exeResultConditionSql = new StringBuilder(" `execute_result`= CASE `tx_id`");
        StringBuilder errorCodeConditionSql = new StringBuilder(",`error_code`= CASE `tx_id`");
        StringBuilder errorMsgConditionSql = new StringBuilder(",`error_msg`= CASE `tx_id`");
        String updateBlockHeightSql = ", `block_height`= " + blockHeight;
        String updateTimeSql = ", `update_time`=NOW(3)";
        String conditionSql = " WHEN ? THEN ? ";
        StringBuilder whereSql = new StringBuilder("");
        List<Object> txIdList = Lists.newLinkedList();
        List<Object> exeResultList = Lists.newLinkedList();
        List<Object> errorCodeList = Lists.newLinkedList();
        List<String> errorMsgList = Lists.newLinkedList();

        for (CoreTransactionPO po : list) {
            String txId = po.getTxId();
            txIdList.add(txId);
            whereSql.append(",?");

            exeResultConditionSql.append(conditionSql);
            exeResultList.add(txId);
            exeResultList.add(po.getExecuteResult());

            errorCodeConditionSql.append(conditionSql);
            errorCodeList.add(txId);
            errorCodeList.add(po.getErrorCode());

            errorMsgConditionSql.append(conditionSql);
            errorMsgList.add(txId);
            errorMsgList.add(po.getErrorMsg());
        }
        exeResultConditionSql.append(" ELSE `execute_result` END");
        errorCodeConditionSql.append(" ELSE `error_code` END");
        errorMsgConditionSql.append(" ELSE `error_msg` END");
        sql.append(exeResultConditionSql).append(errorCodeConditionSql).append(errorMsgConditionSql).append(updateBlockHeightSql).append(updateTimeSql).append(" WHERE `tx_id` in (" + whereSql.substring(1) + ")");
        List<Object> params = Lists.newLinkedList();
        params.addAll(exeResultList);
        params.addAll(errorCodeList);
        params.addAll(errorMsgList);
        params.addAll(txIdList);
        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

}
