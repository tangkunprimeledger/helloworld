package com.higgs.trust.slave.dao.manage;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.manage.RsPubKeyPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/03/28
 * @desc rsPubKey dao
 */
@Mapper public interface RsPubKeyDao extends BaseDao<RsPubKeyPO> {
    /**
     * query rs_pub_key by rsId
     *
     * @param rsId
     * @return
     */
    RsPubKeyPO queryByRsId(String rsId);

    /**
     * query all rs_pub_key
     *
     * @return
     */
    List<RsPubKeyPO> queryAll();
}
