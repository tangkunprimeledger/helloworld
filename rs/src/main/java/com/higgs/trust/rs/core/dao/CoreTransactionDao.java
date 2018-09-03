package com.higgs.trust.rs.core.dao;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CoreTransactionDao extends BaseDao<CoreTransactionPO> {

    /**
     * query by tx id
     * @param txId
     * @return
     */
    CoreTransactionPO queryByTxId(@Param("txId")String txId,@Param("forUpdate")boolean forUpdate);
    /**
     * query by txs
     * @param txIdList
     * @return
     */
    List<CoreTransactionPO> queryByTxIds(List<String> txIdList);
    /**
     * save tx execute reslult
     *
     * @param txId
     * @param executResult
     * @param errorCode
     * @param errorMsg
     * @return
     */
    int saveExecuteResultAndHeight(@Param("txId")String txId,@Param("executResult")String executResult,@Param("errorCode")String errorCode, @Param("errorMsg")String errorMsg, @Param("blockHeight")Long blockHeight);

    /**
     * update sign datas
     *
     * @param txId
     * @param signDatas
     * @return
     */
    int updateSignDatas(@Param("txId")String txId,@Param("signDatas")String signDatas);
}
