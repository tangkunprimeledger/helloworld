package com.higgs.trust.slave.dao.pack;

import com.higgs.trust.slave.dao.BaseDao;
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
     * query min package height with status
     *
     * @param statusSet
     * @return
     */
    long getMinHeightWithStatus(@Param("statusSet") Set<String> statusSet);

    /**
     * query min package height with status and start height
     *
     * @param statusSet
     * @return
     */
    Long getMinHeightWithHeightAndStatus(@Param("startHeight") Long startHeight,
        @Param("statusSet") Set<String> statusSet);

    /**
     * count how much package
     *
     * @param statusSet
     * @return
     */
    long countWithStatus(@Param("statusSet") Set<String> statusSet);

    /**
     * get height list for process
     *
     * @param maxBlockHeight
     * @param limit
     * @return
     */
    List<Long> getHeightListForProcess(@Param("maxBlockHeight") Long maxBlockHeight, @Param("limit") int limit);
}
