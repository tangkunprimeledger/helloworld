package com.higgs.trust.rs.core.dao;

import com.google.common.collect.Lists;
import com.higgs.trust.rs.common.enums.RequestEnum;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RequestJDBCDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * batch update status
     *
     * @param rsCoreTxVOS
     * @param from
     * @param to
     */
    public int batchUpdateStatus(List<RsCoreTxVO> rsCoreTxVOS, RequestEnum from, RequestEnum to) {
        String sql = "UPDATE request SET ";
        String errorCodeConditionSql = " `resp_code`= CASE `request_id`";
        String errorMsgConditionSql = ",`resp_msg`= CASE `request_id`";
        String updateStatusSql = ", `status`='" + to.getCode() + "'";
        String updateTimeSql = ", `update_time`=NOW(3)";
        String conditionSql = " WHEN ? THEN ? ";
        String whereSql = "";
        List<Object> txIdList = Lists.newLinkedList();
        List<Object> errorCodeList = Lists.newLinkedList();
        List<String> errorMsgList = Lists.newLinkedList();

        for (RsCoreTxVO vo : rsCoreTxVOS) {
            String txId = vo.getTxId();
            txIdList.add(txId);
            whereSql += ",?";

            errorCodeConditionSql += conditionSql;
            errorCodeList.add(txId);
            errorCodeList.add(StringUtils.isBlank(vo.getErrorCode()) ? "000000" : vo.getErrorCode());

            errorMsgConditionSql += conditionSql;
            errorMsgList.add(txId);
            errorMsgList.add(StringUtils.isBlank(vo.getErrorMsg()) ? "success" : vo.getErrorMsg());
        }
        errorCodeConditionSql += " ELSE `resp_code` END";
        errorMsgConditionSql += " ELSE `resp_msg` END";
        whereSql = " WHERE `request_id` in (" + whereSql.substring(1) + ")  AND `status`='" + from.getCode() + "'";
        sql += errorCodeConditionSql + errorMsgConditionSql + updateStatusSql + updateTimeSql + whereSql;
        List<Object> params = Lists.newLinkedList();
        params.addAll(errorCodeList);
        params.addAll(errorMsgList);
        params.addAll(txIdList);
        return jdbcTemplate.update(sql, params.toArray());
    }
}
