package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.common.enums.RequestEnum;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.dao.po.RequestPO;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.Transaction;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 * @desc key: requestId, value: requestPO
 */
@Service
@Slf4j
public class RequestRocksDao extends RocksBaseDao<RequestPO> {
    @Override
    protected String getColumnFamilyName() {
        return "request";
    }

    public void save(RequestPO requestPO) {
        String key = requestPO.getRequestId();
        if (keyMayExist(key) && null != get(key)) {
            log.error("[RequestRocksDao.save] request is exist, requestId={}", key);
            throw new DuplicateKeyException("[RequestRocksDao.save] request is exist, requestId = " + requestPO.getRequestId());
        }
        requestPO.setCreateTime(new Date());
        put(key, requestPO);
    }

    /**
     * batchInsert
     *
     * @param requestPOList
     */
    public void batchInsert(List<RequestPO> requestPOList) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[RequestRocksDao.batchInsert] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }
        for (RequestPO requestPO : requestPOList) {
            String key = requestPO.getRequestId();
            if (keyMayExist(key) && null != get(key)) {
                log.error("[RequestRocksDao.save] request is exist, requestId={}", key);
                throw new DuplicateKeyException("[RequestRocksDao.save] request is exist, requestId = " + requestPO.getRequestId());
            }
            requestPO.setCreateTime(new Date());
            txPut(tx, key, requestPO);
        }
    }

    /**
     * slave-callback update
     *
     * @param requestId
     * @param fromStatus
     * @param toStatus
     * @param respCode
     * @param respMsg
     */
    public void updateStatus(String requestId, String fromStatus, String toStatus, String respCode, String respMsg) {

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[RequestRocksDao.updateStatus] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        RequestPO requestPO = get(requestId);
        if (null == requestPO) {
            log.error("[RequestRocksDao.updateStatus] request is not exist. requestId={}", requestId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
        }

        if (!StringUtils.isEmpty(fromStatus) && !StringUtils.equals(fromStatus, requestPO.getStatus())) {
            log.error("[RequestRocksDao.updateStatus] request status is invalid, requestId={}, currentStatus={}, status", requestId, requestPO.getStatus(), fromStatus);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_REQUEST_UPDATE_STATUS_FAILED);
        }
        if (!StringUtils.isEmpty(toStatus)) {
            requestPO.setStatus(toStatus);
        }
        requestPO.setRespCode(respCode);
        requestPO.setRespMsg(respMsg);
        requestPO.setUpdateTime(new Date());

        txPut(tx, requestId, requestPO);
    }



    public void batchUpdateStatus(List<RsCoreTxVO> rsCoreTxVOS, RequestEnum from, RequestEnum to) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[RequestRocksDao.batchUpdateStatus] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (RsCoreTxVO vo : rsCoreTxVOS) {
            String key = vo.getTxId();
            RequestPO po = get(key);
            if (null == po) {
                log.error("[RequestRocksDao.batchUpdateStatus] request is not exist, requestId={}", key);
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
            }

            if (!StringUtils.equals(from.getCode(), po.getStatus())) {
                log.error("[RequestRocksDao.batchUpdateStatus] request status is invalid, requestId={}, currentStatus={}, status", key, po.getStatus(), from.getCode());
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_REQUEST_UPDATE_STATUS_FAILED);
            }

            po.setStatus(to.getCode());
            po.setRespMsg(vo.getErrorMsg());
            po.setRespCode(vo.getErrorMsg());
            po.setUpdateTime(new Date());

            txPut(tx, key, po);
        }
    }
}
