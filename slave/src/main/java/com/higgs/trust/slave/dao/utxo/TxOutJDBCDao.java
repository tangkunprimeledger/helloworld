package com.higgs.trust.slave.dao.utxo;

import com.google.common.base.Preconditions;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private NamedParameterJdbcTemplate jdbc;


    /**
     * batch insert
     *
     * @param txOutPOList
     * @return
     */
    int batchInsert(List<TxOutPO> txOutPOList){
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(txOutPOList), "txOutPOList is empty");

        StringBuilder sql = new StringBuilder("INSERT INTO `vn_callback_process_record` " +
                "(`instructionId`, `blockHeight`, `index`, `receiptType`, `status`, `localCreateAt`, `localUpdateAt`, `version`) " +
                "VALUES ");

        String template = "(:c[${i}].instructionId, :c[${i}].blockHeight, :c[${i}].index, :c[${i}].receiptType.name, :c[${i}].status.name, " +
                ":c[${i}].localCreateAt, :c[${i}].localUpdateAt, :c[${i}].version),";
        int size = txOutPOList.size();
        for (int i = 0; i < size; i++) {
            sql.append(template.replaceAll("\\$\\{i\\}", String.valueOf(i)));
        }
        sql.deleteCharAt(sql.length() - 1);

        int update = jdbc.update(sql.toString(), new BeanPropertySqlParameterSource(new CollectionBean(txOutPOList)));
       return update;
    }

    /**
     * batch update
     *
     * @param txOutPOList
     * @return
     */
    int batchUpdate(List<TxOutPO> txOutPOList){

    }

}
