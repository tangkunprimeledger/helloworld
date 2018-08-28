package com.higgs.trust.slave.dao.mysql.account;

import com.google.common.collect.Lists;
import com.higgs.trust.common.utils.CollectionBean;
import com.higgs.trust.slave.model.bo.account.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-06-20
 */
@Component @Slf4j public class AccountJDBCDao {
    @Autowired private NamedParameterJdbcTemplate jdbc;
    @Autowired private JdbcTemplate jdbcTemplate;

    /**
     * batch insert
     *
     * @param list
     * @return
     */
    public int batchInsertAccount(List<AccountInfo> list) {
        StringBuilder sql = new StringBuilder("INSERT INTO ACCOUNT_INFO "
            + " (id,account_no,currency,balance,freeze_amount,fund_direction,detail_no,detail_freeze_no,status,create_time)"
            + "  VALUES");
        String template = "(:c[${i}].id,:c[${i}].accountNo,:c[${i}].currency,:c[${i}].balance,:c[${i}].freezeAmount,"
            + ":c[${i}].fundDirection,:c[${i}].detailNo,:c[${i}].detailFreezeNo,:c[${i}].status,NOW(3)),";
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
    public int batchUpdateAccount(List<AccountInfo> list) {
        String sql = "UPDATE ACCOUNT_INFO";
        String balanceConditionSql = " SET `balance`= CASE `account_no`";
        String freezeAmountConditionSql = ",`freeze_amount`= CASE `account_no`";
        String updateTimeSql = ", `update_time`=NOW(3)";
        String conditionSql = " WHEN ? THEN ?";
        String whereSql = "";
        List<Object> balanceList = Lists.newLinkedList();
        List<Object> freezeAmountList = Lists.newLinkedList();
        List<String> accountNoList = Lists.newLinkedList();

        for (AccountInfo accountInfo : list) {
            String accountNo = accountInfo.getAccountNo();
            accountNoList.add(accountNo);
            whereSql += ",?";

            balanceConditionSql += conditionSql;
            balanceList.add(accountNo);
            balanceList.add(accountInfo.getBalance());

            freezeAmountConditionSql += conditionSql;
            freezeAmountList.add(accountNo);
            freezeAmountList.add(accountInfo.getFreezeAmount());
        }
        balanceConditionSql += " ELSE `balance` END";
        freezeAmountConditionSql += " ELSE `freeze_amount` END";
        whereSql = " WHERE `account_no` in (" + whereSql.substring(1) + ")";
        sql += balanceConditionSql + freezeAmountConditionSql + updateTimeSql + whereSql;
        List<Object> params = Lists.newLinkedList();
        params.addAll(balanceList);
        params.addAll(freezeAmountList);
        params.addAll(accountNoList);
        return jdbcTemplate.update(sql, params.toArray());
    }

    /**
     * batch insert account detail
     *
     * @param accountDetails
     * @return
     */
    public int batchInsertAccountDetail(List<AccountDetail> accountDetails) {
        StringBuilder sql = new StringBuilder("INSERT INTO ACCOUNT_DETAIL "
            + " (id,biz_flow_no,account_no,block_height,detail_no,change_direction,amount,before_amount,after_amount,remark,create_time)"
            + "  VALUES");
        String template =
            "(:c[${i}].id,:c[${i}].bizFlowNo,:c[${i}].accountNo,:c[${i}].blockHeight,:c[${i}].detailNo,:c[${i}].changeDirection,"
                + ":c[${i}].amount,:c[${i}].beforeAmount,:c[${i}].afterAmount,:c[${i}].remark,now(3)),";
        int size = accountDetails.size();
        for (int i = 0; i < size; i++) {
            sql.append(template.replaceAll("\\$\\{i\\}", String.valueOf(i)));
        }
        sql.deleteCharAt(sql.length() - 1);
        return jdbc.update(sql.toString(), new BeanPropertySqlParameterSource(new CollectionBean(accountDetails)));
    }

    /**
     * batch insert account dc detail
     *
     * @param dcRecords
     * @return
     */
    public int batchInsertDcRecords(List<AccountDcRecord> dcRecords) {
        StringBuilder sql = new StringBuilder(
            "INSERT INTO ACCOUNT_DC_RECORD " + " (id,biz_flow_no,account_no,dc_flag,amount,create_time)" + "  VALUES");
        String template = "(:c[${i}].id,:c[${i}].bizFlowNo,:c[${i}].accountNo,:c[${i}].dcFlag,:c[${i}].amount,now(3)),";
        int size = dcRecords.size();
        for (int i = 0; i < size; i++) {
            sql.append(template.replaceAll("\\$\\{i\\}", String.valueOf(i)));
        }
        sql.deleteCharAt(sql.length() - 1);
        return jdbc.update(sql.toString(), new BeanPropertySqlParameterSource(new CollectionBean(dcRecords)));
    }

    /**
     * batch insert
     *
     * @param currencyInfos
     */
    public int batchInsertCurrency(List<CurrencyInfo> currencyInfos) {
        StringBuilder sql =
            new StringBuilder("INSERT INTO CURRENCY_INFO" + "  (id,currency,remark,create_time) " + "  VALUES");
        String template = "(:c[${i}].id,:c[${i}].currency,:c[${i}].remark,now(3)),";
        int size = currencyInfos.size();
        for (int i = 0; i < size; i++) {
            sql.append(template.replaceAll("\\$\\{i\\}", String.valueOf(i)));
        }
        sql.deleteCharAt(sql.length() - 1);
        return jdbc.update(sql.toString(), new BeanPropertySqlParameterSource(new CollectionBean(currencyInfos)));
    }

    /**
     * batch insert freeze record
     *
     * @param accountFreezeRecords
     */
    public int batchInsertFreezeRecord(List<AccountFreezeRecord> accountFreezeRecords) {
        StringBuilder sql = new StringBuilder("INSERT INTO ACCOUNT_FREEZE_RECORD"
            + "  (id,biz_flow_no,account_no,block_height,contract_addr,amount,create_time)" + "  VALUES");
        String template =
            "(:c[${i}].id,:c[${i}].bizFlowNo,:c[${i}].accountNo,:c[${i}].blockHeight,:c[${i}].contractAddr,:c[${i}].amount,now(3)),";
        int size = accountFreezeRecords.size();
        for (int i = 0; i < size; i++) {
            sql.append(template.replaceAll("\\$\\{i\\}", String.valueOf(i)));
        }
        sql.deleteCharAt(sql.length() - 1);
        return jdbc
            .update(sql.toString(), new BeanPropertySqlParameterSource(new CollectionBean(accountFreezeRecords)));
    }

    /**
     * batch update freeze record
     *
     * @param accountFreezeRecords
     */
    public int batchUpdateFreezeRecord(List<AccountFreezeRecord> accountFreezeRecords) {
        String sql = "UPDATE ACCOUNT_FREEZE_RECORD SET ";

        StringBuilder sAmountConditionSql = new StringBuilder(" `amount` = CASE");
        StringBuilder sContractAddrConditionSql = new StringBuilder(",`contract_addr`= CASE");
        String updateTimeSql = ", `update_time`=NOW(3)";
        String conditionSql = " WHEN `account_no` = ? AND `biz_flow_no` = ? THEN ? ";
        StringBuilder whereSql = new StringBuilder(" WHERE (");
        String whereConditionSql = " `account_no` = ? AND `biz_flow_no` = ? ";

        List<Object> sAmountConditionList = Lists.newLinkedList();
        List<Object> sContractAddrConditionList = Lists.newLinkedList();
        List<Object> whereConditionList = Lists.newLinkedList();
        int index = 0;
        int size = accountFreezeRecords.size();
        for (AccountFreezeRecord accountFreezeRecord : accountFreezeRecords) {
            String accountNo = accountFreezeRecord.getAccountNo();
            String bizFlowNo = accountFreezeRecord.getBizFlowNo();
            BigDecimal amount = accountFreezeRecord.getAmount();
            String contractAddr = accountFreezeRecord.getContractAddr();

            sAmountConditionSql.append(conditionSql);
            sAmountConditionList.add(accountNo);
            sAmountConditionList.add(bizFlowNo);
            sAmountConditionList.add(amount);

            sContractAddrConditionSql.append(conditionSql);
            sContractAddrConditionList.add(accountNo);
            sContractAddrConditionList.add(bizFlowNo);
            sContractAddrConditionList.add(contractAddr);

            whereSql.append(whereConditionSql);
            if(index < size - 1){
                whereSql.append(" OR ");
            }
            whereConditionList.add(accountNo);
            whereConditionList.add(bizFlowNo);
            index++;
        }
        sAmountConditionSql.append(" ELSE `amount` END");
        sContractAddrConditionSql.append(" ELSE `contract_addr` END");
        whereSql.append(")");
        sql += sAmountConditionSql.append(updateTimeSql).append(sContractAddrConditionSql).append(whereSql);
        List<Object> params = Lists.newLinkedList();
        params.addAll(sAmountConditionList);
        params.addAll(sContractAddrConditionList);
        params.addAll(whereConditionList);
        return jdbcTemplate.update(sql, params.toArray());
    }

    /**
     * batch insert detail freeze
     *
     * @param detailFreezes
     */
    public int batchInsertDetailFreezes(List<AccountDetailFreeze> detailFreezes) {
        StringBuilder sql = new StringBuilder(
            "INSERT INTO ACCOUNT_DETAIL_FREEZE(id,biz_flow_no,account_no,block_height,freeze_detail_no,freeze_type,"
                + " amount,before_amount,after_amount,remark,create_time)" + " VALUES");
        String template =
            "(:c[${i}].id,:c[${i}].bizFlowNo,:c[${i}].accountNo,:c[${i}].blockHeight,:c[${i}].freezeDetailNo,:c[${i}].freezeType,"
                + ":c[${i}].amount,:c[${i}].beforeAmount,:c[${i}].afterAmount,:c[${i}].remark,now(3)),";
        int size = detailFreezes.size();
        for (int i = 0; i < size; i++) {
            sql.append(template.replaceAll("\\$\\{i\\}", String.valueOf(i)));
        }
        sql.deleteCharAt(sql.length() - 1);
        return jdbc.update(sql.toString(), new BeanPropertySqlParameterSource(new CollectionBean(detailFreezes)));
    }
}
