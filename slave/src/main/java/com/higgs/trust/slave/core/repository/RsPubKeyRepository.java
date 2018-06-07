package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.dao.manage.RsPubKeyDao;
import com.higgs.trust.slave.dao.po.manage.RsPubKeyPO;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
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
 * @desc rs and public key repository for business
 */
@Service
@Slf4j
public class RsPubKeyRepository {

    @Autowired
    private RsPubKeyDao rsPubKeyDao;

    /**
     * query all rs and public key
     * @return
     */
    public List<RsPubKey> queryAll() {
        List<RsPubKeyPO> rsPubKeyPOList = rsPubKeyDao.queryAll();
        if (CollectionUtils.isEmpty(rsPubKeyPOList)) {
            log.info("rs public key list is empty");
            return null;
        }

        List<RsPubKey> rsPubKeyList = new ArrayList<>();
        rsPubKeyPOList.forEach(rsPubKeyPO -> {
            rsPubKeyList.add(convertRsPubKeyPOtoRsPubKey(rsPubKeyPO));
        });

        return rsPubKeyList;
    }

    /**
     * query all rs id
     * @return
     */
    public List<String> queryAllRsId() {
        List<RsPubKeyPO> rsPubKeyPOList = rsPubKeyDao.queryAll();
        if (CollectionUtils.isEmpty(rsPubKeyPOList)) {
            log.info("rs public key list is empty");
            return null;
        }

        List<String> rsIds = new ArrayList<>();
        rsPubKeyPOList.forEach(rsPubKeyPO -> {
            rsIds.add(rsPubKeyPO.getRsId());
        });

        return rsIds;
    }


    /**
     * query rsPubKey by rs id
     * @param rsId
     * @return
     */
    public RsPubKey queryByRsId(String rsId) {
        RsPubKeyPO rsPubKeyPO = rsPubKeyDao.queryByRsId(rsId);

        if (null == rsPubKeyPO) {
            return null;
        }

        return convertRsPubKeyPOtoRsPubKey(rsPubKeyPO);
    }

    /**
     * save rsPubKey
     * @param rsPubKey
     */
    public void save(RsPubKey rsPubKey) {
        if (null == rsPubKey) {
            log.error("rsPubKey is null");
            return;
        }
        rsPubKeyDao.add(convertRsPubKeyToRsPubKeyPO(rsPubKey));
    }

    public RsPubKey convertActionToRsPubKey(RegisterRS registerRS){
        RsPubKey rsPubKey = new RsPubKey();
        rsPubKey.setRsId(registerRS.getRsId());
        rsPubKey.setPubKey(registerRS.getPubKey());
        return rsPubKey;
    }

    private RsPubKey convertRsPubKeyPOtoRsPubKey(RsPubKeyPO rsPubKeyPO) {
        RsPubKey rsPubKey = new RsPubKey();
        BeanUtils.copyProperties(rsPubKeyPO, rsPubKey);
        return rsPubKey;
    }

    private RsPubKeyPO convertRsPubKeyToRsPubKeyPO(RsPubKey rsPubKey) {
        RsPubKeyPO rsPubKeyPO = new RsPubKeyPO();
        BeanUtils.copyProperties(rsPubKey, rsPubKeyPO);
        return rsPubKeyPO;
    }
}
