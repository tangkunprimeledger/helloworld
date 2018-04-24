package com.higgs.trust.slave.dao.transaction;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.transaction.ActionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * action data deal dao
 *
 * @author lingchao
 * @create 2018年03月27日20:10
 */
@Mapper public interface ActionDao extends BaseDao<ActionPO> {
    /**
     * batch insert
     *
     * @param list
     * @return
     */
    int batchInsert(List<ActionPO> list);

    /**
     * query by TxId
     *
     * @param txId
     * @return
     */
    List<ActionPO> queryByTxId(@Param("txId") String txId);
}
