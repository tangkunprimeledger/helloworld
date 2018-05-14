package com.higgs.trust.rs.custom.dao;

import com.higgs.trust.rs.custom.dao.po.BankChainRequestPO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * @desc 存证request的持久化以及存证临时表identity_request
 * @author WangQuanzhou
 * @date 2018/3/5 11:18
 */
@Mapper
@Repository
public interface BankChainRequestDAO {

    
    /* 
     * @desc 插入所有接收到的Request请求
     * @param BankChainRequestPO
     * @return
     */  
    public void insertRequest(BankChainRequestPO bankChainRequestPO);
    
    /* 
     * @desc 查询出20条状态是INIT的数据
     * @param
     * @return   BankChainRequestPO
     */  
    public List<BankChainRequestPO> queryRequest();

    /* 
     * @desc 根据reqNo查询出request的状态
     * @param   String
     * @return   BankChainRequestPO
     */  
    public BankChainRequestPO queryRequestByReqNo(String reqNo);

    /* 
     * @desc 根据reqNo将request的状态从INIT更改为PROCESSING
     * @param   String
     * @return   int
     */  
    public int updateRequestToProc(String reqNo);


    /*
     * @desc 对于重复的key且flag=999，即不需要覆盖的情形，直接将状态修改为DUPLICATE
     * @param   String
     * @return   int
     */
    public int updateRequestToDuplicate(String reqNo);


    /*
     * @desc 根据reqNo将request的状态从PROCESSING更改为slave返回的状态
     * @param   String
     * @return   int
     */
    public int updateRequest(BankChainRequestPO bankChainRequestPO);


}
