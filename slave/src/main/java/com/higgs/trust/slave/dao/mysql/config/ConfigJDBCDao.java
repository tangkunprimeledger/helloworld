package com.higgs.trust.slave.dao.mysql.config;

import com.google.common.collect.Lists;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-08-02
 */
@Component @Slf4j public class ConfigJDBCDao {
    @Autowired private NamedParameterJdbcTemplate jdbc;
    @Autowired private JdbcTemplate jdbcTemplate;

    /**
     * batch update
     *
     * @param nodes
     */
    public void batchEnable(List<String> nodes) {
        String sql = "UPDATE config SET ";
        String errorCodeConditionSql = "`pubKey`= `tmpPubKey`";
        String errorMsgConditionSql = ",`priKey`= `tmpPriKey`";
        String updateStatusSql = ", `valid`=true";
        String updateTimeSql = ", `update_time`=NOW(3)";
        String whereSql = "";
        List<Object> txIdList = Lists.newLinkedList();
        for (String node : nodes) {
            txIdList.add(node);
            whereSql += ",?";
        }
        whereSql = " WHERE `node_name` in (" + whereSql.substring(1) + ")";
        sql += errorCodeConditionSql + errorMsgConditionSql + updateStatusSql + updateTimeSql + whereSql;
        List<Object> params = Lists.newLinkedList();
        params.addAll(txIdList);
        int size = jdbcTemplate.update(sql, params.toArray());
        if (size != nodes.size()) {
            log.error("[batchEnable] has fail ,updated.size:{},data.size:{}", size, nodes.size());
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_BATCH_UPDATE_ERROR);
        }
    }

    /**
     * batch update
     *
     * @param nodes
     */
    public void batchCancel(List<String> nodes) {
        String sql = "UPDATE config SET ";
        String updateStatusSql = " `valid`=false";
        String updateTimeSql = ", `update_time`=NOW(3)";
        String whereSql = "";
        List<Object> txIdList = Lists.newLinkedList();
        for (String node : nodes) {
            txIdList.add(node);
            whereSql += ",?";
        }
        whereSql = " WHERE `node_name` in (" + whereSql.substring(1) + ")";
        sql += updateStatusSql + updateTimeSql + whereSql;
        List<Object> params = Lists.newLinkedList();
        params.addAll(txIdList);
        int size = jdbcTemplate.update(sql, params.toArray());
        if (size != nodes.size()) {
            log.error("[batchCancel] has fail ,updated.size:{},data.size:{}", size, nodes.size());
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_BATCH_UPDATE_ERROR);
        }
    }
}
