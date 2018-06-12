package com.higgs.trust.rs.custom.biz.api.impl.identity;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.custom.api.enums.ActionTypeEnum;
import com.higgs.trust.rs.custom.api.enums.RequestStatusEnum;
import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.rs.custom.api.identity.IdentityService;
import com.higgs.trust.rs.custom.api.vo.identity.IdentityVO;
import com.higgs.trust.rs.custom.dao.BankChainRequestDAO;
import com.higgs.trust.rs.custom.dao.identity.IdentityDAO;
import com.higgs.trust.rs.custom.dao.identity.IdentityRequestDAO;
import com.higgs.trust.rs.custom.dao.po.BankChainRequestPO;
import com.higgs.trust.rs.custom.dao.po.identity.IdentityPO;
import com.higgs.trust.rs.custom.dao.po.identity.IdentityRequestPO;
import com.higgs.trust.rs.custom.model.RespData;
import com.higgs.trust.rs.custom.model.bo.BankChainRequest;
import com.higgs.trust.rs.custom.model.bo.identity.IdentityRequest;
import com.higgs.trust.rs.custom.model.convertor.identity.BOToPOConvertor;
import com.higgs.trust.rs.custom.model.convertor.identity.POToBOConvertor;
import com.higgs.trust.rs.custom.model.convertor.identity.POToVOConvertor;
import com.higgs.trust.rs.custom.util.Profiler;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/*
 * @desc TODO
 * @author WangQuanzhou
 * @date 2018/3/2 19:49
 */
@Service @Slf4j public class IdentityServiceImpl implements IdentityService {

    private static final Logger PERF_LOGGER = LoggerFactory.getLogger("BANKCHAIN-PERF");

    @Autowired private IdentityRequestDAO identityRequestDAO;

    @Autowired private BankChainRequestDAO bankChainRequestDAO;

    @Autowired private IdentityDAO identityDAO;

    @Autowired TransactionTemplate txRequired;

    @Autowired private CoreTransactionService coreTransactionService;

    @Autowired private NodeState nodeState;

    @Override public RespData acceptRequest(IdentityRequest identityRequest) {
        if (null == identityRequest) {
            log.error("[acceptRequest]请求参数为空");
            return new RespData(RespCodeEnum.PARAM_CHECK_EMPTY_VALID);
        }

        log.info("[acceptRequest]: start handle reqNo={},key={}", identityRequest.getReqNo(), identityRequest.getKey());

        try {
            // 性能日志开始
            Profiler.start("[acceptRequest] accept request start");

            BankChainRequestPO bankChainRequestPO = new BankChainRequestPO();
            bankChainRequestPO.setBizType(BizTypeEnum.STORAGE.getCode());
            bankChainRequestPO.setReqNo(identityRequest.getReqNo());
            bankChainRequestPO.setStatus(RequestStatusEnum.INIT.getCode());
            try {
                //开启事务
                txRequired.execute(new TransactionCallbackWithoutResult() {
                    @Override protected void doInTransactionWithoutResult(TransactionStatus status) {

                        log.info("[acceptRequest] transaction start，reqNo={}", identityRequest.getReqNo());
                        // 插入存证请求的request
                        bankChainRequestDAO.insertRequest(bankChainRequestPO);

                        // 插入存证请求的key-value数据
                        identityRequestDAO
                            .insertIdentityRequest(BOToPOConvertor.convertIdentityRequestBOToPO(identityRequest));

                        log.info("[acceptRequest] transaction success，reqNo={}", identityRequest.getReqNo());
                    }
                });
            } catch (DuplicateKeyException e) {
                log.error("[acceptRequest]请求幂等,reqNo={}", identityRequest.getReqNo(), e);
                return new RespData(RespCodeEnum.REQUEST_REPEAT_CHECK_VALID);
            } catch (Throwable e) {
                log.error("[acceptRequest]系统异常,reqNo={}", identityRequest.getReqNo(), e);
                return new RespData(RespCodeEnum.SYS_FAIL);
            }
        } finally {
            // 性能日志结束
            Profiler.release();
            if (PERF_LOGGER.isInfoEnabled() && Profiler.getDuration() > 0) {
                PERF_LOGGER.info(Profiler.dump());
            }
        }
        log.info("[acceptRequest]: end handle reqNo={}, key={}", identityRequest.getReqNo(), identityRequest.getKey());
        return new RespData(RespCodeEnum.ASYNC_SEND_IDENTITY_REQUEST);
    }

