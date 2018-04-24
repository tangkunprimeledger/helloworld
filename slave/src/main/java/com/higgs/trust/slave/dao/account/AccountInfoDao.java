package com.higgs.trust.slave.dao.account;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.account.AccountInfoPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * @author liuyu
 * @description account info DAO
 * @date 2018-03-27
 */
@Mapper public interface AccountInfoDao extends BaseDao<AccountInfoPO> {
    /**
     * query by accountNo,support FOR UPDATE
     *
     * @param accountNo
     * @param forUpdate
     * @return
     */
    AccountInfoPO queryByAccountNo(@Param("accountNo") String accountNo, @Param("forUpdate") boolean forUpdate);

    /**
     * increase balance for account
     *
     * @param accountNo
     * @param amount
     * @return
     */
    int increaseBalance(@Param("accountNo") String accountNo, @Param("amount") BigDecimal amount);

    /**
     * decrease balance for account
     *
     * @param accountNo
     * @param amount
     * @return
     */
    int decreaseBalance(@Param("accountNo") String accountNo, @Param("amount") BigDecimal amount);

    /**
     * freeze account balance
     * only increase freeze_amount
     *
     * @param accountNo
     * @param amount
     * @return
     */
    int freeze(@Param("accountNo") String accountNo, @Param("amount") BigDecimal amount);

    /**
     * unfreeze account balance
     * only decrease freeze_amount
     *
     * @param accountNo
     * @param amount
     * @return
     */
    int unfreeze(@Param("accountNo") String accountNo, @Param("amount") BigDecimal amount);

}
