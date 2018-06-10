package com.higgs.trust.slave.dao.manage;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.manage.RsNodePO;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/03/28
 * @desc rs node dao
 */
@Mapper public interface RsNodeDao extends BaseDao<RsNodePO> {
    /**
     * query rs_node by rsId
     *
     * @param rsId
     * @return
     */
    RsNodePO queryByRsId(String rsId);

    /**
     * query all rs_node
     *
     * @return
     */
    List<RsNodePO> queryAll();

    /**
     * query all rs and public key when status is 'COMMON'
     * @return
     */
    List<RsPubKey> queryRsAndPubKey();

    /**
     * batch insert
     *
     * @param rsNodePOList
     * @return
     */
    int batchInsert(List<RsNodePO> rsNodePOList);

    /**
     * batch update
     * @param rsNodePOList
     * @return
     */
    int batchUpdate(List<RsNodePO> rsNodePOList);

}
