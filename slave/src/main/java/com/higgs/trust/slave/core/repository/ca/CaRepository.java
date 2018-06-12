package com.higgs.trust.slave.core.repository.ca;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.ca.CaDao;
import com.higgs.trust.slave.dao.po.ca.CaPO;
import com.higgs.trust.slave.model.bo.ca.Ca;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/6 11:32
 */
@Repository @Slf4j public class CaRepository {
    @Autowired private CaDao caDao;

    /**
     * @param ca
     * @return
     * @desc insert CA into db
     */
    public void insertCa(Ca ca) {
        CaPO caPO = new CaPO();
        BeanUtils.copyProperties(ca, caPO);
        caDao.insertCa(caPO);
    }

    /**
     * @param ca
     * @return
     * @desc update CA information
     */
    public void updateCa(Ca ca) {
        CaPO caPO = new CaPO();
        BeanUtils.copyProperties(ca, caPO);
        caDao.updateCa(caPO);
    }

    /**
     * @param user
     * @return CaPO
     * @desc get CA information by nodeName
     */
    public Ca getCa(String user) {
        CaPO caPO = caDao.getCa(user);
        if (null == caPO) {
            return null;
        }
        Ca newCa = new Ca();
        BeanUtils.copyProperties(caPO, newCa);
        return newCa;
    }

    public List<Ca> getAllCa() {
        List<CaPO> list = caDao.getAllCa();
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
        int affectRows = 0;
        try {
            affectRows = caDao.batchInsert(caPOList);
        } catch (DuplicateKeyException e) {
            log.error("batch insert ca fail, because there is DuplicateKeyException for caPOList:", caPOList);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
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
        return caPOList.size() == caDao.batchUpdate(caPOList);
    }
}
