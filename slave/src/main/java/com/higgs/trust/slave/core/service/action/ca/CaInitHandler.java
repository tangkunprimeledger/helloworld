package com.higgs.trust.slave.core.service.action.ca;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.HashUtil;
import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.core.repository.config.ClusterConfigRepository;
import com.higgs.trust.slave.core.repository.config.ClusterNodeRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.dao.block.BlockDao;
import com.higgs.trust.slave.dao.po.block.BlockPO;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.StateRootHash;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author WangQuanzhou
 * @desc init ca handler
 * @date 2018/6/6 10:25
 */
@Slf4j @Component public class CaInitHandler {

    @Autowired ClusterConfigRepository clusterConfigRepository;
    @Autowired ClusterNodeRepository clusterNodeRepository;
    @Autowired BlockRepository blockRepository;
    @Autowired TransactionTemplate txRequired;
    @Autowired BlockService blockService;
    @Autowired BlockDao blockDao;
    @Autowired NodeState nodeState;
    @Autowired CaRepository caRepository;

    /**
     * 使用固定算法生成CA
     * 1、生成创世快
     * 2、更新集群节点信息
     * 3、更新节点配置信息
     * 4、生成CA信息插入db
     *
     * @param map
     */
    public void process(Map map) {
        try {
            BlockPO blockPO = generateGeniusBlock(map);
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus status) {

                    log.info("[process] transaction start，insert genius block into db");
                    blockDao.add(blockPO);

                    log.info("[process]insert clusterNode information into db");
                    saveClusterNode(map);

                    log.info("[process]insert clusterConfig information into db");
                    saveClusterConfig(map);

                    log.info("[process]insert ca information into db");
                    saveCa(map);
                }
            });
        } catch (Throwable e) {
            log.error("[process] store ca init data error", e);
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_INIT_ERROR, "[process] store ca init data error");
        }
    }

    private BlockPO generateGeniusBlock(Map<String, String> map) {

        BlockHeader blockHeader = new BlockHeader();

        StateRootHash stateRootHash = new StateRootHash();
        stateRootHash.setCaRootHash(HashUtil.getSHA256S(JSON.toJSONString(map)));
        stateRootHash.setAccountRootHash("NO_TREE");
        stateRootHash.setContractRootHash("NO_TREE");
        stateRootHash.setPolicyRootHash("NO_TREE");
        stateRootHash.setRsRootHash("NO_TREE");
        stateRootHash.setTxReceiptRootHash("NO_TREE");
        stateRootHash.setTxRootHash("NO_TREE");
        blockHeader.setStateRootHash(stateRootHash);

        blockHeader.setHeight(1L);
        blockHeader.setPreviousHash("IS_NULL");
        blockHeader.setVersion(VersionEnum.V1.getCode());
        blockHeader.setBlockTime(generateBlockTime().getTime());
        //        blockHeader.setBlockHash(blockService.buildBlockHash(blockHeader));

        BlockPO blockPO = new BlockPO();
        blockPO.setCaRootHash(HashUtil.getSHA256S(JSON.toJSONString(map)));
        blockPO.setAccountRootHash("NO_TREE");
        blockPO.setContractRootHash("NO_TREE");
        blockPO.setPolicyRootHash("NO_TREE");
        blockPO.setRsRootHash("NO_TREE");
        blockPO.setTxReceiptRootHash("NO_TREE");
        blockPO.setTxRootHash("NO_TREE");

        blockPO.setBlockHash(blockService.buildBlockHash(blockHeader));
        blockPO.setBlockTime(generateBlockTime());
        blockPO.setHeight(1L);
        blockPO.setVersion(VersionEnum.V1.getCode());
        blockPO.setTxNum(0);
        blockPO.setPreviousHash("IS_NULL");

        return blockPO;
    }

    private Date generateBlockTime() {
        String string = "2018-06-06 20:00:00";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(string);
        } catch (ParseException e) {
            log.error("[CaInitHandler.generateBlockTime] generate block time error");
        }
        return null;
    }

    private void saveClusterNode(Map<String, String> map) {
        for (String key : map.keySet()) {
            map.get(key);
            ClusterNode clusterNode = new ClusterNode();
            clusterNode.setNodeName(key);
            clusterNode.setRsStatus(false);
            clusterNode.setP2pStatus(true);
            clusterNodeRepository.insertClusterNode(clusterNode);
        }
    }

    private void saveClusterConfig(Map map) {
        ClusterConfig clusterConfig = new ClusterConfig();
        clusterConfig.setNodeNum(map.size());

        // TODO 应该从配置文件读取集群名字
        clusterConfig.setClusterName("TRUST");

        clusterConfig.setFaultNum((map.size() - 1) / 3);
        clusterConfigRepository.insertClusterConfig(clusterConfig);
    }

    private void saveCa(Map<String, String> map) {

        List list = new LinkedList();

        for (String key : map.keySet()) {
            Ca ca = new Ca();
            ca.setPeriod(calculatePeriod());
            ca.setPubKey(map.get(key));
            ca.setValid(true);
            ca.setUser(key);
            ca.setVersion(VersionEnum.V1.getCode());
            ca.setUsage("consensus");
            log.info("[CaInitHandler.saveCa] nodeName={}, pubKey={}, period={}", key, map.get(key), ca.getPeriod());
            list.add(ca);
        }

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
}
