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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
        RequestPO requestPO = queryRequestPO(requestId);
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
        RequestPO requestPO = queryRequestPO(requestId);
        if (null == requestPO) {
            return null;
        }
        RequestVO requestVO = new RequestVO();
        BeanUtils.copyProperties(requestPO, requestVO);
        return requestVO;
    }

    /**
     * query by requestId
     *
     * @param requestId
     * @return
     */
    private RequestPO queryRequestPO(String requestId) {
        // for mysql
        if (rsConfig.isUseMySQL()) {
            return requestDao.queryByRequestId(requestId);
        }
        //for rocks DB
        return requestRocksDao.get(requestId);
    }

    /**
     * request insert db
     *
     * @param
     */
    public void insertRequest(String requestId, RequestEnum requestEnum, String respCode, String respMsg) {
        if (StringUtils.isBlank(requestId) || null == requestEnum) {
            throw new NullPointerException("RequestId or requestEnum is null error!");
        }
        RequestPO requestPO = buildRequestPO(requestId, requestEnum, respCode, respMsg);
        try {
            //for mysql
            if (rsConfig.isUseMySQL()) {
                requestDao.add(requestPO);
                return;
            }
            //for rocks DB
            requestRocksDao.save(requestPO);
        } catch (DuplicateKeyException e) {
            log.error("Request for requestId : {} is idempotent", requestId);
            throw e;
        }
    }


    /**
     * batch insert
     *
     * @param requestPOList
     */
    public void batchInsert(List<RequestPO> requestPOList) {
        if (CollectionUtils.isEmpty(requestPOList)) {
            throw new NullPointerException("requestPOList is null error!");
        }
        try {
            //for mysql
            if (rsConfig.isUseMySQL()) {
                int insertRows = requestDao.batchInsert(requestPOList);
                if (insertRows != requestPOList.size()) {
                    log.error("Batch insert request failed for insert rows:{}, need to insert size is :{}", insertRows, requestPOList.size());
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_REQUEST_ADD_FAILED);
                }
                return;
            }
            //for rocks DB
            requestRocksDao.batchInsert(requestPOList);
        } catch (DuplicateKeyException e) {
            log.error("Request for requestPOList : {} is idempotent", requestPOList);
            throw e;
        }
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

    /**
     * batch update
     *
     * @param rsCoreTxVOS
     * @param from
     * @param to
     */
    public boolean batchUpdateStatus(List<RsCoreTxVO> rsCoreTxVOS, RequestEnum from, RequestEnum to) {
        if (rsConfig.isUseMySQL()) {
            return rsCoreTxVOS.size() == requestJDBCDao.batchUpdateStatus(rsCoreTxVOS, from, to);
        }
        try {
            requestRocksDao.batchUpdateStatus(rsCoreTxVOS, from, to);
        } catch (Throwable e) {
            log.error("BatchUpdate request status failed!", e);
            return false;
        }
        return true;
    }
}
