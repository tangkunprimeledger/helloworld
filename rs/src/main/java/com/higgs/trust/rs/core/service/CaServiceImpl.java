package com.higgs.trust.rs.core.service;

import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.common.crypto.KeyPair;
import com.higgs.trust.common.utils.CryptoUtil;
import com.higgs.trust.common.utils.HashUtil;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.rs.common.enums.RespCodeEnum;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.CaService;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.integration.CaClient;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.CaVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.core.repository.RsNodeRepository;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.ca.CaAction;
import com.higgs.trust.slave.model.bo.config.Config;
import com.higgs.trust.slave.model.bo.manage.RsNode;
import com.higgs.trust.slave.model.bo.node.NodeAction;
import com.higgs.trust.slave.model.enums.UsageEnum;
import com.higgs.trust.slave.model.enums.biz.RsNodeStatusEnum;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author WangQuanzhou
 * @desc ca service
 * @date 2018/6/5 15:48
 */
@Service @Slf4j public class CaServiceImpl implements CaService {

    public static final String PUB_KEY = "pubKeyForConsensus";
    public static final String PRI_KEY = "priKey";

    private static final String SUCCESS = "sucess";
    private static final String FAIL = "fail";

    @Autowired private ConfigRepository configRepository;
    @Autowired private CaRepository caRepository;
    @Autowired private NodeState nodeState;
    @Autowired private CaClient caClient;
    @Autowired private CoreTransactionService coreTransactionService;
    @Autowired private RsNodeRepository rsNodeRepository;

    /**
     * @return
     * @desc generate pubKeyForConsensus and PriKey ,send CA auth request to other TRUST node,then insert into db
     */
    @Override public String authKeyPair(String user) {
        //check nodeName
        if (!nodeState.getNodeName().equals(user)) {
            log.error("[authKeyPair] invalid node name");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_INVALID_NODE_NAME_ERROR,
                "[authKeyPair] invalid node name");
        }

