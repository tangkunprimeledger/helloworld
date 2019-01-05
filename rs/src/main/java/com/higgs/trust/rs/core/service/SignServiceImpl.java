package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.slave.api.enums.TxTypeEnum;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.config.Config;
import com.higgs.trust.slave.model.enums.UsageEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
@Service
@Slf4j
public class SignServiceImpl implements SignService {
    @Autowired
    private RsConfig rsConfig;
    @Autowired
    private NodeState nodeState;

    @Override
    public SignInfo signTx(CoreTransaction coreTx) {
        String coreTxJSON = JSON.toJSONString(coreTx);
        if (log.isDebugEnabled()) {
            log.debug("[signTx]txId:{},coreTxJSON:{}", coreTx.getTxId(), coreTxJSON);
        }
        SignInfo signInfo = new SignInfo();
        signInfo.setOwner(rsConfig.getRsName());
        //check tx type for consensus
        if (TxTypeEnum.isTargetType(coreTx.getTxType(), TxTypeEnum.NODE)) {
            signInfo.setSignType(SignInfo.SignTypeEnum.CONSENSUS);
        } else {
            signInfo.setSignType(SignInfo.SignTypeEnum.BIZ);
        }
        signInfo.setSign(sign(coreTxJSON, signInfo.getSignType()));
        return signInfo;
    }

    @Override
    public String sign(String signValue, SignInfo.SignTypeEnum signTypeEnum) {
        if (signTypeEnum == SignInfo.SignTypeEnum.CONSENSUS) {
            return CryptoUtil.getProtocolCrypto().sign(signValue, nodeState.getConsensusPrivateKey());
        } else {
            return CryptoUtil.getBizCrypto(null).sign(signValue, nodeState.getPrivateKey());
        }
    }
}
