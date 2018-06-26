package com.higgs.trust.slave.dao.ca;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.slave.dao.po.ca.CaPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author WangQuanzhou
 * @desc CA dao
 * @date 2018/6/5 10:18
 */
@Mapper public interface CaDao extends BaseDao {

    /**
     * @param caPO
     * @return
     * @desc insert CA into db
     */
    void insertCa(CaPO caPO);

    /**
     * @param caPO
     * @return
     * @desc update CA information
     */
    void updateCa(CaPO caPO);

    /**
     * @param user
     * @return CaPO
     * @desc get CA information by nodeName
     */
    CaPO getCa(String user);

    /**
     * @param
     * @return
     * @desc get all CA information
     */
    List<CaPO> getAllCa();

    /**
     * batch insert
     *
     * @param caPOList
     * @return
     */
    int batchInsert(List<CaPO> caPOList);

    /**
     * batch update
     *
     * @param caPOList
     * @return
     */
    int batchUpdate(List<CaPO> caPOList);

}
