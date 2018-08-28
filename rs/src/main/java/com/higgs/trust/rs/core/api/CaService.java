package com.higgs.trust.rs.core.api;

import com.higgs.trust.slave.api.vo.CaVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.ca.Ca;

import java.util.List;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 15:40
 */
public interface CaService {

    /**
     * @param user
     * @return
     * @desc generate pubKey and PriKey ,then insert into db
     */
    String authKeyPair(String user);

    /**
     * @param
     * @return
     * @desc construct ca auth tx and send to slave
     */
    RespData authCaTx(List<CaVO> list);

    /**
     * @param user
     * @return
     * @desc update pubKey and priKey ,then insert into db
     */
    RespData updateKeyPair(String user);

    /**
     * @param
     * @return
     * @desc construct ca update tx and send to slave
     */
    RespData updateCaTx(CaVO caVO);

    /**
     * @param
     * @return
     * @desc cancel pubKey and PriKey ,then update db
     */
    RespData cancelKeyPair(String user);

    /**
     * @param
     * @return
     * @desc construct ca cancel tx and send to slave
     */
    RespData cancelCaTx(CaVO caVO);

    /**
     * @param
     * @return
     * @desc after ca tx has bean authoritied by the current cluster, then update table config column valid to TRUE
     */
    void callbackCa();

    /**
     * @param user
     * @return
     * @desc acquire CA information by user
     */
    RespData<Ca> acquireCA(String user);

    /**
     * @param user
     * @return
     * @desc TODO
     */
    Ca getCa(String user);

}
