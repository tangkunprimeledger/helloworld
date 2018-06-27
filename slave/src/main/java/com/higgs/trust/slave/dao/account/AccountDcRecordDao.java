package com.higgs.trust.slave.dao.account;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.slave.dao.po.account.AccountDcRecordPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liuyu
 * @description account DC record DAO
 * @date 2018-03-27
 */
@Mapper public interface AccountDcRecordDao extends BaseDao<AccountDcRecordPO> {
    /**
     * batch insert
     *
     * @param list
     * @return
     */
    int batchInsert(@Param("list") List<AccountDcRecordPO> list);
}
