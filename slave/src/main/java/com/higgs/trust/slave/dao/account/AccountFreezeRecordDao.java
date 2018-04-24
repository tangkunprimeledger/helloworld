package com.higgs.trust.slave.dao.account;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.account.AccountFreezeRecordPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * @author liuyu
 * @descrition
 * @date 2018-03-30
 */
@Mapper public interface AccountFreezeRecordDao extends BaseDao<AccountFreezeRecordPO> {

    /**
     * query by flowNo and accountNo,return entity
     *
     * @param bizFlowNo
     * @param accountNo
     * @return
     */
    AccountFreezeRecordPO queryByFlowNoAndAccountNo(@Param("bizFlowNo") String bizFlowNo,
        @Param("accountNo") String accountNo);

    /**
     * decrease amount by data id
     *
     * @param id
     * @param amount
     * @return
     */
    int decreaseAmount(@Param("id") Long id, @Param("amount") BigDecimal amount);
}
