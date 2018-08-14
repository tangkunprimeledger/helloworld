package com.higgs.trust.rs.core.api;

import com.higgs.trust.rs.core.bo.ContractMigrationRequest;
import com.higgs.trust.rs.core.bo.ContractQueryRequest;
import com.higgs.trust.slave.api.vo.ContractVO;
import com.higgs.trust.slave.api.vo.PageVO;
import com.higgs.trust.slave.api.vo.QueryContractVO;
import com.higgs.trust.slave.api.vo.RespData;

import java.util.List;

/**
 * @author duhongming
 * @date 2018/5/14
 */
public interface ContractService {
    /**
     * deploy contract
     *
     * @param txId
     * @param code
     * @return
     */
    RespData deploy(String txId, String code, Object... initArgs);

    /**
     * query contract list
     *
     * @param height
     * @param txId
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @Deprecated
    PageVO<ContractVO> queryList(Long height, String txId, Integer pageIndex, Integer pageSize);


    /**
     * query contract list
     *
     * @param queryContractVO
     * @return
     */
    List<ContractVO> queryContractsByPage(QueryContractVO queryContractVO);

    /**
     * invoke contract
     *
     * @param txId
     * @param address
     * @param args
     */
    RespData invoke(String txId, String address, Object... args);

    /**
     * migration contract state
     *
     * @param migrationRequest
     * @return
     */
    RespData migration(ContractMigrationRequest migrationRequest);

    /**
     * query contract state
     *
     * @param request
     * @return
     */
    Object query(ContractQueryRequest request);
}
