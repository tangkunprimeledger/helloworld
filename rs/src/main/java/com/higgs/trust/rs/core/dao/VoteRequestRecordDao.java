package com.higgs.trust.rs.core.dao;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.rs.core.dao.po.VoteRequestRecordPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper public interface VoteRequestRecordDao extends BaseDao<VoteRequestRecordPO> {

    /**
     * query by transaction id
     *
     * @param txId
     * @return
     */
    VoteRequestRecordPO queryByTxId(@Param("txId") String txId);

    /**
     * set vote result,from INIT to AGREE or DISAGREE
     *
     * @param txId
     * @param sign
     * @param voteResult
     * @return
     */
    int setVoteResult(@Param("txId") String txId,@Param("sign")String sign,@Param("voteResult") String voteResult);
}
