package com.higgs.trust.rs.custom.dao.identity;

import com.higgs.trust.rs.custom.dao.po.identity.IdentityRequestPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/*
 * @desc 存证临时表identity_request的相关操作
 * @author WangQuanzhou
 * @date 2018/3/5 11:18
 */
@Mapper
@Repository
public interface IdentityRequestDAO {


   /* 
    * @desc 插入
    * @param  IdentityRequest
    * @return   
    */  
    public void insertIdentityRequest(IdentityRequestPO identityRequestPO);

    /*
     * @desc 查询出临时的存证数据，用于异步下发
     * @param  String
     * @return IdentityRequestPO
     */
    public IdentityRequestPO queryIdentityRequest(String reqNo);
}
