package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.dao.CoreTransactionDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.vo.CoreTxVO;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.repository.PolicyRepository;
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

    @Override public void submitTx(CoreTxVO vo) {
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
        //check sign data of self
        if(CollectionUtils.isEmpty(vo.getSignDatas())){
            log.error("[submitTx] self sign data is empty");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_VERIFY_SIGNATURE_FAILED);
        }
        //convert bo
        CoreTxBO bo = BeanConvertor.convertBean(vo, CoreTxBO.class);
        CoreTransactionPO po = coreTransactionDao.queryByTxId(bo.getTxId(), false);
        if (po != null) {
            log.info("[submitTx]is idempotent txId:{}", bo.getTxId());
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_IDEMPOTENT);
        }
        //convert po
        po = BeanConvertor.convertBean(bo, CoreTransactionPO.class);
        po.setBizType(bo.getBizType().getCode());
        po.setVersion(bo.getVersion().getCode());
        if (bo.getBizModel() != null) {
            po.setBizModel(bo.getBizModel().toJSONString());
        }
        String actionDatas = JSON.toJSONString(bo.getActionDatas());
        po.setActionDatas(actionDatas);
        String signDatas = JSON.toJSONString(bo.getSignDatas());
        po.setSignDatas(signDatas);
        po.setStatus(CoreTxStatusEnum.INIT.getCode());
        po.setCreateTime(new Date());
        try {
            coreTransactionDao.add(po);
        } catch (DuplicateKeyException e) {
            log.error("[submitTx]has idempotent error");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_IDEMPOTENT);
        }
    }

    @Override public void processSignData(String txId) {
        log.info("[processSignData]txId:{}", txId);
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                CoreTransactionPO po = coreTransactionDao.queryByTxId(txId, true);
                Date lockTime = po.getLockTime();
                if (lockTime != null && lockTime.after(new Date())) {
                    log.info("[processSignData]should skip this tx by lock time:{}", lockTime);
                    return;
                }
                //convert bo
                CoreTxBO bo = convertTxBO(po);
                String policyId = bo.getPolicyId();
                log.info("[processSignData]policyId:{}", policyId);
                Policy policy = policyRepository.getPolicyById(policyId);
                if (policy == null) {
                    log.error("[processSignData]get policy is null by policyId:{}", policyId);
                    toEndAndCallBackByError(txId, CoreTxStatusEnum.INIT,RsCoreErrorEnum.RS_CORE_TX_POLICY_NOT_EXISTS_FAILED);
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
                        log.error("[processSignData]getSignDataByOther is fail txId:{}", txId);
                        toEndAndCallBackByError(txId, CoreTxStatusEnum.INIT,RsCoreErrorEnum.RS_CORE_TX_POLICY_NOT_EXISTS_FAILED);
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
                        log.error("[processSignData]updateSignDatas is fail txId:{}", txId);
                        throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_SIGN_DATAS_FAILED);
                    }
                    log.info("[processSignData]updateSignDatas is success txId:{}", txId);
                }
                //update status to WAIT
                int r = coreTransactionDao
                    .updateStatus(txId, CoreTxStatusEnum.INIT.getCode(), CoreTxStatusEnum.WAIT.getCode());
                if (r != 1) {
                    log.error(
                        "[processSignData]update status from INIT to END is fail txId:{}",
                        txId);
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
                }
            }
        });
        log.info("[processSignData]is success");
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
        bo.setActionDatas(JSON.parseArray(actionJSON, Action.class));
        return bo;
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
     * @param txId
     * @param from
     * @param coreErrorEnum
     */
    private void toEndAndCallBackByError(String txId,CoreTxStatusEnum from,RsCoreErrorEnum coreErrorEnum){
        //save execute result and error code
        coreTransactionDao
            .saveExecuteResult(txId, CoreTxResultEnum.FAIL.getCode(),coreErrorEnum.getCode());
        //update status from INIT to END
        int r = coreTransactionDao
            .updateStatus(txId, from.getCode(), CoreTxStatusEnum.END.getCode());
        if (r != 1) {
            log.error("[toEndStatusForFail]policy is null,update status from INIT to END is fail txId:{}",
                txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
        }
        //TODO:callback custom rs
    }



    @Override public void submitToSlave() {
        int maxSize = 100;
        List<CoreTransactionPO> list = coreTransactionDao.queryByStatus(CoreTxStatusEnum.WAIT.getCode(),0,maxSize);
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        List<CoreTxBO> boList = new ArrayList<>(maxSize);
        for (CoreTransactionPO po : list){
            boList.add(convertTxBO(po));
        }


    }
}
