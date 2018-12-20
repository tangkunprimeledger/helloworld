package com.higgs.trust.rs.core.api;

import com.higgs.trust.rs.core.bo.ContractCreateV2Request;
import com.higgs.trust.rs.core.bo.ContractMigrationRequest;
import com.higgs.trust.rs.core.bo.ContractQueryRequest;
import com.higgs.trust.slave.api.vo.ContractVO;
import com.higgs.trust.slave.api.vo.PageVO;
import com.higgs.trust.slave.api.vo.QueryContractVO;
import com.higgs.trust.slave.api.vo.RespData;

import java.math.BigDecimal;
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
     * deploy contract
     *
     * @return
     */
    RespData deployV2(ContractCreateV2Request request);

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
     * invoke v2 contract
     * @param txId
     * @param from
     * @param to
     * @param value
     * @param methodSignature
     * @param args
     * @return
     */
    RespData invokeV2(String txId, String from, String to, BigDecimal value, String methodSignature, Object... args);

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
    /**
     * query by txId and action index
     *
     * @param txId
     * @param actionIndex
     * @return
     */
    ContractVO queryByTxId(String txId,int actionIndex);

    /**
     * Queries contract.
     *
     * @param blockHeight block height
     * @param contractAddress contract address
     * @param methodSignature method signature written with target language
     * @param methodInputArgs actual parameters
     * @return result returned by contract invocation
     */
    List<?> query2(Long blockHeight, String contractAddress, String methodSignature, Object... methodInputArgs);
}
