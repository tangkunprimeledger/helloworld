package com.higgs.trust.rs.core.api;

import com.higgs.trust.slave.api.vo.CaVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.config.Config;

/**
 * @desc TODO  
 * @author WangQuanzhou
 * @date 2018/6/5 15:40
 */  
public interface CaService {

    /** 
     * @desc generate pubKey and PriKey ,then insert into db
     * @param user
     * @return   
     */  
    void initKeyPair(String user);
    
    /** 
     * @desc construct ca tx and send to slave
     * @param
     * @return   
     */  
    RespData authCaTx(CaVO caVO);

    /** 
     * @desc update pubKey and PriKey ,then insert into db
     * @param user
     * @return
     */  
    void updateKeyPair(String user);

    /** 
     * @desc TODO 
     * @param
     * @return   
     */  
    RespData updateCaTx(CaVO caVO);

    void cancelKeyPair(String user);

    RespData cancelCaTx(CaVO caVO);

    /** 
     * @desc after ca tx has bean authoritied by the current cluster, then update table config column valid to TRUE
     * @param
     * @return   
     */  
    void callbackCa();
}
