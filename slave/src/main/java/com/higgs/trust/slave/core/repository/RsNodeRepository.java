package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.dao.mysql.manage.RsNodeDao;
import com.higgs.trust.slave.dao.po.manage.RsNodePO;
import com.higgs.trust.slave.dao.rocks.manage.RsNodeRocksDao;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsNode;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import com.higgs.trust.slave.model.enums.biz.RsNodeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/04/012
 * @desc rs node repository for business
 */
@Service @Slf4j public class RsNodeRepository {

    @Autowired private RsNodeDao rsNodeDao;

    @Autowired private RsNodeRocksDao rsNodeRocksDao;

    @Autowired private CaRepository caRepository;

    @Autowired private InitConfig initConfig;
    /**
     * query all rs and public key
     *
     * @return
     */
    public List<RsNode> queryAll() {
        List<RsNodePO> rsNodePOList;
        if (initConfig.isUseMySQL()) {
            rsNodePOList = rsNodeDao.queryAll();
        } else {
            rsNodePOList = rsNodeRocksDao.queryAll();
        }

        if (CollectionUtils.isEmpty(rsNodePOList)) {
            log.info("rs public key list is empty");
            return null;
        }

        List<RsNode> rsNodeList = new ArrayList<>();
        rsNodePOList.forEach(rsNodePO -> {
            rsNodeList.add(convertRsNodePOtoRsNode(rsNodePO));
        });

        return rsNodeList;
    }

    /**
     * query all rs id
     *
     * @return
     */
    public List<String> queryAllRsId() {
        List<RsNodePO> rsNodePOList = rsNodeDao.queryAll();
        if (CollectionUtils.isEmpty(rsNodePOList)) {
            log.info("rs public key list is empty");
            return null;
        }

        List<String> rsIds = new ArrayList<>();
        rsNodePOList.forEach(rsNodePO -> {
            rsIds.add(rsNodePO.getRsId());
        });

        return rsIds;
    }

    /**
     * query rsNode by rs id
     *
     * @param rsId
     * @return
     */
    public RsNode queryByRsId(String rsId) {

        RsNodePO rsNodePO;
        if (initConfig.isUseMySQL()) {
            rsNodePO = rsNodeDao.queryByRsId(rsId);
        } else {
            rsNodePO = rsNodeRocksDao.get(rsId);
        }

        return null == rsNodePO ? null : convertRsNodePOtoRsNode(rsNodePO);
    }

    public List<RsPubKey> queryRsAndPubKey() {
        if (initConfig.isUseMySQL()) {
            return rsNodeDao.queryRsAndPubKey();
        }
        return queryRsAndPubKeyFromRocks();
    }

    private List<RsPubKey> queryRsAndPubKeyFromRocks() {
        List<RsNodePO> rsNodePOList = rsNodeRocksDao.queryAll();
        if (CollectionUtils.isEmpty(rsNodePOList)) {
            return null;
        }

        List<String> rsIds = new ArrayList<>();
        for (RsNodePO po : rsNodePOList) {
            if (StringUtils.equals(RsNodeStatusEnum.COMMON.getCode(), po.getStatus())) {
                rsIds.add(po.getRsId());
            }
        }

        List<Ca> list = caRepository.getCaListByUsers(rsIds, "biz");
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        List<RsPubKey> rsPubKeys = new ArrayList<>();
        for (Ca ca : list) {
            RsPubKey rsPubKey = new RsPubKey();
            rsPubKey.setPubKey(ca.getPubKey());
            rsPubKey.setRsId(ca.getUser());
            rsPubKeys.add(rsPubKey);
        }
        return rsPubKeys;
    }

    public RsNode convertActionToRsNode(RegisterRS registerRS) {
        RsNode rsNode = new RsNode();
        rsNode.setRsId(registerRS.getRsId());
        rsNode.setDesc(registerRS.getDesc());
        rsNode.setStatus(RsNodeStatusEnum.COMMON);
        return rsNode;
    }

    public RsNode convertRsNodePOtoRsNode(RsNodePO rsNodePO) {
        RsNode rsNode = new RsNode();
        BeanUtils.copyProperties(rsNodePO, rsNode);
        rsNode.setStatus(RsNodeStatusEnum.getByCode(rsNodePO.getStatus()));
        return rsNode;
    }

    public RsNodePO convertRsNodeToRsNodePO(RsNode rsNode) {
        if (null == rsNode) {
            return null;
        }
        RsNodePO rsNodePO = new RsNodePO();
        rsNodePO.setStatus(rsNode.getStatus().getCode());
        BeanUtils.copyProperties(rsNode, rsNodePO);
        return rsNodePO;
    }

    public int batchUpdate(List<RsNodePO> rsNodePOList) {
        if (initConfig.isUseMySQL()) {
            return rsNodeDao.batchUpdate(rsNodePOList);
        }
        return rsNodeRocksDao.batchUpdate(rsNodePOList);
    }

    public int batchInsert(List<RsNodePO> rsNodePOList) {
        if (initConfig.isUseMySQL()) {
            return rsNodeDao.batchInsert(rsNodePOList);
        }
        return rsNodeRocksDao.batchInsert(rsNodePOList);
    }
}
