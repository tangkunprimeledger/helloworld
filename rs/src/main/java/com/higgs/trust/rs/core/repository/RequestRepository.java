package com.higgs.trust.rs.core.repository;

import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RequestEnum;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.dao.RequestDao;
import com.higgs.trust.rs.core.dao.RequestJDBCDao;
import com.higgs.trust.rs.core.dao.po.RequestPO;
import com.higgs.trust.rs.core.dao.rocks.RequestRocksDao;
import com.higgs.trust.rs.core.vo.RequestVO;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.vo.RespData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tangfashuang
 */
@Repository
@Slf4j
public class RequestRepository {
    @Autowired
    private RequestDao requestDao;

    @Autowired
    private RequestJDBCDao requestJDBCDao;

    @Autowired
    private RequestRocksDao requestRocksDao;

    @Autowired
    private RsConfig rsConfig;

    /**
     * check request idempotent
     *
     * @param requestId
     * @return
     */
    public RespData<?> requestIdempotent(String requestId) {
        RespData<?> respData = null;
        RequestPO requestPO;
        if (rsConfig.isUseMySQL()) {
            requestPO = requestDao.queryByRequestId(requestId);
        } else {
            requestPO = requestRocksDao.get(requestId);
        }
        if (null != requestPO) {
            respData = new RespData<>(requestPO.getRespCode(), requestPO.getRespMsg());
        }
        return respData;
    }

    /**
     * check request idempotent
     *
     * @param requestId
     * @return
     */
    public RequestVO queryByRequestId(String requestId) {
        RequestPO requestPO = requestDao.queryByRequestId(requestId);
        if (null == requestPO) {
            return null;
        }
        RequestVO requestVO = new RequestVO();
        BeanUtils.copyProperties(requestPO, requestVO);
        return requestVO;
    }

    /**
     * request insert db
     *
     * @param
     */
    public RespData<?> insertRequest(String requestId, RequestEnum requestEnum, String respCode, String respMsg) {
        RespData<?> respData = null;
        RequestPO requestPO = buildRequestPO(requestId, requestEnum, respCode, respMsg);
        try {
            //for mysql
            if (rsConfig.isUseMySQL()) {
                requestDao.add(requestPO);
                return null;
            }
            //for rocks DB
            requestRocksDao.save(requestPO);
        } catch (DuplicateKeyException e) {
            log.error("Request for requestId : {} is idempotent", requestId);
            respData = requestIdempotent(requestId);
        }
        return respData;
    }


    /**
     * just update code
     *
     * @param requestId
     * @param respCode
     * @param respMsg
     */
    public void updateCode(String requestId, String respCode, String respMsg) {
        update(requestId, null, null, respCode, respMsg);
    }


    /**
     * update status and code
     *
     * @param requestId
     * @param fromStatus
     * @param toStatus
     * @param respCode
     * @param respMsg
     */
    public void updateStatusAndCode(String requestId, RequestEnum fromStatus, RequestEnum toStatus, String respCode, String respMsg) {
        update(requestId, fromStatus, toStatus, respCode, respMsg);
    }

    /**
     * update
     *
     * @param requestId
     * @param fromStatus
     * @param toStatus
     * @param respCode
     * @param respMsg
     */
    private void update(String requestId, RequestEnum fromStatus, RequestEnum toStatus, String respCode, String respMsg) {
        String fromStatusCode = null != fromStatus ? fromStatus.getCode() : null;
        String toStatusCode = null != toStatus ? toStatus.getCode() : null;
        //for mysql
        if (rsConfig.isUseMySQL()) {
            int isUpdated = requestDao.updateStatusByRequestId(requestId, fromStatusCode, toStatusCode, respCode, respMsg);
            if (1 != isUpdated) {
                log.error("Update request failed for requestId :{}, fromStatus :{}, toStatus :{}, respCode :{}, respMsg :{}", requestId, fromStatus.getCode(), toStatus.getCode(), respCode, respMsg);
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_REQUEST_UPDATE_STATUS_FAILED);
            }
            return;
        }

        //for rockDB
        requestRocksDao.updateStatus(requestId, fromStatusCode, toStatusCode, respCode, respMsg);
    }

    private RequestPO buildRequestPO(String requestId, RequestEnum requestEnum, String respCode, String respMsg) {
        RequestPO requestPO = new RequestPO();
        requestPO.setRequestId(requestId);
        requestPO.setRespCode(respCode);
        requestPO.setRespMsg(respMsg);
        requestPO.setStatus(requestEnum.getCode());
        return requestPO;
    }

    public void batchUpdateStatus(List<RsCoreTxVO> rsCoreTxVOS, RequestEnum from, RequestEnum to) {
        if (rsConfig.isUseMySQL()) {
            requestJDBCDao.batchUpdateStatus(rsCoreTxVOS, from, to);
        } else {
            requestRocksDao.batchUpdateStatus(rsCoreTxVOS, from, to);
        }
    }
}
