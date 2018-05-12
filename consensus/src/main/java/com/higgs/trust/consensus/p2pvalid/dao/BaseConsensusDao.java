package com.higgs.trust.consensus.p2pvalid.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description:
 * @author: cwy
 **/
interface BaseConsensusDao<T> {

    int add(T t);

    void deleteByMessageDigest(@Param("messageDigest") String messageDigest);

    void deleteByMessageDigestList(@Param("messageDigestList") List<String> messageDigestList);
}
