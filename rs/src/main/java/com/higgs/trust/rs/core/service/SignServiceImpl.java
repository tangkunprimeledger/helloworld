package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.OkHttpClientManager;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.rs.core.integration.ServiceProviderClient;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
@Service @Slf4j public class SignServiceImpl implements SignService {
    @Autowired private RsConfig rsConfig;
    @Autowired private ServiceProviderClient serviceProviderClient;

    @Override public String requestSign(String rsName, CoreTransaction coreTx) {
        log.info("[requestSign]rsName:{}",rsName);
        log.info("[requestSign]coreTx:{}",coreTx);
        boolean useHttpChannel = rsConfig.isUseHttpChannel();
        log.info("[requestSign]useHttpChannel:{}",useHttpChannel);
        if(useHttpChannel){
            return signTxByHttp(rsName,coreTx);
        }
        RespData<String> respData = serviceProviderClient.signTx(rsName,coreTx);
        log.info("[requestSign]respData:{}",respData);
        if(respData.isSuccess()){
            return respData.getData();
        }
        return null;
    }

    /**
     * sign tx by http channel
     *
     * @param rsName
     * @param coreTx
     * @return
     */
    private String signTxByHttp(String rsName, CoreTransaction coreTx){
        String url = "http://" + rsName + ":" + rsConfig.getServerPort() + "/signTx";
        log.info("[signTxByHttp]url:" + url);
        String paramJSON = JSON.toJSONString(coreTx);
        log.info("[signTxByHttp]paramJSON:" + paramJSON);
        try {
            String resultJSON = OkHttpClientManager.postAsString(url,paramJSON,10000L);
            log.info("[signTxByHttp]resultJSON:" + resultJSON);
            if(StringUtils.isEmpty(resultJSON)){
                return null;
            }
            RespData respData = JSON.parseObject(resultJSON,RespData.class);
            if(!respData.isSuccess()){
                return null;
            }
            return String.valueOf(respData.getData());
        } catch (Exception e) {
            log.error("[signTxByHttp] has error",e);
        }
        return null;
    }

    @Override public RespData<String> signTx(CoreTransaction coreTx) {
        RespData<String> respData = new RespData<>();
        if (coreTx == null) {
            log.info("[signTx]coreTxt is null");
            respData.setCode(RsCoreErrorEnum.RS_CORE_TX_VERIFY_SIGNATURE_FAILED.getCode());
            return respData;
        }
        String coreTxJSON = JSON.toJSONString(coreTx);
        log.info("[signTx]coreTxJSON:{}", coreTxJSON);
        String privateKey = rsConfig.getPrivateKey();
        log.debug("[signTx]privateKey:{}", privateKey);
        String sign = SignUtils.sign(coreTxJSON, privateKey);
        log.info("[signTx]sign:{}", sign);
        respData.setData(sign);
        return respData;
    }
}
