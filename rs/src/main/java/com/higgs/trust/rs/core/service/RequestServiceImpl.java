package com.higgs.trust.rs.core.service;

import com.higgs.trust.rs.common.enums.RequestEnum;
import com.higgs.trust.rs.core.api.RequestService;
import com.higgs.trust.rs.core.repository.RequestRepository;
import com.higgs.trust.rs.core.vo.RequestVO;
import com.higgs.trust.slave.api.vo.RespData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description: com.higgs.trust.rs.core.service
 * @author: lingchao
 * @datetime:2018年11月28日19:47
 **/
@Service
public class RequestServiceImpl implements RequestService {
    @Autowired
    private RequestRepository requestRepository;

    /**
     * check request not Idempotent
     *
     * @param requestId
     * @return
     */
    @Override
    public RespData<?> requestIdempotent(String requestId) {
        return requestRepository.requestIdempotent(requestId);
    }

    /**
     * check request idempotent
     *
     * @param requestId
     * @return
     */
    public RequestVO queryByRequestId(String requestId) {
        return requestRepository.queryByRequestId(requestId);
    }

    /**
     * insert request
     * when return null is  inserted.
     * @param requestId
     * @param respCode
     * @param respMsg
     * @return
     */
    @Override
    public RespData<?> insertRequest(String requestId, RequestEnum requestEnum, String respCode, String respMsg) {
        return requestRepository.insertRequest(requestId, requestEnum, respCode, respMsg);
    }

    /**
     * @param requestId
     * @param respCode
     * @param respMsg
     */
    @Override
    public void updateRespData(String requestId, String respCode, String respMsg) {
        requestRepository.updateCode(requestId, respCode, respMsg);
    }

    /**
     * @param requestId
     * @param fromStatus
     * @param toStatus
     * @param respCode
     * @param respMsg
     */
    @Override
    public void updateStatusAndRespData(String requestId, RequestEnum fromStatus, RequestEnum toStatus, String respCode, String respMsg) {
        requestRepository.updateStatusAndCode(requestId, fromStatus, toStatus, respCode, respMsg);
    }
}
