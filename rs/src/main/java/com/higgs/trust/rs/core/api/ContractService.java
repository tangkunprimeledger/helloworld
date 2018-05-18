package com.higgs.trust.rs.core.api;

import com.higgs.trust.rs.core.bo.Contract;
import com.higgs.trust.slave.api.vo.ContractVO;
import com.higgs.trust.slave.api.vo.PageVO;
import com.higgs.trust.slave.api.vo.RespData;

import java.util.List;

/**
 * @author duhongming
 * @date 2018/5/14
 */
public interface ContractService {
    /**
     * deploy contract
     * @param txId
     * @param code
     * @return
     */
    RespData deploy(String txId, String code);

    /**
     * query contract list
     * @param height
     * @param txId
     * @param pageIndex
     * @param pageSize
     * @return
     */
    PageVO<ContractVO> queryList(Long height, String txId, Integer pageIndex, Integer pageSize);

    /**
     * invoke contract
     * @param txId
     * @param address
     * @param args
     */
    RespData invoke(String txId, String address, Object... args);
}
