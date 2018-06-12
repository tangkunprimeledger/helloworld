package com.higgs.trust.slave.core.service.action.ca;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.HashUtil;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.config.ClusterConfigRepository;
import com.higgs.trust.slave.core.repository.config.ClusterNodeRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.StateRootHash;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

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

    /**
     * 使用固定算法生成CA
     * 1、生成创世快
     * 2、更新集群节点信息
     * 3、更新节点配置信息
     *
     * @param map
     */
    public void process(Map map) {
        try {
            Block block = generateGeniusBlock(map);
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus status) {

                    log.info("[process] transaction start，insert genius block into db");
                    blockRepository.saveBlock(block, null);

                    log.info("[process]insert clusterNode information into db");
                    saveClusterNode(map);

                    log.info("[process]insert clusterConfig information into db");
                    saveClusterConfig(map);
                }
            });
        } catch (Throwable e) {
            log.error("[process] store ca init data error", e);
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_INIT_ERROR, "[process] store ca init data error");
        }
    }

    private Block generateGeniusBlock(Map map) {
        Block block = new Block();
        BlockHeader blockHeader = new BlockHeader();
        StateRootHash stateRootHash = new StateRootHash();

        stateRootHash.setCaRootHash(HashUtil.getSHA256S(JSON.toJSONString(map)));
        blockHeader.setStateRootHash(stateRootHash);
        blockHeader.setHeight(1L);
        blockHeader.setPreviousHash(null);
        blockHeader.setVersion(VersionEnum.V1.getCode());
        blockHeader.setBlockTime(1L);
        blockHeader.setBlockHash(blockService.buildBlockHash(blockHeader));
        block.setGenesis(true);
        block.setBlockHeader(blockHeader);

        return block;
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
}