    @Override public RespData queryIdentityByKey(String key) {
        if (StringUtils.isBlank(key)) {
            log.error("[queryIdentityByKey]请求参数为空");
            return new RespData(RespCodeEnum.PARAM_CHECK_EMPTY_VALID);
        }

        log.info("[queryIdentityByKey]: start handle , key = {}", key);
        IdentityPO identityPO = null;
        try {
            // 性能日志开始
            Profiler.start("[queryIdentityByKey] query Identity By Key start");

            identityPO = identityDAO.queryIdentityByKey(key);
            if (null == identityPO || !identityPO.getKey().equals(key)) {
                log.info("[queryIdentityByKey]存证数据不存在,key={}", key);
                return new RespData(RespCodeEnum.IDENTITY_NOT_EXIST);
            }

            identityPO.setStatus(RequestStatusEnum.SUCCESS.getCode());
        } finally {
            // 性能日志结束
            Profiler.release();
            if (PERF_LOGGER.isInfoEnabled() && Profiler.getDuration() > 0) {
                PERF_LOGGER.info(Profiler.dump());
            }
        }

        log.info("[queryIdentityByKey]: end handle , key = {}", key);
        IdentityVO identityVO = POToVOConvertor.convertidentityPOToVO(identityPO);
        RespData respData = new RespData(RespCodeEnum.GET_IDENTITY_REQUEST_SUCCESS);
        respData.setData(identityVO);
        return respData;
    }

    @Override public RespData queryIdentityByReqNo(String reqNo) {
        if (StringUtils.isBlank(reqNo)) {
            log.error("[queryIdentityByReqNo]请求参数为空");
            return new RespData(RespCodeEnum.PARAM_CHECK_EMPTY_VALID);
        }

        log.info("[queryIdentityByReqNo]: start handle , reqNo = {}", reqNo);
        try {
            // 性能日志开始
            Profiler.start("[queryIdentityByReqNo] query Identity By reqNo start");

            // 根据reqNo从bankchain_request表取数据
            BankChainRequestPO bankChainRequestPO = bankChainRequestDAO.queryRequestByReqNo(reqNo);
            if (null == bankChainRequestPO || !bankChainRequestPO.getReqNo().equals(reqNo)) {
                log.info("[queryIdentityByReqNo]存证数据不存在,reqNo={}", reqNo);
                return new RespData(RespCodeEnum.IDENTITY_NOT_EXIST);
            }

            log.info("[queryIdentityByReqNo]: end handle , reqNo = {}", reqNo);
            // 根据reqNo从identity_request表取数据
            IdentityRequestPO identityRequestPO = identityRequestDAO.queryIdentityRequest(reqNo);
            // 组装返回数据
            IdentityVO identityVO = new IdentityVO();
            identityVO.setReqNo(reqNo);
            identityVO.setKey(identityRequestPO.getKey());
            identityVO.setValue(identityRequestPO.getValue());
            identityVO.setStatus(bankChainRequestPO.getStatus());
            RespData respData = new RespData(RespCodeEnum.GET_IDENTITY_REQUEST_SUCCESS);
            respData.setData(identityVO);
            return respData;
        } finally {
            // 性能日志结束
            Profiler.release();
            if (PERF_LOGGER.isInfoEnabled() && Profiler.getDuration() > 0) {
                PERF_LOGGER.info(Profiler.dump());
            }
        }

    }

