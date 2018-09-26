package com.higgs.trust.slave.dao.mysql.dataIdentity;

import com.higgs.trust.common.utils.CollectionBean;
import com.higgs.trust.slave.dao.po.DataIdentityPO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DataIdentity jdbc Dao
 *
 * @author lingchao
 * @create 2018年06月20日17:17
 */
@Service
public class DataIdentityJDBCDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * batch insert
     *
     * @param dataIdentityList
     * @return
     */
    public int batchInsert(List<DataIdentityPO> dataIdentityList) {
        if (CollectionUtils.isEmpty(dataIdentityList)) {
            return 0;
        }

        StringBuilder sql = new StringBuilder("INSERT INTO `data_identity` " + "(`identity`, `chain_owner`, `data_owner`, `create_time`) " + "VALUES ");

        String template = "(:c[${i}].identity, :c[${i}].chainOwner, :c[${i}].dataOwner, now(3)),";
        int size = dataIdentityList.size();
        for (int i = 0; i < size; i++) {
            sql.append(template.replaceAll("\\$\\{i\\}", String.valueOf(i)));
        }
        sql.deleteCharAt(sql.length() - 1);

        return jdbcTemplate.update(sql.toString(), new BeanPropertySqlParameterSource(new CollectionBean(dataIdentityList)));
    }
}
