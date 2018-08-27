package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.common.utils.CryptoUtil;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.config.Config;
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
    @Autowired private ConfigRepository configRepository;


    @Override public SignInfo signTx(CoreTransaction coreTx) {
        String coreTxJSON = JSON.toJSONString(coreTx);
        log.debug("[signTx]txId:{},coreTxJSON:{}", coreTx.getTxId(), coreTxJSON);
        Config config = configRepository.getBizConfig(rsConfig.getRsName());
        if (config == null) {
            log.error("[signTx]get config is null rsName:{}", rsConfig.getRsName());
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_GET_RS_CONFIG_NULL_ERROR);
        }
        String privateKey = config.getPriKey();
        log.debug("[signTx]priKeyForBiz:{}", privateKey);
        String sign = CryptoUtil.getBizCrypto().sign(coreTxJSON, privateKey);
        log.debug("[signTx]sign:{}", sign);
        SignInfo signInfo = new SignInfo();
        signInfo.setOwner(rsConfig.getRsName());
        signInfo.setSign(sign);
        return signInfo;
    }
}
