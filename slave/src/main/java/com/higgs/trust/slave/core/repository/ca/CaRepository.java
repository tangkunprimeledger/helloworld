package com.higgs.trust.slave.core.repository.ca;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.mysql.ca.CaDao;
import com.higgs.trust.slave.dao.po.ca.CaPO;
import com.higgs.trust.slave.dao.rocks.ca.CaRocksDao;
import com.higgs.trust.slave.model.bo.ca.Ca;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/6 11:32
 */
@Repository @Slf4j public class CaRepository {
    @Autowired private CaDao caDao;
    @Autowired private CaRocksDao caRocksDao;
    @Autowired private InitConfig initConfig;

    /**
     * @param ca
     * @return
     * @desc insert CA into db
     */
    public void insertCa(Ca ca) {
        CaPO caPO = new CaPO();
        BeanUtils.copyProperties(ca, caPO);
        if (initConfig.isUseMySQL()) {
            caDao.insertCa(caPO);
        } else {
            caRocksDao.save(caPO);
        }
    }

    /**
     * @param ca
     * @return
     * @desc update CA information
     */
    public void updateCa(Ca ca) {
        CaPO caPO = new CaPO();
        BeanUtils.copyProperties(ca, caPO);
        if (initConfig.isUseMySQL()) {
            caDao.updateCa(caPO);
        } else {
            caRocksDao.update(caPO);
        }
    }

    /**
     * @param user
     * @return CaPO
     * @desc get CA information by nodeName
     */
    public Ca getCaForBiz(String user) {
        CaPO caPO;
        if (initConfig.isUseMySQL()) {
            caPO = caDao.getCaForBiz(user);
        } else {
            caPO = caRocksDao.get(user + Constant.SPLIT_SLASH + "biz");
        }

        if (null == caPO) {
            return null;
        }
        Ca newCa = new Ca();
        BeanUtils.copyProperties(caPO, newCa);
        return newCa;
    }

    public List<Ca> getCaListByUsers(List<String> users, String usage) {
        if (CollectionUtils.isEmpty(users)) {
            return null;
        }

        List<String> key = new ArrayList<>(users.size());
        for (String user : users) {
            key.add(user + Constant.SPLIT_SLASH + usage);
        }

        List<CaPO> list = caRocksDao.getCaListByUsers(key);
        return BeanConvertor.convertList(list, Ca.class);

    }

    public List<Ca> getAllCa() {
        List<CaPO> list;
        if (initConfig.isUseMySQL()) {
            list = caDao.getAllCa();
        } else {
            list = caRocksDao.queryAll();
        }

        List<Ca> CaList = new LinkedList<>();
        for (CaPO caPO : list) {
            Ca ca = new Ca();
            BeanUtils.copyProperties(caPO, ca);
            CaList.add(ca);
        }
        return CaList;
    }

    /**
     * batch insert
     *
     * @param caPOList
     * @return
     */
    public boolean batchInsert(List<CaPO> caPOList) {
        int affectRows;
        if (initConfig.isUseMySQL()) {
            try {
                affectRows = caDao.batchInsert(caPOList);
            } catch (DuplicateKeyException e) {
                log.error("batch insert ca fail, because there is DuplicateKeyException for caPOList:", caPOList);
                throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
            }
        } else {
            affectRows = caRocksDao.batchInsert(caPOList);
        }
        return affectRows == caPOList.size();
    }

    /**
     * batch update
     *
     * @param caPOList
     * @return
     */
    public boolean batchUpdate(List<CaPO> caPOList) {
        if (initConfig.isUseMySQL()) {
            return caPOList.size() == caDao.batchUpdate(caPOList);
        }
        return caPOList.size() == caRocksDao.batchInsert(caPOList);
    }

    public Ca getCaForConsensus(String user){
        CaPO caPO;
        if (initConfig.isUseMySQL()) {
            caPO = caDao.getCaForConsensus(user);
        } else {
            caPO = caRocksDao.get(user + Constant.SPLIT_SLASH + "consensus");
        }

        if (caPO == null){
            return null;
        }
        Ca ca = new Ca();
        BeanUtils.copyProperties(caPO,ca);
        return ca;
    }
}
