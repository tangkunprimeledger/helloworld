package com.higgs.trust.rs.core.dao;

import com.google.common.collect.Lists;
import com.higgs.trust.common.utils.CollectionBean;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component @Slf4j public class CoreTxJDBCDao {

    @Autowired private NamedParameterJdbcTemplate jdbc;
    @Autowired private JdbcTemplate jdbcTemplate;

    /**
     * batch insert
     *
     * @param list
     * @return
     */
    public int batchInsertTx(List<CoreTransactionPO> list) {
        StringBuilder sql = new StringBuilder("INSERT INTO core_transaction "
            + " (id,tx_id,policy_id,lock_time,sender,version,biz_model,action_datas,sign_datas,status,execute_result,error_code,error_msg,send_time,block_height,create_time)"
            + "  VALUES");
        String template =
            "(:c[${i}].id,:c[${i}].txId,:c[${i}].policyId,:c[${i}].lockTime,:c[${i}].sender,:c[${i}].version,:c[${i}].bizModel,:c[${i}].actionDatas,"
                + ":c[${i}].signDatas,:c[${i}].status,:c[${i}].executeResult,:c[${i}].errorCode,:c[${i}].errorMsg,:c[${i}].sendTime,:c[${i}].blockHeight,NOW(3)),";
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
    public int batchUpdateStatus(List<CoreTransactionPO> list, CoreTxStatusEnum from, CoreTxStatusEnum to,
        Long blockHeight) {
        String sql = "UPDATE core_transaction SET ";
        String exeResultConditionSql = " `execute_result`= CASE `tx_id`";
        String errorCodeConditionSql = ",`error_code`= CASE `tx_id`";
        String errorMsgConditionSql = ",`error_msg`= CASE `tx_id`";
        String updateStatusSql = ", `status`='" + to.getCode() + "'";
        String updateBlockHeightSql = ", `block_height`= " + blockHeight;
        String updateTimeSql = ", `update_time`=NOW(3)";
        String conditionSql = " WHEN ? THEN ? ";
        String whereSql = "";
        List<Object> txIdList = Lists.newLinkedList();
        List<Object> exeResultList = Lists.newLinkedList();
        List<Object> errorCodeList = Lists.newLinkedList();
        List<String> errorMsgList = Lists.newLinkedList();

        for (CoreTransactionPO po : list) {
            String txId = po.getTxId();
            txIdList.add(txId);
            whereSql += ",?";

            exeResultConditionSql += conditionSql;
            exeResultList.add(txId);
            exeResultList.add(po.getExecuteResult());

            errorCodeConditionSql += conditionSql;
            errorCodeList.add(txId);
            errorCodeList.add(po.getErrorCode());

            errorMsgConditionSql += conditionSql;
            errorMsgList.add(txId);
            errorMsgList.add(po.getErrorMsg());
        }
        exeResultConditionSql += " ELSE `execute_result` END";
        errorCodeConditionSql += " ELSE `error_code` END";
        errorMsgConditionSql += " ELSE `error_msg` END";
        whereSql = " WHERE `tx_id` in (" + whereSql.substring(1) + ")  AND `status`='" + from.getCode() + "'";
        sql += exeResultConditionSql + errorCodeConditionSql + errorMsgConditionSql + updateStatusSql
            + updateBlockHeightSql + updateTimeSql + whereSql;
        List<Object> params = Lists.newLinkedList();
        params.addAll(exeResultList);
        params.addAll(errorCodeList);
        params.addAll(errorMsgList);
        params.addAll(txIdList);
        return jdbcTemplate.update(sql, params.toArray());
    }

}
