package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.dao.manage.RsNodeDao;
import com.higgs.trust.slave.dao.po.manage.RsNodePO;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsNode;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import com.higgs.trust.slave.model.enums.biz.RsNodeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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

    /**
     * query all rs and public key
     *
     * @return
     */
    public List<RsNode> queryAll() {
        List<RsNodePO> rsNodePOList = rsNodeDao.queryAll();
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
        RsNodePO rsNodePO = rsNodeDao.queryByRsId(rsId);

        if (null == rsNodePO) {
            return null;
        }

        return convertRsNodePOtoRsNode(rsNodePO);
    }

    public List<RsPubKey> queryRsAndPubKey() {
        return rsNodeDao.queryRsAndPubKey();
    }

    /**
     * save rs node
     *
     * @param rsNode
     */
    public void save(RsNode rsNode) {
        if (null == rsNode) {
            log.error("rs node is null");
            return;
        }
        rsNodeDao.add(convertRsNodeToRsNodePO(rsNode));
    }

    public RsNode convertActionToRsNode(RegisterRS registerRS) {
        RsNode rsNode = new RsNode();
        rsNode.setRsId(registerRS.getRsId());
        rsNode.setDesc(registerRS.getDesc());
        rsNode.setStatus(RsNodeStatusEnum.COMMON);
        return rsNode;
    }

    private RsNode convertRsNodePOtoRsNode(RsNodePO rsNodePO) {
        RsNode rsNode = new RsNode();
        BeanUtils.copyProperties(rsNodePO, rsNode);
        rsNode.setStatus(RsNodeStatusEnum.getByCode(rsNodePO.getStatus()));
        return rsNode;
    }

    private RsNodePO convertRsNodeToRsNodePO(RsNode rsNode) {
        RsNodePO rsNodePO = new RsNodePO();
        BeanUtils.copyProperties(rsNode, rsNodePO);
        return rsNodePO;
    }

    public int batchUpdate(List<RsNodePO> rsNodePOList) {
        return rsNodeDao.batchUpdate(rsNodePOList);
    }

    public int batchInsert(List<RsNodePO> rsNodePOList) {
        return rsNodeDao.batchInsert(rsNodePOList);
    }
}
