package com.higgs.trust.slave.core.repository.ca;

import com.higgs.trust.slave.dao.ca.CaDao;
import com.higgs.trust.slave.dao.po.ca.CaPO;
import com.higgs.trust.slave.model.bo.ca.Ca;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
        CaPO caPO = new CaPO();
        caPO.setUser(user);
        caPO = caDao.getCa(caPO);
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
}
