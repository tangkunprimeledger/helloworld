package com.higgs.trust.slave.core.service.ca;

import com.higgs.trust.slave.api.vo.RespData;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 15:40
 */
public interface CaInitService {

    /**
     * @param
     * @return
     * @desc TODO
     */
    RespData<String> initStart();

    /**
     * @param
     * @return
     * @desc construct ca init tx and send to slave
     */
    RespData<String> initCaTx();

    void initKeyPair();

}
