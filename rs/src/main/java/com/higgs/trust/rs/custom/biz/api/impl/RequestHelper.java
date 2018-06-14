package com.higgs.trust.rs.custom.biz.api.impl;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.rs.custom.api.enums.RequestEnum;
import com.higgs.trust.rs.custom.api.enums.RequestStatusEnum;
import com.higgs.trust.rs.custom.dao.RequestDao;
import com.higgs.trust.rs.custom.dao.po.RequestPO;
import com.higgs.trust.slave.api.vo.RespData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * @author tangfashuang
 * @date 2018/05/18 14:20
 * @desc request service
 */
@Service
@Slf4j
public class RequestHelper {

    @Autowired
    private RequestDao requestDao;

    /**
     * check request idempotent
     * @param requestId
     * @return
     */
    public RespData requestIdempotent(String requestId) {

        RespData respData = null;
        RequestPO requestPO = requestDao.queryByRequestId(requestId);
        if (null != requestPO) {
            if (StringUtils.equals(RequestStatusEnum.PROCESSING.getCode(), requestPO.getStatus())) {
                return new RespData(RequestStatusEnum.DUPLICATE.getCode(), RequestStatusEnum.DUPLICATE.getDesc());
            }
            return new RespData<>(requestPO.getRespCode(), requestPO.getRespMsg());
        }
        return respData;
    }

    /**
     * request insert db
     *
     * @param
     */
    public RespData insertRequest(String requestId, Object obj) {
        RespData respData = null;
        try {
            requestDao.add(buildRequestPO(requestId, obj));
        } catch (DuplicateKeyException e) {
            log.error("requestId : {} for : obj {} is idempotent", requestId, obj);
            respData = requestIdempotent(requestId);
        }
        return respData;
    }

    public void updateRequest(String requestId, RequestEnum fromStatus, RequestEnum toStatus, String respCode, String respMsg) {
        requestDao.updateStatusByRequestId(requestId, fromStatus.getCode(), toStatus.getCode(), respCode, respMsg);
    }

    private RequestPO buildRequestPO(String requestId, Object obj) {
        RequestPO requestPO = new RequestPO();
        requestPO.setRequestId(requestId);
        requestPO.setData(JSON.toJSONString(obj));
        requestPO.setStatus(RequestEnum.PROCESS.getCode());
        return requestPO;
    }
}