        log.info("[authKeyPair] start to auth CA pubKey/priKey, nodeName={}", user);
        // CA existence check
        Ca ca = caRepository.getCaForBiz(user);
        if (null != ca && ca.isValid()) {
            log.error("[authKeyPair] ca information for node={} already exist, pubKey={}", ca.getUser(),
                ca.getPubKey());
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CA_ALREADY_EXIST_ERROR,
                "[authKeyPair] ca information already exist");
        }

        // build pubKey and priKey
        List<CaVO> list = generateKeyPair();

        try {
            // send CA auth request
            RespData respData = null;
            if (nodeState.isState(NodeStateEnum.Offline)) {
                log.info("current node is Offline, send tx by other node");
                respData = caClient.caAuth(nodeState.notMeNodeNameReg(), list);
            }
            if (nodeState.isState(NodeStateEnum.Running)) {
                log.info("current node is Running, send tx by self");
                respData = authCaTx(list);
            }
            if (!respData.isSuccess()) {
                log.error("send tx error");
                return FAIL;
            }
        } catch (HystrixRuntimeException e1) {
            log.error("wait timeOut", e1);
            return FAIL;
        } catch (Throwable e2) {
            log.error("send ca auth error", e2);
            return FAIL;
        }

        // insert ca into db (for consensus layer)
        //   协议层的公私钥不更新   即非首次加入时，判断公私钥是否已经存在，存在就不做任何操作
        if (null != caRepository.getCaForConsensus(nodeState.getNodeName())) {
            log.info("not the first time for ca auth, ca for consensus layer already exist");
            return SUCCESS;
        }
        ca = new Ca();
        for (CaVO caVO : list) {
            if (UsageEnum.CONSENSUS.getCode().equals(caVO.getUsage())) {
                BeanUtils.copyProperties(caVO, ca);
            }
        }
        if (nodeState.isState(NodeStateEnum.Offline)) {
            //            ca.setValid(true);
            caRepository.insertCa(ca);
            log.info("insert ca end (for consensus layer)");
        }
        return SUCCESS;
    }

    /**
     * @return
     * @desc construct ca auth tx and send to slave
     */
    @Override public RespData authCaTx(List<CaVO> list) {
        //send and get callback result
        try {
            coreTransactionService.submitTx(constructAuthCoreTx(list));
        } catch (Throwable e) {
            log.error("send auth CA transaction error", e);
            return new RespData<>(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
        }
        return new RespData<>();
    }

    /**
     * @return
     * @desc update pubKey and PriKey for biz layer,then insert into db
     */
    @Override public RespData updateKeyPair(String user) {

        //check nodeName
        if (!nodeState.getNodeName().equals(user)) {
            log.error("[updateKeyPair] invalid node name");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_INVALID_NODE_NAME_ERROR,
                "[updateKeyPair] invalid node name");
        }

        // CA existence check
        Ca ca = caRepository.getCaForBiz(user);
        if (null == ca) {
            log.error("[updateKeyPair] ca information for node={} ", user);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CA_NOT_EXIST_ERROR,
                "[updateKeyPair] ca information doesn't exist");
        }

        log.info("[updateKeyPair] start to update CA pubKey/priKey, nodeName={}", user);
        // generate temp pubKeyForConsensus and priKey, insert into db
        CaVO caVO = generateTmpKeyPair(ca);

        // send CA update request
        return updateCaTx(caVO);
    }

    /**
     * @return
     * @desc construct ca update tx and send to slave
     */
    @Override public RespData updateCaTx(CaVO caVO) {

        //send and get callback result
        try {
            coreTransactionService.submitTx(constructUpdateCoreTx(caVO));
        } catch (Throwable e) {
            log.error("send update CA transaction error", e);
            return new RespData<>(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
        }
        return new RespData<>();
    }

    /**
     * @return
     * @desc cancel ca
     */
    @Override public RespData cancelKeyPair(String user) {

        //check nodeName
        if (!nodeState.getNodeName().equals(user)) {
            log.error("[cancelKeyPair] invalid node name");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_INVALID_NODE_NAME_ERROR,
                "[cancelKeyPair] invalid node name");
        }

        // check RS status, before cancel CA, RS should be canceled
        RsNode rsNode = rsNodeRepository.queryByRsId(user);
        if (null != rsNode && RsNodeStatusEnum.COMMON == rsNode.getStatus()) {
            log.error("[cancelKeyPair] invalid RS status, it should be canceled before CA");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CA_CANCEL_ERROR,
                "[cancelKeyPair] invalid RS status, it should be canceled before CA");
        }

        log.info("[cancelKeyPair] start to cancel CA, user={}", user);

        // CA existence check
        Ca ca = caRepository.getCaForBiz(user);
        if (null == ca) {
            log.error("[cancelKeyPair] ca information for node={} doesn't exist", user);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CA_NOT_EXIST_ERROR,
                "[cancelKeyPair] ca information doesn't exist");
        }

        //construct caVO
        CaVO caVO = new CaVO();
        caVO.setReqNo(HashUtil.getSHA256S(ca.getPubKey() + InitPolicyEnum.CA_CANCEL.getType()));
        caVO.setUser(user);
        caVO.setPeriod(ca.getPeriod());
        caVO.setPubKey(ca.getPubKey());
        caVO.setUsage(UsageEnum.BIZ.getCode());

        // send CA cancel request
        return cancelCaTx(caVO);
    }

    /**
     * @return
     * @desc construct ca cancel tx and send to slave
     */
    @Override public RespData cancelCaTx(CaVO caVO) {
        //send and get callback result
        try {
            coreTransactionService.submitTx(constructCancelCoreTx(caVO));
        } catch (Throwable e) {
            log.error("send cancel CA transaction error", e);
            return new RespData<>(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
        }
        return new RespData<>();
    }

    /**
     * @return
     * @desc after ca tx has bean authoritied by the current cluster, then update table config column valid to TRUE
     */
    @Override public void callbackCa() {

    }

    /**
     * @param user
     * @return
     * @desc acquire CA information by user
     */
    @Override public RespData<Ca> acquireCA(String user) {
        if (StringUtils.isEmpty(user)) {
            log.info("[acquireCA] param is null");
            return null;
        }
        Ca ca = caRepository.getCaForBiz(user);
        if (null == ca) {
            log.info("[acquireCA] user={}, ca information is null", user);
            return new RespData<>();
        }
        log.info("[acquireCA] user={}, ca information={}", user, ca.toString());
        RespData resp = new RespData();
        resp.setData(ca);
        return resp;
    }

    private CoreTransaction constructAuthCoreTx(List<CaVO> list) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(list.get(0).getReqNo());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setPolicyId(InitPolicyEnum.CA_AUTH.getPolicyId());
        coreTx.setActionList(buildAuthActionList(list));
        return coreTx;
    }

    private List<Action> buildAuthActionList(List<CaVO> list) {
        // first join list.size=2, not the first join list.size=1
        if (list.size() != 2 && list.size() != 1) {
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CA_AUTH_ERROR, "ca auth, list size illegal");
        }
        List<Action> actions = new ArrayList<>();
        int index = 0;
        for (CaVO caVO : list) {
            CaAction caAction = new CaAction();
            caAction.setPeriod(caVO.getPeriod());
            caAction.setPubKey(caVO.getPubKey());
            caAction.setUsage(caVO.getUsage());
            caAction.setVersion(VersionEnum.V1.getCode());
            caAction.setUser(caVO.getUser());
            caAction.setValid(true);
            caAction.setType(ActionTypeEnum.CA_AUTH);
            caAction.setIndex(index);
            actions.add(caAction);
            index++;
        }

        NodeAction nodeAction = new NodeAction();
        nodeAction.setNodeName(list.get(0).getUser());
        nodeAction.setType(ActionTypeEnum.NODE_JOIN);
        nodeAction.setIndex(index);
        actions.add(nodeAction);

        return actions;
    }

    private CoreTransaction constructUpdateCoreTx(CaVO caVO) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(caVO.getReqNo());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setPolicyId(InitPolicyEnum.CA_UPDATE.getPolicyId());
        coreTx.setActionList(buildUpdateActionList(caVO));
        return coreTx;
    }

    private List<Action> buildUpdateActionList(CaVO caVO) {
        List<Action> actions = new ArrayList<>();
        CaAction caAction = new CaAction();
        caAction.setPeriod(caVO.getPeriod());
        caAction.setPubKey(caVO.getPubKey());
        caAction.setUsage(UsageEnum.BIZ.getCode());
        caAction.setUser(caVO.getUser());
        caAction.setType(ActionTypeEnum.CA_UPDATE);
        caAction.setIndex(0);
        caAction.setValid(true);
        actions.add(caAction);
        return actions;
    }

    private CoreTransaction constructCancelCoreTx(CaVO caVO) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(caVO.getReqNo());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setPolicyId(InitPolicyEnum.CA_CANCEL.getPolicyId());
        coreTx.setActionList(buildCancelActionList(caVO));
        return coreTx;
    }

    private List<Action> buildCancelActionList(CaVO caVO) {
        List<Action> actions = new ArrayList<>();
        CaAction caAction = new CaAction();
        caAction.setUser(caVO.getUser());
        caAction.setType(ActionTypeEnum.CA_CANCEL);
        caAction.setIndex(0);
        caAction.setValid(false);
        caAction.setPeriod(caVO.getPeriod());
        caAction.setPubKey(caVO.getPubKey());
        caAction.setUsage(caVO.getUsage());
        actions.add(caAction);
        return actions;
    }

    private CaVO generateTmpKeyPair(Ca ca) {

        // generate temp pubKeyForConsensus and priKey and insert into db
        log.info("[generateTmpKeyPair] start to generate tempKeyPairs");
        Crypto crypto = CryptoUtil.getBizCrypto();
        KeyPair keyPair = crypto.generateKeyPair();
        String pubKey = keyPair.getPubKey();
        String priKey = keyPair.getPriKey();
        //store temp pubKeyForConsensus and priKey
        Config config = configRepository.getBizConfig(ca.getUser());
        config.setTmpPubKey(pubKey);
        config.setTmpPriKey(priKey);
        config.setValid(true);
        configRepository.updateConfig(config);

        //construct caVO
        CaVO caVO = new CaVO();
        caVO.setVersion(VersionEnum.V1.getCode());
        caVO.setPeriod(calculatePeriod());
        caVO.setPubKey(pubKey);
        caVO.setReqNo(HashUtil.getSHA256S(ca.getPubKey() + InitPolicyEnum.CA_UPDATE.getType()));
        caVO.setUsage(UsageEnum.BIZ.getCode());
        caVO.setUser(ca.getUser());

        return caVO;
    }

    private Date calculatePeriod() {
        Calendar calendar = Calendar.getInstance();
        // default 1 year later
        calendar.add(Calendar.YEAR, 1);
        return calendar.getTime();
    }

    @Override public Ca getCa(String user) {
        if (StringUtils.isEmpty(user)) {
            log.info("[getCaForBiz] user is null");
            return null;
        }

        log.info("[getCaForBiz] start to getCaForBiz, user={}", user);
        RespData resp = caClient.acquireCA(nodeState.notMeNodeNameReg(), user);
        if (!resp.isSuccess() || null == resp.getData()) {
            log.error("[getCaForBiz] get ca error");
            return null;
        }
        Ca ca = new Ca();
        log.info("[getCaForBiz] success getCaForBiz, resp={}", resp.getData());
        BeanUtils.copyProperties((Ca)resp.getData(), ca);

        return ca;
    }

    private List<CaVO> generateKeyPair() {

        List<CaVO> list = new LinkedList<>();
        String reqNo;

        // generate KeyPair for consensus layer
        Crypto consensusCrypto = CryptoUtil.getProtocolCrypto();
        KeyPair keyPair = consensusCrypto.generateKeyPair();
        String pubKey = keyPair.getPubKey();
        String priKey = keyPair.getPriKey();
        //store pubKeyForConsensus and priKey
        Config config = new Config();
        config.setNodeName(nodeState.getNodeName());
        config.setPubKey(pubKey);
        config.setPriKey(priKey);
        config.setValid(true);
        config.setVersion(VersionEnum.V1.getCode());
        config.setUsage(UsageEnum.CONSENSUS.getCode());
        if (nodeState.isState(NodeStateEnum.Offline)) {
            configRepository.insertConfig(config);
        }
        //construct caVO for consensus layer
        CaVO caVO1 = new CaVO();
        caVO1.setVersion(VersionEnum.V1.getCode());
        caVO1.setPeriod(calculatePeriod());
        caVO1.setPubKey(pubKey);
        caVO1.setUsage(UsageEnum.CONSENSUS.getCode());
        caVO1.setUser(nodeState.getNodeName());

        // generate KeyPair for biz layer
        Crypto bizCrypto = CryptoUtil.getBizCrypto();
        keyPair = bizCrypto.generateKeyPair();
        pubKey = keyPair.getPubKey();
        priKey = keyPair.getPriKey();
        //store pubKeyForConsensus and priKey
        config = new Config();
        config.setNodeName(nodeState.getNodeName());
        config.setPubKey(pubKey);
        config.setPriKey(priKey);
        config.setValid(true);
        config.setVersion(VersionEnum.V1.getCode());
        config.setUsage(UsageEnum.BIZ.getCode());
        configRepository.insertConfig(config);
        //construct caVO for biz layer
        CaVO caVO2 = new CaVO();
        caVO2.setVersion(VersionEnum.V1.getCode());
        caVO2.setPeriod(calculatePeriod());
        caVO2.setPubKey(pubKey);
        caVO2.setUsage(UsageEnum.BIZ.getCode());
        caVO2.setUser(nodeState.getNodeName());

        if (nodeState.isState(NodeStateEnum.Offline)) {
            reqNo = HashUtil.getSHA256S(caVO1.getPubKey() + caVO2.getPubKey() + caVO2.getUser());
            list.add(caVO1);
        } else {
            reqNo = HashUtil.getSHA256S(caVO2.getPubKey() + caVO2.getUser());
        }
        caVO1.setReqNo(reqNo);
        caVO2.setReqNo(reqNo);

        list.add(caVO2);
        return list;
    }

}
