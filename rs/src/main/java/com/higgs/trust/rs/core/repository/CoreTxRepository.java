package com.higgs.trust.rs.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.dao.CoreTransactionDao;
import com.higgs.trust.rs.core.dao.CoreTransactionProcessDao;
import com.higgs.trust.rs.core.dao.CoreTxJDBCDao;
import com.higgs.trust.rs.core.dao.CoreTxProcessJDBCDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import com.higgs.trust.rs.core.dao.rocks.CoreTxProcessRocksDao;
import com.higgs.trust.rs.core.dao.rocks.CoreTxRocksDao;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.ReadOptions;
import org.rocksdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author liuyu
 * @description
 * @date 2018-06-06
 */
@Slf4j @Repository public class CoreTxRepository {
    @Autowired private RsConfig rsConfig;
    @Autowired private CoreTransactionDao coreTransactionDao;
    @Autowired private CoreTransactionProcessDao coreTransactionProcessDao;
    @Autowired private CoreTxRocksDao coreTxRocksDao;
    @Autowired private CoreTxProcessRocksDao coreTxProcessRocksDao;
    @Autowired private CoreTxJDBCDao coreTxJDBCDao;
    @Autowired private CoreTxProcessJDBCDao coreTxProcessJDBCDao;

    /**
     * create new core_transaction for INIT
     *
     * @param coreTx
     * @param signInfos
     * @param blockHeight
     */
    public void add(CoreTransaction coreTx, List<SignInfo> signInfos, Long blockHeight) {
        CoreTransactionPO po = BeanConvertor.convertBean(coreTx, CoreTransactionPO.class);
        po.setVersion(coreTx.getVersion());
        if (coreTx.getBizModel() != null) {
            po.setBizModel(coreTx.getBizModel().toJSONString());
        }
        String actionDataJSON = JSON.toJSONString(coreTx.getActionList());
        po.setActionDatas(actionDataJSON);
        String signDataJSON = JSON.toJSONString(signInfos);
        po.setSignDatas(signDataJSON);
        po.setBlockHeight(blockHeight);
        po.setCreateTime(new Date());
        add(po, CoreTxStatusEnum.INIT);
    }

    /**
     * create new core_transaction for result data and block height
     *
     * @param coreTx
     * @param signInfos
     * @param respData
     * @param blockHeight
     */
    public void add(CoreTransaction coreTx, List<SignInfo> signInfos, RespData respData, Long blockHeight) {
        try {
            Profiler.enter("[rs.core.addCoreTx]");
            CoreTransactionPO po = BeanConvertor.convertBean(coreTx, CoreTransactionPO.class);
            po.setVersion(coreTx.getVersion());
            if (coreTx.getBizModel() != null) {
                po.setBizModel(coreTx.getBizModel().toJSONString());
            }
            String actionDataJSON = JSON.toJSONString(coreTx.getActionList());
            po.setActionDatas(actionDataJSON);
            String signDataJSON = JSON.toJSONString(signInfos);
            po.setSignDatas(signDataJSON);
            po.setExecuteResult(
                respData.isSuccess() ? CoreTxResultEnum.SUCCESS.getCode() : CoreTxResultEnum.FAIL.getCode());
            po.setErrorCode(respData.getRespCode());
            po.setErrorMsg(respData.getMsg());
            po.setBlockHeight(blockHeight);
            po.setCreateTime(new Date());

            add(po, null);
        } finally {
            Profiler.release();
        }
    }

