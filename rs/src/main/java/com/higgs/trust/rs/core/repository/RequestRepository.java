package com.higgs.trust.rs.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RequestEnum;
import com.higgs.trust.rs.common.enums.RespCodeEnum;
import com.higgs.trust.rs.core.dao.RequestDao;
import com.higgs.trust.rs.core.dao.RequestJDBCDao;
import com.higgs.trust.rs.core.dao.po.RequestPO;
import com.higgs.trust.rs.core.dao.rocks.RequestRocksDao;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.vo.RespData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
     * @param requestId
     * @return
     */
    public RespData requestIdempotent(String requestId) {

        RequestPO requestPO;
        if (rsConfig.isUseMySQL()) {
            requestPO = requestDao.queryByRequestId(requestId);
        } else {
            requestPO = requestRocksDao.get(requestId);
        }

        if (null != requestPO) {
            if (StringUtils.equals(RequestEnum.PROCESS.getCode(), requestPO.getStatus())) {
                return new RespData(RespCodeEnum.REQUEST_DUPLICATE.getRespCode(), RespCodeEnum.REQUEST_DUPLICATE.getMsg());
            }
            return new RespData<>(requestPO.getRespCode(), requestPO.getRespMsg());
        }
        return null;
    }

    /**
     * request insert db
     *
     * @param
     */
    public RespData insertRequest(String requestId, Object obj) {
        RespData respData = null;
        if (rsConfig.isUseMySQL()) {
            try {
                requestDao.add(buildRequestPO(requestId, obj));
            } catch (DuplicateKeyException e) {
                log.error("requestId : {} for : obj {} is idempotent", requestId, obj);
                respData = requestIdempotent(requestId);
            }
        } else {
            requestRocksDao.save(buildRequestPO(requestId, obj));
        }
        return respData;
    }

    public void updateRequest(String requestId, RequestEnum fromStatus, RequestEnum toStatus, String respCode, String respMsg) {
        if (rsConfig.isUseMySQL()) {
            requestDao.updateStatusByRequestId(requestId, fromStatus.getCode(), toStatus.getCode(), respCode, respMsg);
        } else {
            requestRocksDao.updateStatus(requestId, fromStatus.getCode(), toStatus.getCode(), respCode, respMsg);
        }
    }

    private RequestPO buildRequestPO(String requestId, Object obj) {
        RequestPO requestPO = new RequestPO();
        requestPO.setRequestId(requestId);
        requestPO.setData(JSON.toJSONString(obj));
        requestPO.setStatus(RequestEnum.PROCESS.getCode());
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
