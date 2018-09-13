package com.higgs.trust.presstest.service;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.presstest.AppConst;
import com.higgs.trust.presstest.vo.StoreVO;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.core.api.RsCoreFacade;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.testng.collections.Lists;

/**
 * @author liuyu
 * @description
 * @date 2018-09-11
 */
@Service @Slf4j public class StoreService {
    @Autowired RsCoreFacade rsCoreFacade;
    @Autowired RsConfig rsConfig;

    /**
     * 存正交易
     *
     * @param vo
     */
    public RespData store(StoreVO vo) {
        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setTxId(vo.getReqNo());
        coreTransaction.setPolicyId(AppConst.STORE);
        JSONObject bizData = new JSONObject();
        bizData.put("biz",vo);
        coreTransaction.setBizModel(bizData);
        coreTransaction.setActionList(Lists.newArrayList());
        coreTransaction.setSender(rsConfig.getRsName());
        coreTransaction.setVersion(VersionEnum.V1.getCode());

        rsCoreFacade.processTx(coreTransaction);
        return rsCoreFacade.syncWait(vo.getReqNo(), true);
    }
}
