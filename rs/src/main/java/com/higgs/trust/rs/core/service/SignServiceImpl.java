package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.slave.api.enums.TxTypeEnum;
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
    @Autowired private NodeState nodeState;


    @Override public SignInfo signTx(CoreTransaction coreTx) {
        String coreTxJSON = JSON.toJSONString(coreTx);
        if(log.isDebugEnabled()){
            log.debug("[signTx]txId:{},coreTxJSON:{}", coreTx.getTxId(), coreTxJSON);
        }
        SignInfo signInfo = new SignInfo();
        signInfo.setOwner(rsConfig.getRsName());
        //check tx type for consensus
        if(TxTypeEnum.isTargetType(coreTx.getTxType(),TxTypeEnum.NODE)){
            signInfo.setSignType(SignInfo.SignTypeEnum.CONSENSUS);
            String sign = CryptoUtil.getProtocolCrypto().sign(coreTxJSON, nodeState.getConsensusPrivateKey());
            signInfo.setSign(sign);
        }else {
            signInfo.setSignType(SignInfo.SignTypeEnum.BIZ);
            String sign = CryptoUtil.getBizCrypto().sign(coreTxJSON, nodeState.getPrivateKey());
            signInfo.setSign(sign);
        }
        return signInfo;
    }
}
