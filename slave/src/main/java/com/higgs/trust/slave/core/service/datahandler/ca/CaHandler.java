package com.higgs.trust.slave.core.service.datahandler.ca;

import com.higgs.trust.slave.dao.po.ca.CaPO;
import com.higgs.trust.slave.model.bo.ca.Ca;

/**
 * @author WangQuanzhou
 * @desc CA handler
 * @date 2018/6/6 10:33
 */
public interface CaHandler {

    /**
     * @param ca
     * @return
     * @desc insert CA into db
     */
    void authCa(Ca ca);

    /**
     * @param ca
     * @return
     * @desc update CA information
     */
    void updateCa(Ca ca);

    /**
     * @param ca
     * @return
     * @desc cancel CA information
     */
    void cancelCa(Ca ca);

    /**
     * @param nodeName
     * @return Ca
     * @desc get CA information by nodeName
     */
    CaPO getCa(String nodeName);
}
