package com.higgs.trust.slave.core.service.ca;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.config.Config;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 15:40
 */
public interface CaInitService {

    void initKeyPair() throws FileNotFoundException;

}
