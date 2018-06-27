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
     * query by status rowNum and count
     *
     * @param status
     * @param rowNum
     * @param count
     * @return
     */
    List<CoreTransactionPO> queryByStatus(@Param("status")String status,@Param("rowNum")int rowNum,@Param("count")int count);

    /**
     * update status by form->to
     * @param txId
     * @param from
     * @param to
     * @return
     */
    int updateStatus(@Param("txId")String txId,@Param("from")String from,@Param("to")String to);

    /**
     * save tx execute reslult
     *
     * @param txId
     * @param executResult
     * @param errorCode
     * @param errorMsg
     * @return
     */
    int saveExecuteResult(@Param("txId")String txId,@Param("executResult")String executResult,@Param("errorCode")String errorCode,@Param("errorMsg")String errorMsg);

    /**
     * update sign datas
     *
     * @param txId
     * @param signDatas
     * @return
     */
    int updateSignDatas(@Param("txId")String txId,@Param("signDatas")String signDatas);
}
