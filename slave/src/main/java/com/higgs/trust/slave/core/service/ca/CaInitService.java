package com.higgs.trust.slave.core.service.ca;

import java.io.FileNotFoundException;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 15:40
 */
public interface CaInitService {

    void initKeyPair() throws FileNotFoundException;

}
