package com.higgs.trust.rs.core.dao;

import com.higgs.trust.common.utils.CollectionBean;
import com.higgs.trust.rs.core.dao.po.VoteRulePO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component public class VoteRuleJDBCDao {

    @Autowired private NamedParameterJdbcTemplate jdbc;
    @Autowired private JdbcTemplate jdbcTemplate;

    /**
     * batch insert
     *
     * @param list
     * @return
     */
    public int batchInsert(List<VoteRulePO> list) {
        StringBuilder sql = new StringBuilder("INSERT INTO vote_rule "
            + " (id,policy_id,vote_pattern,callback_type,create_time)"
            + "  VALUES");
        String template = "(:c[${i}].id,:c[${i}].policyId,:c[${i}].votePattern,:c[${i}].callbackType,NOW(3)),";
        int size = list.size();
        for (int i = 0; i < size; i++) {
            sql.append(template.replaceAll("\\$\\{i\\}", String.valueOf(i)));
        }
        sql.deleteCharAt(sql.length() - 1);
        return jdbc.update(sql.toString(), new BeanPropertySqlParameterSource(new CollectionBean(list)));
    }
}