package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.rs.common.TxCallbackHandler;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.TxCallbackRegistor;
import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.dao.CoreTransactionDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.vo.CoreTxVO;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.Policy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service @Slf4j public class CoreTransactionServiceImpl implements CoreTransactionService {
    @Autowired private TransactionTemplate txRequired;
    @Autowired private CoreTransactionDao coreTransactionDao;
    @Autowired private PolicyRepository policyRepository;
    @Autowired private RsConfig rsConfig;
    @Autowired private BlockChainService blockChainService;
    @Autowired private TxCallbackRegistor txCallbackRegistor;

    @Override public void submitTx(BizTypeEnum bizType,CoreTxVO vo,String signData) {
        log.info("[submitTx]{}", vo);
        if (vo == null) {
            log.error("[submitTx] the tx is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_PARAM_VALIDATE_ERROR);
        }
        //validate param
        BeanValidateResult validateResult = BeanValidator.validate(vo);
        if (!validateResult.isSuccess()) {
            log.error("[submitTx] param validate is fail,first msg:{}", validateResult.getFirstMsg());
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_PARAM_VALIDATE_ERROR);
        }
        //check bizType
        if(bizType == null){
            log.error("[submitTx] bizType is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_BIZ_TYPE_IS_NULL);
        }
        //check sign data of self
        if(StringUtils.isEmpty(signData)){
            log.error("[submitTx] self sign data is empty");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_VERIFY_SIGNATURE_FAILED);
        }
        //convert po
        CoreTransactionPO po = coreTransactionDao.queryByTxId(vo.getTxId(), false);
        if (po != null) {
            log.info("[submitTx]is idempotent txId:{}", vo.getTxId());
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_IDEMPOTENT);
        }
        //convert po
        po = BeanConvertor.convertBean(vo, CoreTransactionPO.class);
        po.setBizType(bizType.getCode());
        po.setVersion(vo.getVersion());
        if (vo.getBizModel() != null) {
            po.setBizModel(vo.getBizModel().toJSONString());
        }
        String actionDataJSON = JSON.toJSONString(vo.getActionList());
        po.setActionDatas(actionDataJSON);
        List<String> signDatas = new ArrayList<>();
        signDatas.add(signData);
        String signDataJSON = JSON.toJSONString(signDatas);
        po.setSignDatas(signDataJSON);
        po.setStatus(CoreTxStatusEnum.INIT.getCode());
        po.setCreateTime(new Date());
        try {
            coreTransactionDao.add(po);
        } catch (DuplicateKeyException e) {
            log.error("[submitTx]has idempotent error");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_IDEMPOTENT);
        }
    }

    @Override public void processInitTx(String txId) {
        log.info("[processInitTx]txId:{}", txId);
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                CoreTransactionPO po = coreTransactionDao.queryByTxId(txId, true);
                Date lockTime = po.getLockTime();
                if (lockTime != null && lockTime.after(new Date())) {
                    log.info("[processInitTx]should skip this tx by lock time:{}", lockTime);
                    return;
                }
                //convert bo
                CoreTxBO bo = convertTxBO(po);
                String policyId = bo.getPolicyId();
                log.info("[processInitTx]policyId:{}", policyId);
                Policy policy = policyRepository.getPolicyById(policyId);
                if (policy == null) {
                    log.error("[processInitTx]get policy is null by policyId:{}", policyId);
                    toEndAndCallBackByError(bo, CoreTxStatusEnum.INIT,RsCoreErrorEnum.RS_CORE_TX_POLICY_NOT_EXISTS_FAILED);
                    return;
                }
                List<String> rsIds = policy.getRsIds();
                List<String> otherSignDatas = null;
                boolean requireSigned = false;
                if (!CollectionUtils.isEmpty(rsIds)) {
                    requireSigned = true;
                    //get other rs sign datas
                    try{
                        otherSignDatas = getSignDataByOther(bo,rsIds);
                    }catch (Throwable t){
                        log.error("[processInitTx]getSignDataByOther is fail txId:{}", txId);
                        toEndAndCallBackByError(bo, CoreTxStatusEnum.INIT,RsCoreErrorEnum.RS_CORE_TX_POLICY_NOT_EXISTS_FAILED);
                        return;
                    }
                }
                //when require other rs sign
                if (requireSigned && !CollectionUtils.isEmpty(otherSignDatas)) {
                    List<String> signDatas = bo.getSignDatas();
                    signDatas.addAll(otherSignDatas);
                    String signJSON = JSON.toJSONString(signDatas);
                    //update sign data
                    int r = coreTransactionDao.updateSignDatas(txId, signJSON);
                    if (r != 1) {
                        log.error("[processInitTx]updateSignDatas is fail txId:{}", txId);
                        throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_SIGN_DATAS_FAILED);
                    }
                    log.info("[processInitTx]updateSignDatas is success txId:{}", txId);
                }
                //update status to WAIT
                int r = coreTransactionDao
                    .updateStatus(txId, CoreTxStatusEnum.INIT.getCode(), CoreTxStatusEnum.WAIT.getCode());
                if (r != 1) {
                    log.error(
                        "[processInitTx]update status from INIT to END is fail txId:{}",
                        txId);
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
                }
            }
        });
        log.info("[processInitTx]is success");
    }

    /**
     * convert transaction PO to BO
     *
     * @param po
     * @return
     */
    private CoreTxBO convertTxBO(CoreTransactionPO po){
        CoreTxBO bo = BeanConvertor.convertBean(po, CoreTxBO.class);
        if(bo == null){
            return null;
        }
        String bizType = po.getBizType();
        bo.setBizType(BizTypeEnum.fromCode(bizType));
        String version = po.getVersion();
        bo.setVersion(VersionEnum.getBizTypeEnumBycode(version));
        String bizModel = po.getBizModel();
        bo.setBizModel(JSON.parseObject(bizModel));
        String signJSON = po.getSignDatas();
        bo.setSignDatas(JSON.parseArray(signJSON, String.class));
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
    private CoreTxVO convertTxVO(CoreTxBO bo){
        CoreTxVO vo = BeanConvertor.convertBean(bo, CoreTxVO.class);
        if(vo == null){
            return null;
        }
        vo.setVersion(bo.getVersion().getCode());
        if(bo.getBizModel()!=null){
            bo.setBizModel(JSON.parseObject(bo.getBizModel().toJSONString()));
        }
        return vo;
    }

    /**
     * get sign data by other rs
     * @param bo
     * @param rsIds
     * @return
     */
    private List<String> getSignDataByOther(CoreTxBO bo, List<String> rsIds) {
        List<String> otherRs = new ArrayList<>(rsIds.size() - 1);
        for(String rs : rsIds){
            //filter self
            if(!StringUtils.equals(rs,rsConfig.getRsName())){
                otherRs.add(rs);
            }
        }
        if(CollectionUtils.isEmpty(otherRs)){
            return null;
        }
        List<String> signDatas = new ArrayList<>();
        for(String rs : otherRs){
            //TODO:to sign by other rs
        }
        return signDatas;
    }

    /**
     * to tx END status for fail business and call back custom rs
     *
     * @param bo
     * @param from
     * @param rsCoreErrorEnum
     */
    private void toEndAndCallBackByError(CoreTxBO bo,CoreTxStatusEnum from,RsCoreErrorEnum rsCoreErrorEnum){
        RespData respData = new RespData();
        respData.setCode(rsCoreErrorEnum.getCode());
        respData.setMsg(rsCoreErrorEnum.getDescription());
        toEndAndCallBackByError(bo,from,respData);
    }
    /**
     * to tx END status for fail business and call back custom rs
     *
     * @param bo
     * @param from
     * @param respData
     */
    private void toEndAndCallBackByError(CoreTxBO bo,CoreTxStatusEnum from,RespData respData){
        //save execute result and error code
        String txId = bo.getTxId();
        coreTransactionDao
            .saveExecuteResult(txId, CoreTxResultEnum.FAIL.getCode(),respData.getRespCode());
        //update status from INIT to END
        int r = coreTransactionDao
            .updateStatus(txId, from.getCode(), CoreTxStatusEnum.END.getCode());
        if (r != 1) {
            log.error("[toEndStatusForFail]policy is null,update status from INIT to END is fail txId:{}",
                txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
        }
        TxCallbackHandler txCallbackHandler = txCallbackRegistor.getCoreTxCallback();
        if(txCallbackHandler == null){
            log.error("[toEndStatusForFail]call back handler is not set");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_CORE_TX_CALLBACK_NOT_SET);
        }
        //save vo
        respData.setData(convertTxVO(bo));
        //callback custom rs
        txCallbackHandler.onEnd(bo.getBizType(),respData);
    }



    @Override public void submitToSlave() {
        int maxSize = 200;
        List<CoreTransactionPO> list = coreTransactionDao.queryByStatus(CoreTxStatusEnum.WAIT.getCode(),0,maxSize);
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        List<CoreTxBO> boList = new ArrayList<>(list.size());
        for (CoreTransactionPO po : list){
            boList.add(convertTxBO(po));
        }
        List<SignedTransaction> txs = makeTxs(boList);
        try {
            log.info("[submitToSlave] start");
            RespData respData = blockChainService.submitTransactions(txs);
            if(respData.getData() == null) {
                return;
            }
            //has fail tx
            List<TransactionVO> txsOfFail = (List<TransactionVO>)respData.getData();
            log.info("[submitToSlave] has fail tx:{}",txsOfFail);
            for(TransactionVO txVo : txsOfFail){
                if(!txVo.getRetry()){
                    CoreTransactionPO po = coreTransactionDao.queryByTxId(txVo.getTxId(),false);
                    CoreTxBO bo = convertTxBO(po);
                    //end
                    RespData mRes = new RespData();
                    //TODO:set error code
//                        mRes.setMsg(txVo.getErrMsg());
                    toEndAndCallBackByError(bo,CoreTxStatusEnum.WAIT,mRes);
                }
            }
        }catch (SlaveException e){
            log.error("[submitToSlave] has slave error",e);
        }catch (Throwable e){
            log.error("[submitToSlave] has unknown error",e);
        }
        log.info("[submitToSlave] end");
    }

    /**
     * make txs from core transaction
     *
     * @param list
     * @return
     */
    private List<SignedTransaction> makeTxs(List<CoreTxBO> list){
        List<SignedTransaction> txs = new ArrayList<>(list.size());
        for (CoreTxBO bo : list){
            SignedTransaction tx = new SignedTransaction();
            CoreTxVO vo = convertTxVO(bo);
            CoreTransaction coreTx = BeanConvertor.convertBean(vo,CoreTransaction.class);
            tx.setCoreTx(coreTx);
            tx.setSignatureList(bo.getSignDatas());
            txs.add(tx);
        }
        return txs;
    }
}
