package com.higgs.trust.rs.core.api;

import com.higgs.trust.rs.common.enums.RequestEnum;
import com.higgs.trust.rs.core.dao.po.RequestPO;
import com.higgs.trust.rs.core.vo.RequestVO;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.vo.RespData;

import java.util.List;

/**
 * @description: RequestService
 * @author: lingchao
 * @datetime:2018年11月28日19:42
 **/
public interface RequestService {
    /**
     * check request not Idempotent
     *
     * @param requestId
     * @return
     */
    RespData<?> requestIdempotent(String requestId);

    /**
     * check request idempotent
     *
     * @param requestId
     * @return
     */
    RequestVO queryByRequestId(String requestId);

    /**
     * insert request
     *
     * @param requestId
     * @param respCode
     * @param respMsg
     * @return
     */
    void insertRequest(String requestId, RequestEnum requestEnum, String respCode, String respMsg);

    /**
     * batch insert
     * @param requestPOList
     */
    void batchInsert(List<RequestPO> requestPOList);


    /**
     * @param requestId
     * @param respCode
     * @param respMsg
     */
    void updateRespData(String requestId, String respCode, String respMsg);

    /**
     * @param requestId
     * @param fromStatus
     * @param toStatus
     * @param respCode
     * @param respMsg
     */
    void updateStatusAndRespData(String requestId, RequestEnum fromStatus, RequestEnum toStatus, String respCode, String respMsg);

    /**
     * batch update
     * @param rsCoreTxVOS
     * @param from
     * @param to
     */
    void batchUpdateStatus(List<RsCoreTxVO> rsCoreTxVOS, RequestEnum from, RequestEnum to);
}
