package com.higgs.trust.rs.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.dao.CoreTransactionDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-06-06
 */
@Slf4j @Repository public class CoreTxRepository {
    @Autowired private RsConfig rsConfig;
    @Autowired private CoreTransactionDao coreTransactionDao;

    /**
     * create new core_transaction
     *
     * @param coreTx
     * @param signInfos
     */
    public void add(CoreTransaction coreTx, List<SignInfo> signInfos,CoreTxStatusEnum statusEnum) {
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return;
        }
        CoreTransactionPO po = BeanConvertor.convertBean(coreTx, CoreTransactionPO.class);
        po.setVersion(coreTx.getVersion());
        if (coreTx.getBizModel() != null) {
            po.setBizModel(coreTx.getBizModel().toJSONString());
        }
        String actionDataJSON = JSON.toJSONString(coreTx.getActionList());
        po.setActionDatas(actionDataJSON);
        String signDataJSON = JSON.toJSONString(signInfos);
        po.setSignDatas(signDataJSON);
        po.setStatus(statusEnum == null ? CoreTxStatusEnum.INIT.getCode() : statusEnum.getCode());
        po.setCreateTime(new Date());
        try {
            coreTransactionDao.add(po);
        } catch (DuplicateKeyException e) {
            log.error("[add.core_transaction]has idempotent error");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_IDEMPOTENT);
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
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return null;
        }
        return coreTransactionDao.queryByTxId(txId, forUpdate);
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
     * query for status by page no
     *
     * @param coreTxStatusEnum
     * @param row
     * @param count
     * @return
     */
    public List<CoreTransactionPO> queryByStatus(CoreTxStatusEnum coreTxStatusEnum, int row, int count){
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return null;
        }
        return coreTransactionDao.queryByStatus(coreTxStatusEnum.getCode(),row,count);
    }

    /**
     * update sign data by txId
     *
     * @param txId
     * @param signDatas
     */
    public void updateSignDatas(String txId, List<SignInfo> signDatas) {
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return;
        }
        String signJSON = JSON.toJSONString(signDatas);
        int r = coreTransactionDao.updateSignDatas(txId, signJSON);
        if (r != 1) {
            log.error("[updateSignDatas]is fail txId:{}", txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_SIGN_DATAS_FAILED);
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
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return;
        }
        int r = coreTransactionDao.updateStatus(txId, from.getCode(), to.getCode());
        if (r != 1) {
            log.error("[updateStatus]from {} to {} is fail txId:{}",from,to, txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
        }
    }

    /**
     * save execute result for transaction
     *
     * @param txId
     * @param executResult
     * @param respCode
     */
    public void saveExecuteResult(String txId,CoreTxResultEnum executResult,String respCode){
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return;
        }
        int r = coreTransactionDao.saveExecuteResult(txId,executResult.getCode(), respCode);
        if (r != 1) {
            log.error("[saveExecuteResult]executResult:{},respCode:{} is fail txId:{}",executResult,respCode, txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
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
}
