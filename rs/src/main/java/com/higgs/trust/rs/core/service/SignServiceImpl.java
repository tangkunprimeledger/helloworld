package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
@Service @Slf4j public class SignServiceImpl implements SignService {
    @Autowired private RsConfig rsConfig;

    @Override public SignInfo signTx(CoreTransaction coreTx) {
        String coreTxJSON = JSON.toJSONString(coreTx);
        log.info("[signTx]coreTxJSON:{}", coreTxJSON);
        String privateKey = rsConfig.getPrivateKey();
        log.debug("[signTx]privateKey:{}", privateKey);
        String sign = SignUtils.sign(coreTxJSON, privateKey);
        log.info("[signTx]sign:{}", sign);
        SignInfo signInfo = new SignInfo();
        signInfo.setOwner(rsConfig.getRsName());
        signInfo.setSign(sign);
        return signInfo;
    }
}
