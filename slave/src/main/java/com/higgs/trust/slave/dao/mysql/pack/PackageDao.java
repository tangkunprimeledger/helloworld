package com.higgs.trust.slave.dao.mysql.pack;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.slave.dao.po.pack.PackagePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper public interface PackageDao extends BaseDao<PackagePO> {

    /**
     * query package by block height
     *
     * @param height
     * @return
     */
    PackagePO queryByHeight(@Param("height") Long height);

    /**
     * query package list by block height
     *
     * @param height
     * @return
     */
    List<Long> queryHeightListByHeight(@Param("height") Long height);

    /**
     * query packagePO by height for update
     *
     * @param height
     * @return
     */
    PackagePO queryByHeightForUpdate(@Param("height") Long height);

    /**
     * update status by block height
     *
     * @param height
     * @param from
     * @param to
     * @return
     */
    int updateStatus(@Param("height") Long height, @Param("from") String from, @Param("to") String to);

    /**
     * get max height of package
     *
     * @return
     */
    Long getMaxHeight();

    /**
     * query package list by package status
     *
     * @param status
     * @return
     */
    List<PackagePO> queryByStatus(@Param("status") String status);

    /**
     * query min package height with status and start height
     *
     * @param statusSet
     * @param startHeight
     * @return
     */
    Long getMinHeightWithHeightAndStatus(@Param("startHeight") Long startHeight,
        @Param("statusSet") Set<String> statusSet);

    /**
     * count how much package
     *
     * @param statusSet
     * @param maxBlockHeight
     * @return
     */
    Long countWithStatus(@Param("statusSet") Set<String> statusSet, @Param("maxBlockHeight") Long maxBlockHeight);

    /**
     * query max height by status
     *
     * @param status
     * @return
     */
    Long getMaxHeightByStatus(@Param("status")String status);
    /**
     * query min height by status
     *
     * @param status
     * @return
     */

    Long getMinHeightByStatus(@Param("status")String status);

    /**
     * delete by less than height
     *
     * @param height
     * @param status
     * @return
     */
    int deleteLessThanHeightAndStatus(@Param("height")Long height,@Param("status")String status);

    /**
     * get height list by status
     * @param status
     * @return
     */
    List<Long> getBlockHeightsByStatus(@Param("status")String status);
}
