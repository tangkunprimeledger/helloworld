package com.higgs.trust.slave.dao.utxo;

import com.google.common.collect.Lists;
import com.higgs.trust.common.utils.CollectionBean;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * TxOut  JDBC  Dao
 *
 * @author lingchao
 * @create 2018年06月20日10:35
 */
@Service
public class TxOutJDBCDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcTemplate template;

    /**
     * batch insert
     *
     * @param txOutPOList
     * @return
     */
    public int batchInsert(List<TxOutPO> txOutPOList) {
        if (CollectionUtils.isEmpty(txOutPOList)) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("INSERT INTO `tx_out` " + "(`tx_id`, `index`, `action_index`, `identity`, `state_class`, `state`, `contract_address`, `status`, `create_time`, `update_time`) " + "VALUES ");

        String template = "(:c[${i}].txId, :c[${i}].index, :c[${i}].actionIndex, :c[${i}].identity, :c[${i}].stateClass, :c[${i}].state, " + ":c[${i}].contractAddress, :c[${i}].status, now(3), now(3)),";
        int size = txOutPOList.size();
        for (int i = 0; i < size; i++) {
            sql.append(template.replaceAll("\\$\\{i\\}", String.valueOf(i)));
        }
        sql.deleteCharAt(sql.length() - 1);

        return jdbcTemplate.update(sql.toString(), new BeanPropertySqlParameterSource(new CollectionBean(txOutPOList)));
    }

    /**
     * batch update
     *
     * @param txOutPOList
     * @return
     */
    public int batchUpdate(List<TxOutPO> txOutPOList) {
        if (CollectionUtils.isEmpty(txOutPOList)) {
            return 0;
        }
        StringBuffer sql = new StringBuffer("UPDATE `tx_out`");
        StringBuffer sTxIdConditionSql = new StringBuffer(" SET `s_tx_id` = CASE");
        StringBuffer statusConditionSql = new StringBuffer(" `status` = CASE");
        StringBuffer updateTimeConditionSql = new StringBuffer(" `update_time` = CASE");
        String conditionSql = " WHEN `tx_id` = ? AND `index` = ? AND `action_index` = ? THEN ?";
        String updateTimeCondition = " WHEN `tx_id` = ? AND `index` = ? AND `action_index` = ? THEN now(3)";
        StringBuffer whereSql = new StringBuffer(" WHERE (");
        String whereConditionSql = " `tx_id`= ? AND `index`= ? AND `action_index`= ? AND `status`=\"UNSPENT\"";

        List<Object> sTxIdConditionList = Lists.newLinkedList();
        List<Object> statusConditionList = Lists.newLinkedList();
        List<Object> updateTimeConditionList = Lists.newLinkedList();
        List<Object> whereConditionList = Lists.newLinkedList();
        int size = txOutPOList.size();

        for (int i = 0; i < size; i++) {
            TxOutPO txOutPO = txOutPOList.get(i);
            String txId = txOutPO.getTxId();
            Integer index = txOutPO.getIndex();
            Integer actionIndex = txOutPO.getActionIndex();
            String sTxId = txOutPO.getSTxId();
            String status = txOutPO.getStatus();


            //stxId condition list
            sTxIdConditionSql.append(conditionSql);
            sTxIdConditionList.add(txId);
            sTxIdConditionList.add(index);
            sTxIdConditionList.add(actionIndex);
            sTxIdConditionList.add(sTxId);


            //status condition list
            statusConditionSql.append(conditionSql);
            statusConditionList.add(txId);
            statusConditionList.add(index);
            statusConditionList.add(actionIndex);
            statusConditionList.add(status);

            //updateTime condition list
            updateTimeConditionSql.append(updateTimeCondition);
            updateTimeConditionList.add(txId);
            updateTimeConditionList.add(index);
            updateTimeConditionList.add(actionIndex);

            // where condition list
            whereSql.append(whereConditionSql);
            if (i < size - 1) {
                whereSql.append(" or");
            }
            whereConditionList.add(txId);
            whereConditionList.add(index);
            whereConditionList.add(actionIndex);
        }
        sTxIdConditionSql.append(" ELSE `s_tx_id` END,");
        statusConditionSql.append(" ELSE `status` END,");
        updateTimeConditionSql.append(" ELSE `update_time` END");
        whereSql.append(" )");
        sql.append(sTxIdConditionSql).append(statusConditionSql).append(updateTimeConditionSql).append(whereSql);
        List<Object> params = Lists.newLinkedList();
        params.addAll(sTxIdConditionList);
        params.addAll(statusConditionList);
        params.addAll(updateTimeConditionList);
        params.addAll(whereConditionList);

        return template.update(sql.toString(), params.toArray());
    }

}
