package com.higgs.trust.rs.core.service;

import com.higgs.trust.rs.common.enums.RequestEnum;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.RequestService;
import com.higgs.trust.rs.core.dao.po.RequestPO;
import com.higgs.trust.rs.core.repository.RequestRepository;
import com.higgs.trust.rs.core.vo.RequestVO;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.vo.RespData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description: com.higgs.trust.rs.core.service
 * @author: lingchao
 * @datetime:2018年11月28日19:47
 **/
@Slf4j
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
     * when return null is inserted
     *
     * @param requestId
     * @param respCode
     * @param respMsg
     * @return
     */
    @Override
    public void insertRequest(String requestId, RequestEnum requestEnum, String respCode, String respMsg) {
        requestRepository.insertRequest(requestId, requestEnum, respCode, respMsg);
    }


    /**
     * batch insert
     *
     * @param requestPOList
     */
    @Override
    public void batchInsert(List<RequestPO> requestPOList) {
        requestRepository.batchInsert(requestPOList);
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

    /**
     * batch update
     *
     * @param rsCoreTxVOS
     * @param from
     * @param to
     */
    @Override
    public void batchUpdateStatus(List<RsCoreTxVO> rsCoreTxVOS, RequestEnum from, RequestEnum to) {
       if (!requestRepository.batchUpdateStatus(rsCoreTxVOS, from, to)){
           log.error("batch update request Status fail for rsCoreTxVOS:{}", rsCoreTxVOS);
           throw new RsCoreException(RsCoreErrorEnum.RS_CORE_REQUEST_UPDATE_STATUS_FAILED);
       }
    }
}
