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
    void authKeyPair(String user);
    
    /** 
     * @desc construct ca auth tx and send to slave
     * @param
     * @return   
     */  
    RespData authCaTx(CaVO caVO);

    /** 
     * @desc update pubKey and priKey ,then insert into db
     * @param user
     * @return
     */  
    void updateKeyPair(String user);

    /** 
     * @desc construct ca update tx and send to slave
     * @param
     * @return   
     */  
    RespData updateCaTx(CaVO caVO);

    /**
     * @desc  cancel pubKey and PriKey ,then update db
     * @param
     * @return
     */
    void cancelKeyPair(String user);

    /** 
     * @desc construct ca cancel tx and send to slave
     * @param
     * @return   
     */  
    RespData cancelCaTx(CaVO caVO);

    /**
     * @desc cluster init start, init cluster ca infor
     * @param
     * @return
     */
    void initKeyPair();

    /** 
     * @desc construct ca init tx and send to slave
     * @param
     * @return   
     */  
    RespData initCaTx();

    /** 
     * @desc after ca tx has bean authoritied by the current cluster, then update table config column valid to TRUE
     * @param
     * @return   
     */  
    void callbackCa();
}
