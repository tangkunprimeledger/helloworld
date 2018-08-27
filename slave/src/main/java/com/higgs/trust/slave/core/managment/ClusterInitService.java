package com.higgs.trust.slave.core.managment;

import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.common.crypto.KeyPair;
import com.higgs.trust.common.utils.CryptoUtil;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.common.enums.RunModeEnum;
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

        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                // generate keyPair for consensus layer
                Crypto consensusCrypto = CryptoUtil.getProtocolCrypto();
                KeyPair keyPair = consensusCrypto.generateKeyPair();
                String pubKey = keyPair.getPubKey();
                String priKey = keyPair.getPriKey();
                Config config = new Config();
                config.setValid(true);
                config.setPubKey(pubKey);
                config.setPriKey(priKey);
                config.setVersion(VersionEnum.V1.getCode());
                config.setNodeName(nodeState.getNodeName());
                config.setUsage(UsageEnum.CONSENSUS.getCode());
                configRepository.insertConfig(config);

                // generate keyPair for biz layer
                Crypto bizCrypto = CryptoUtil.getBizCrypto();
                keyPair = bizCrypto.generateKeyPair();
                pubKey = keyPair.getPubKey();
                priKey = keyPair.getPriKey();
                config = new Config();
                config.setValid(true);
                config.setPubKey(pubKey);
                config.setPriKey(priKey);
                config.setVersion(VersionEnum.V1.getCode());
                config.setNodeName(nodeState.getNodeName());
                config.setUsage(UsageEnum.BIZ.getCode());
                configRepository.insertConfig(config);
            }
        });

    }
}
