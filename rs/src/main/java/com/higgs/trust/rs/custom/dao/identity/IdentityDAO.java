package com.higgs.trust.rs.custom.dao.identity;

import com.higgs.trust.rs.custom.dao.po.identity.IdentityPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


/*  
 * @desc 存证接口
 * @author WangQuanzhou
 * @date 2018/3/2 19:32
 */  
@Mapper
@Repository
public interface IdentityDAO {

    /* 
     * @desc 插入存证数据
     * @param  IdentityPO
     * @return   
     */  
    public void insertIdentity(IdentityPO identityPO);

    /* 
     * @desc 根据key查询对应的value
     * @param String key
     * @return   IdentityPO
     */  
    public IdentityPO queryIdentityByKey(String key);

    /*
     * @desc 根据key查询对应的value
     * @param String
     * @return   IdentityPO
     */
    public IdentityPO queryIdentityByReqNo(String reqNo);
}
