package com.higgs.trust.slave.core.service.ca;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.config.Config;

import java.util.List;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 15:40
 */
public interface CaInitService {

    /**
     * @param
     * @return
     * @desc construct ca init tx and send to slave
     */
    RespData<List<Config>> initCaTx();

    void initKeyPair();

}
