package com.higgs.trust.slave.core.managment;

import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.common.crypto.KeyPair;
import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.common.enums.KeyModeEnum;
import com.higgs.trust.slave.common.enums.RunModeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.core.service.ca.CaInitService;
import com.higgs.trust.slave.model.bo.config.Config;
import com.higgs.trust.slave.model.enums.UsageEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author WangQuanzhou
 * @desc cluster init service
 * @date 2018/6/28 19:53
 */
@Service @Slf4j public class ClusterInitService {

    @Autowired private BlockRepository blockRepository;

    @Autowired private CaInitService caInitService;

    @Autowired private ConfigRepository configRepository;

    @Autowired private NodeState nodeState;

    @Autowired private ClusterInfo clusterInfo;

    @Autowired private TransactionTemplate txRequired;

    @Value("${trust.start.mode:cluster}") private String startMode;

    @Value("${trust.key.mode:auto}") private String keyMode;

    // public key for manual mode
    @Value("${higgs.trust.publicKey}") String publicKey;

    // private key for manual mode
    @Value("${higgs.trust.privateKey}") String privateKey;

    @StateChangeListener(NodeStateEnum.SelfChecking) @Order(Ordered.HIGHEST_PRECEDENCE) public void init() {
        if (needInit()) {
            // 1、生成公私钥,存入db
            // 2、获取其他节点的公钥,公钥写入配置文件给共识层使用
            // 3、使用所有的公钥及节点名生成创世块
            caInitService.initKeyPair();
            log.info("[ClusterInitService.init] end initKeyPair");
        }
        clusterInfo.refresh();
        clusterInfo.refreshConsensus();
    }

    private boolean needInit() {
        // 1、 本地没有创世块，集群也没有创世块时（即集群初始启动），需要生成公私钥，以及创世块
        // 2、 本地没有创世块，集群有创世块时（即动态单节点加入），需要进行failover得到创世块
        Long maxHeight = blockRepository.getMaxHeight();
        if (null == maxHeight && startMode.equals(RunModeEnum.CLUSTER.getCode())) {
            log.info("[ClusterInitService.needInit] start generateKeyPair, cluster mode");
            generateKeyPair();
            return true;
        }
        return false;
    }

    private void generateKeyPair() {

        if (null != configRepository.getConfig(new Config(nodeState.getNodeName()))) {
            log.info("[ClusterInitService.generateKeyPair] pubKey/priKey already exist in table config");
            return;
        }

        // auto generate keyPair for cluster
        if (keyMode.equals(KeyModeEnum.AUTO.getCode())) {
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    // generate keyPair for consensus layer
                    Crypto consensusCrypto = CryptoUtil.getProtocolCrypto();
                    KeyPair keyPair = consensusCrypto.generateKeyPair();
                    saveConfig(keyPair.getPubKey(), keyPair.getPriKey(), UsageEnum.CONSENSUS);

                    // generate keyPair for biz layer
                    Crypto bizCrypto = CryptoUtil.getBizCrypto();
                    keyPair = bizCrypto.generateKeyPair();
                    saveConfig(keyPair.getPubKey(), keyPair.getPriKey(), UsageEnum.BIZ);
                }
            });
        } else if (keyMode.equals(KeyModeEnum.MANUAL.getCode())) {
            // load keyPair for cluster from json file
            saveConfig(publicKey, privateKey, UsageEnum.CONSENSUS);
            saveConfig(publicKey, privateKey, UsageEnum.BIZ);
        } else {
            throw new SlaveException(SlaveErrorEnum.SLAVE_CONFIGURATION_ERROR, "keyMode is invalid");
        }
    }

    private void saveConfig(String pubKey, String priKey, UsageEnum usage) {
        Config config = new Config();
        config.setValid(true);
        config.setPubKey(pubKey);
        config.setPriKey(priKey);
        config.setVersion(VersionEnum.V1.getCode());
        config.setNodeName(nodeState.getNodeName());
        config.setUsage(usage.getCode());
        configRepository.insertConfig(config);
    }

}