    /**
     * add tx po
     *
     * @param po
     * @param coreTxStatusEnum
     */
    private void add(CoreTransactionPO po, CoreTxStatusEnum coreTxStatusEnum) {
        if (rsConfig.isUseMySQL()) {
            try {
                coreTransactionDao.add(po);
                //on init
                if (coreTxStatusEnum != null && coreTxStatusEnum == CoreTxStatusEnum.INIT) {
                    CoreTransactionProcessPO coreTransactionProcessPO = new CoreTransactionProcessPO();
                    coreTransactionProcessPO.setTxId(po.getTxId());
                    coreTransactionProcessPO.setStatus(coreTxStatusEnum.getCode());
                    coreTransactionProcessDao.add(coreTransactionProcessPO);
                }
            } catch (DuplicateKeyException e) {
                log.error("[add.core_transaction]has idempotent error");
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_IDEMPOTENT, e);
            }
        } else {
            coreTxRocksDao.saveWithTransaction(po);

            //on init
            if (coreTxStatusEnum != null && coreTxStatusEnum == CoreTxStatusEnum.INIT) {
                CoreTransactionProcessPO processPO = new CoreTransactionProcessPO();
                processPO.setTxId(po.getTxId());
                processPO.setStatus(coreTxStatusEnum.getCode());
                coreTxProcessRocksDao.saveWithTransaction(processPO, coreTxStatusEnum.getIndex());
            }
        }
    }

    /**
     * query by transaction id
     *
     * @param txId
     * @param forUpdate
     * @return
     */
    public CoreTransactionPO queryByTxId(String txId, boolean forUpdate) {
        if (rsConfig.isUseMySQL()) {
            return coreTransactionDao.queryByTxId(txId, forUpdate);
        }
        //TODO for update
        return coreTxRocksDao.get(txId);
    }

    /**
     * query by txIds
     *
     * @param txIdList
     * @return
     */
    public List<CoreTransactionPO> queryByTxIds(List<String> txIdList) {

        if (rsConfig.isUseMySQL()) {
            return coreTransactionDao.queryByTxIds(txIdList);
        }
        return coreTxRocksDao.queryByTxIds(txIdList);
    }

    /**
     * has core_transaction for txId
     *
     * @param txId
     * @return
     */
    public boolean isExist(String txId) {
        return queryByTxId(txId, false) != null;
    }

    /**
     * update sign data by txId
     *
     * @param txId
     * @param signDatas
     */
    public void updateSignDatas(String txId, List<SignInfo> signDatas) {

        String signJSON = JSON.toJSONString(signDatas);
        if (rsConfig.isUseMySQL()) {
            int r = coreTransactionDao.updateSignDatas(txId, signJSON);
            if (r != 1) {
                log.error("[updateSignDatas]is fail txId:{}", txId);
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_SIGN_DATAS_FAILED);
            }
        } else {
            coreTxRocksDao.updateSignDatas(txId, signJSON);
        }
    }

    /**
     * save execute result for transaction
     *
     * @param txId
     * @param executResult
     * @param respCode
     * @param respMsg
     */
    public void saveExecuteResultAndHeight(String txId, CoreTxResultEnum executResult, String respCode, String respMsg,
        Long blockHeight) {
        try {
            Profiler.enter("[rs.core.saveExecuteResult]");
            if (rsConfig.isUseMySQL()) {
                int r = coreTransactionDao
                    .saveExecuteResultAndHeight(txId, executResult.getCode(), respCode, respMsg, blockHeight);
                if (r != 1) {
                    log.error("[saveExecuteResult]executResult:{},respCode:{} is fail txId:{}", executResult, respCode,
                        txId);
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
                }
            } else {
                coreTxRocksDao.saveExecuteResultAndHeight(txId, executResult.getCode(), respCode, respMsg, blockHeight);
            }
        } finally {
            Profiler.release();
        }
    }

    /**
     * convert transaction PO to BO
     *
     * @param po
     * @return
     */
    public CoreTxBO convertTxBO(CoreTransactionPO po) {
        CoreTxBO bo = BeanConvertor.convertBean(po, CoreTxBO.class);
        if (bo == null) {
            return null;
        }
        String version = po.getVersion();
        bo.setVersion(VersionEnum.getBizTypeEnumBycode(version));
        String bizModel = po.getBizModel();
        bo.setBizModel(JSON.parseObject(bizModel));
        String signJSON = po.getSignDatas();
        bo.setSignDatas(JSON.parseArray(signJSON, SignInfo.class));
        String actionJSON = po.getActionDatas();
        bo.setActionList(JSON.parseArray(actionJSON, Action.class));
        return bo;
    }

    /**
     * convert transaction BO to VO
     *
     * @param bo
     * @return
     */
    public CoreTransaction convertTxVO(CoreTxBO bo) {
        CoreTransaction vo = BeanConvertor.convertBean(bo, CoreTransaction.class);
        if (vo == null) {
            return null;
        }
        if (bo.getVersion() != null) {
            vo.setVersion(bo.getVersion().getCode());
        }
        return vo;
    }

    /**
     * create new core_transaction for batch
     *
     * @param txs
     * @param blockHeight
     */
    public void batchInsert(List<RsCoreTxVO> txs, Long blockHeight) {
        try {
            Profiler.enter("[rs.core.batchInsert]");
            List<CoreTransactionPO> coreTransactionPOList = convert(txs, blockHeight);
            if (rsConfig.isUseMySQL()) {
                try {
                    coreTxJDBCDao.batchInsert(coreTransactionPOList);
                } catch (DuplicateKeyException e) {
                    log.error("[batchInsert.core_transaction]has idempotent error");
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_IDEMPOTENT, e);
                }
            } else {
                coreTxRocksDao.batchInsert(coreTransactionPOList);
            }
        } finally {
            Profiler.release();
        }
    }

    /**
     * update coreTx  for batch
     *
     * @param txs
     * @param blockHeight
     */
    public void batchUpdate(List<RsCoreTxVO> txs, Long blockHeight) {
        if (CollectionUtils.isEmpty(txs)) {
            return;
        }
        try {
            Profiler.enter("[rs.core.batchUpdate]");
            if (rsConfig.isUseMySQL()) {
                int r = coreTxJDBCDao.batchUpdate(convert(txs, blockHeight), blockHeight);
                if (r != txs.size()) {
                    log.error("[batchUpdate.coreTx]  is fail,exe.num:{},txs:{}", r, txs);
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_FAILED);
                }
            } else {
                List<String> txIds = new ArrayList<>(txs.size());
                Map<String, RsCoreTxVO> map = new HashMap<>(txs.size());
                for (RsCoreTxVO vo : txs) {
                    txIds.add(vo.getTxId());
                    map.put(vo.getTxId(), vo);
                }
                List<CoreTransactionPO> pos = coreTxRocksDao.queryByTxIds(txIds);
                if (txIds.size() != pos.size()) {
                    log.error("[batchUpdate.coreTx]  is fail, rsIds.size:{}, pos.size:{}", txIds.size(), pos.size());
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_FAILED);
                }
                for (CoreTransactionPO po : pos) {
                    RsCoreTxVO vo = map.get(po.getTxId());
                    po.setErrorMsg(vo.getErrorMsg());
                    po.setErrorCode(vo.getErrorCode());
                    po.setExecuteResult(vo.getExecuteResult().getCode());
                    po.setBlockHeight(blockHeight);
                    po.setUpdateTime(new Date());
                    coreTxRocksDao.updateWithTransaction(po);
                }
            }
        } finally {
            Profiler.release();
        }
    }

    /**
     * convert bean
     *
     * @param txs
     * @return
     */
    private List<CoreTransactionPO> convert(List<RsCoreTxVO> txs, Long blockHeight) {
        List<CoreTransactionPO> coreTransactionPOList = new ArrayList<>(txs.size());
        for (RsCoreTxVO vo : txs) {
            CoreTransactionPO po = new CoreTransactionPO();
            po.setTxId(vo.getTxId());
            po.setPolicyId(vo.getPolicyId());
            if (vo.getBizModel() != null) {
                po.setBizModel(vo.getBizModel().toJSONString());
            }
            po.setTxType(vo.getTxType());
            po.setVersion(vo.getVersion().getCode());
            po.setLockTime(vo.getLockTime());
            po.setSendTime(vo.getSendTime());
            po.setSender(vo.getSender());
            po.setActionDatas(JSON.toJSONString(vo.getActionList()));
            po.setSignDatas(JSON.toJSONString(vo.getSignDatas()));
            po.setExecuteResult(vo.getExecuteResult().getCode());
            po.setErrorCode(vo.getErrorCode());
            po.setErrorMsg(vo.getErrorMsg());
            po.setBlockHeight(blockHeight);
            coreTransactionPOList.add(po);
        }
        return coreTransactionPOList;
    }

    public CoreTransactionPO getForUpdate(Transaction tx, ReadOptions readOptions, String txId, boolean exclusive) {
        return coreTxRocksDao.getForUpdate(tx, readOptions, txId, exclusive);
    }

    /**
     * query by transaction id
     *
     * @param txId
     * @param statusEnum
     * @return
     */
    public CoreTransactionProcessPO queryStatusByTxId(String txId, CoreTxStatusEnum statusEnum) {
        if (rsConfig.isUseMySQL()) {
            return coreTransactionProcessDao.queryByTxId(txId, statusEnum == null ? null : statusEnum.getCode());
        }
        CoreTxStatusEnum coreTxStatusEnum =
            coreTxProcessRocksDao.queryByTxIdAndStatus(txId, statusEnum == null ? null : statusEnum.getIndex());

        if (null == coreTxStatusEnum) {
            return null;
        }

        CoreTransactionProcessPO coreTxProcessPO = new CoreTransactionProcessPO();
        coreTxProcessPO.setTxId(txId);
        coreTxProcessPO.setStatus(coreTxStatusEnum.getCode());
        return coreTxProcessPO;
    }

    /**
     * query for status by page no
     *
     * @param coreTxStatusEnum
     * @param row
     * @param count
     * @param preKey           for rocks db seek
     * @return
     */
    public List<CoreTransactionProcessPO> queryByStatus(CoreTxStatusEnum coreTxStatusEnum, int row, int count,
        String preKey) {
        if (rsConfig.isUseMySQL()) {
            return coreTransactionProcessDao.queryByStatus(coreTxStatusEnum.getCode(), row, count);
        } else {
            preKey = StringUtils.isEmpty(preKey) ? coreTxStatusEnum.getIndex() :
                coreTxStatusEnum.getIndex() + Constant.SPLIT_SLASH + preKey;
            return coreTxProcessRocksDao.queryByPrefix(preKey, count);
        }
    }

    /**
     * update status by from to
     *
     * @param txId
     * @param from
     * @param to
     */
    public void updateStatus(String txId, CoreTxStatusEnum from, CoreTxStatusEnum to) {
        if (rsConfig.isUseMySQL()) {
            int r = coreTransactionProcessDao.updateStatus(txId, from.getCode(), to.getCode());
            if (r != 1) {
                log.error("[updateStatus]from {} to {} is fail txId:{}", from, to, txId);
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
            }
        } else {
            coreTxProcessRocksDao.updateStatus(txId, from, to);
        }
    }

    /**
     * delete status for batch
     *
     * @param txs
     * @param statusEnum
     */
    public void batchDelete(List<RsCoreTxVO> txs, CoreTxStatusEnum statusEnum) {
        try {
            Profiler.enter("[rs.core.batchDeleteForEnd]");
            if (rsConfig.isUseMySQL()) {
                int r = coreTxProcessJDBCDao.batchDelete(txs, statusEnum);
                if (r != txs.size()) {
                    log.error("[batchDeleteForEnd]has fail result:{},tx.size:{}", r, txs.size());
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
                }
            } else {
                coreTxProcessRocksDao.batchDelete(txs, statusEnum);
            }
        } finally {
            Profiler.release();
        }
    }

    /**
     * convert bean
     *
     * @param txs
     * @return
     */
    private List<CoreTransactionProcessPO> convert(List<RsCoreTxVO> txs, CoreTxStatusEnum statusEnum) {
        List<CoreTransactionProcessPO> coreTransactionProcessPOList = new ArrayList<>(txs.size());
        for (RsCoreTxVO vo : txs) {
            CoreTransactionProcessPO po = new CoreTransactionProcessPO();
            po.setTxId(vo.getTxId());
            po.setStatus(statusEnum.getCode());
            coreTransactionProcessPOList.add(po);
        }
        return coreTransactionProcessPOList;
    }

    /**
     * query by status
     *
     * @param txId
     * @param statusEnum
     * @return
     */
    public CoreTransactionProcessPO queryByStatus(String txId, CoreTxStatusEnum statusEnum) {
        String key = statusEnum.getIndex() + Constant.SPLIT_SLASH + txId;
        return coreTxProcessRocksDao.get(key);
    }

    /**
     * for failover
     *
     * @param txs
     * @param blockHeight
     */
    public void failoverBatchInsert(List<RsCoreTxVO> txs, Long blockHeight) {
        List<CoreTransactionPO> coreTransactionPOList = convert(txs, blockHeight);
        coreTxRocksDao.failoverBatchInsert(coreTransactionPOList);
        coreTxProcessRocksDao.batchDelete(txs, CoreTxStatusEnum.WAIT);
    }
}