    /*
     * @desc 异步下发存证业务数据
     * @param   BankChainRequest
     * @return
     */
    @Override public void asyncSendIdentity(BankChainRequest bankChainRequest) {
        if (null == bankChainRequest) {
            log.warn("[asyncSendIdentity]异步下发,请求数据为空");
            return;
        }
        log.info("[asyncSendIdentity]: start handle , reqNo = {}", bankChainRequest.getReqNo());

        try {
            // 性能日志开始
            Profiler.start("[asyncSendIdentity] query Identity By reqNo start");
            IdentityRequest identityRequest = getRequestData(bankChainRequest);

            if (null == identityRequest) {
                log.error("[asyncSendIdentity]异步下发的数据为空");
                return;
            }

            //开启事务
            txRequired.execute(status -> {

                //根据reqNo修改request状态，从INIT修改为PROCESSING
                bankChainRequestDAO.updateRequestToProc(identityRequest.getReqNo());

                //下发 slave
                asyncSendToSlave(identityRequest);

                log.info("[asyncSendIdentity] transaction success，reqNo={}", bankChainRequest.getReqNo());
                return RespCodeEnum.ASYNC_SEND_IDENTITY_REQUEST;
            });
            log.info("[asyncSendIdentity]: end handle , reqNo = {}", bankChainRequest.getReqNo());
        } finally {
            // 性能日志结束
            Profiler.release();
            if (PERF_LOGGER.isInfoEnabled() && Profiler.getDuration() > 0) {
                PERF_LOGGER.info(Profiler.dump());
            }
        }

    }

    private IdentityRequest getRequestData(BankChainRequest bankChainRequest) {
        if (StringUtils.isBlank(bankChainRequest.getReqNo())) {
            log.warn("[getRequestData] reqNo is null");
            return null;
        }

        log.info("[getRequestData]: start handle , reqNo = {}", bankChainRequest.getReqNo());
        IdentityRequestPO identityRequestPO = identityRequestDAO.queryIdentityRequest(bankChainRequest.getReqNo());

        //  对于不需要更新数据的情况下  首先判断数据是否已经存在  存在就不做任何更改
        //  否则和普通的存证请求一样的走下发流程
        if (ActionTypeEnum.PRESERVE.getType().equals(identityRequestPO.getFlag())) {
            IdentityPO identityPO = identityDAO.queryIdentityByKey(identityRequestPO.getKey());
            if (null != identityPO) {
                log.info("[getRequestData] data has already exists ,key={}", identityRequestPO.getKey());
                //  将重复的请求数据的状态为修改为DUPLICATE，避免定时任务重复扫描处理
                bankChainRequestDAO.updateRequestToDuplicate(identityRequestPO.getReqNo());
                return null;
            }
        }
        log.info("[getRequestData]: end handle , reqNo = {}", bankChainRequest.getReqNo());
        return POToBOConvertor.convertIdentityRequestPOToBO(identityRequestPO);
    }

    private void asyncSendToSlave(IdentityRequest identityRequest) {
        //下面组装数据  下发到slave
        if (null == identityRequest) {
            log.error("[asyncSendToSlave]异步下发slave,入参为空");
            return;
        }

        log.info("[asyncSendToSlave]: start handle , reqNo = {}", identityRequest.getReqNo());
        //2.获取policyId
//        String policyId = policyDao.queryByPolicyId("api.user.storageIdentity");

        //3.组装bizModel
        JSONObject bizModel = new JSONObject();
        bizModel.put("reqNo", identityRequest.getReqNo());
        bizModel.put("key", identityRequest.getKey());
        bizModel.put("value", identityRequest.getValue());
        bizModel.put("flag", identityRequest.getFlag());

        // 组装CoreTransaction
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setBizModel(bizModel);
        coreTx.setPolicyId("api.user.storageIdentity");
        coreTx.setTxId(identityRequest.getReqNo());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTransactionService.submitTx(BizTypeEnum.STORAGE, coreTx);
        log.info("[asyncSendToSlave]: end handle , reqNo = {}", identityRequest.getReqNo());
    }
}
