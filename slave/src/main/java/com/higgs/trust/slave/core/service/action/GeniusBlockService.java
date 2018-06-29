package com.higgs.trust.slave.core.service.action;

import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.core.repository.config.ClusterConfigRepository;
import com.higgs.trust.slave.core.repository.config.ClusterNodeRepository;
import com.higgs.trust.slave.core.service.action.ca.CaInitHandler;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.ca.CaAction;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Slf4j @Service public class GeniusBlockService {

    @Autowired ClusterConfigRepository clusterConfigRepository;
    @Autowired ClusterNodeRepository clusterNodeRepository;
    @Autowired BlockRepository blockRepository;
    @Autowired TransactionTemplate txRequired;
    @Autowired CaRepository caRepository;
    @Autowired CaInitHandler caInitHandler;

    public void generateGeniusBlock(Block block) {
        try {

            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus status) {

                    log.info("[process] transaction start, insert genius block into db");
                    blockRepository.saveBlock(block, null);

                    log.info("[process]insert clusterNode information into db");
                    saveClusterNode(block);

                    log.info("[process]insert clusterConfig information into db");
                    saveClusterConfig(block);

                    log.info("[process]insert ca information into db");
                    saveCa(block);
                }
            });
        } catch (Throwable e) {
            log.error("[process] store ca init data error", e);
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_INIT_ERROR, "[process] store ca init data error", e);
        }
    }

    private void saveClusterNode(Block block) {
        List<Action> caList = acquireAction(block);
        caList.forEach((caAction) -> {
            ClusterNode clusterNode = new ClusterNode();
            clusterNode.setNodeName(((CaAction)caAction).getUser());
            clusterNode.setRsStatus(false);
            clusterNode.setP2pStatus(true);
            clusterNodeRepository.insertClusterNode(clusterNode);
        });
    }

    private void saveClusterConfig(Block block) {
        List<Action> caList = acquireAction(block);
        ClusterConfig clusterConfig = new ClusterConfig();
        clusterConfig.setNodeNum(caList.size());

        // TODO 应该从配置文件读取集群名字
        clusterConfig.setClusterName("TRUST");

        clusterConfig.setFaultNum((caList.size() - 1) / 3);
        clusterConfigRepository.insertClusterConfig(clusterConfig);
    }

    private void saveCa(Block block) {

        List list = new LinkedList();
        List<Action> caList = acquireAction(block);
        caList.forEach((caAction) -> {
            Ca ca = new Ca();
            ca.setPeriod(calculatePeriod());
            ca.setPubKey(((CaAction)caAction).getPubKey());
            ca.setValid(true);
            ca.setUser(((CaAction)caAction).getUser());
            ca.setVersion(VersionEnum.V1.getCode());
            ca.setUsage("consensus");
            log.info("[CaInitHandler.saveCa] nodeName={}, pubKey={}, period={}", ca.getUser(), ca.getPubKey(),
                ca.getPeriod());
            list.add(ca);
        });

        log.info("[CaInitHandler.saveCa] start to insert ca information");
        caRepository.batchInsert(list);
        log.info("[CaInitHandler.saveCa] end insert ca information");
    }

    private Date calculatePeriod() {
        Calendar calendar = Calendar.getInstance();
        // default 1 year later
        calendar.add(Calendar.YEAR, 1);
        return calendar.getTime();
    }

    private List<Action> acquireAction(Block block) {
        List<SignedTransaction> signedTransactionList = block.getSignedTxList();
        CoreTransaction coreTransaction = signedTransactionList.get(0).getCoreTx();
        return coreTransaction.getActionList();
    }
}
