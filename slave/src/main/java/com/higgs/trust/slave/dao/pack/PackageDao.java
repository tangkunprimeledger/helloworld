package com.higgs.trust.slave.dao.pack;

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
     * query package height list by package status
     *
     * @param status
     * @return
     */
    List<Long> queryHeightListByStatus(@Param("status") String status);

    /**
     * query package height list by package status and limit
     *
     * @param status
     * @param limit
     * @return
     */
    List<Long> queryHeightsByStatusAndLimit(@Param("status") String status, @Param("limit") int limit);

    /**
     * query min package height with status
     *
     * @param statusSet
     * @param maxBlockHeight
     * @return
     */
    Long getMinHeightWithStatus(@Param("statusSet") Set<String> statusSet, @Param("maxBlockHeight") Long maxBlockHeight);

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
}
