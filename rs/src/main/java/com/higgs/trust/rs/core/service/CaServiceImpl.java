package com.higgs.trust.rs.core.service;

import com.higgs.trust.common.utils.HashUtil;
import com.higgs.trust.common.utils.KeyGeneratorUtils;
import com.higgs.trust.consensus.config.NodeState;
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
import com.higgs.trust.slave.model.enums.biz.RsNodeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 15:48
 */
@Service @Slf4j public class CaServiceImpl implements CaService {

    public static final String PUB_KEY = "pubKey";
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
     * @desc generate pubKey and PriKey ,then insert into db
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
        Ca ca = caRepository.getCa(user);
        if (null != ca) {
            log.error("[authKeyPair] ca information for node={} already exist, pubKey={}", ca.getUser(),
                ca.getPubKey());
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CA_ALREADY_EXIST_ERROR,
                "[authKeyPair] ca information already exist");
        }

        // build pubKey and priKey
        CaVO caVO = generateKeyPair();

        // send CA auth request
        RespData respData = caClient.caAuth(nodeState.notMeNodeNameReg(), caVO);
        if (respData.isSuccess()) {
            log.error("send tx error");
            return FAIL;
        }

        return SUCCESS;
    }

    /**
     * @return
     * @desc construct ca auth tx and send to slave
     */
    @Override public RespData authCaTx(CaVO caVO) {
        //send and get callback result
        try {
            coreTransactionService.submitTx(constructAuthCoreTx(caVO));
        } catch (Throwable e) {
            log.error("send auth CA transaction error", e);
            return new RespData<>(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
        }
        return new RespData<>();
    }

    /**
     * @return
     * @desc update pubKey and PriKey ,then insert into db
     */
    @Override public RespData updateKeyPair(String user) {

        //check nodeName
        if (!nodeState.getNodeName().equals(user)) {
            log.error("[updateKeyPair] invalid node name");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_INVALID_NODE_NAME_ERROR,
                "[updateKeyPair] invalid node name");
        }

        // CA existence check
        Ca ca = caRepository.getCa(user);
        if (null == ca) {
            log.error("[updateKeyPair] ca information for node={} ", user);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CA_NOT_EXIST_ERROR,
                "[updateKeyPair] ca information doesn't exist");
        }

        log.info("[updateKeyPair] start to update CA pubKey/priKey, nodeName={}", user);
        // generate temp pubKey and priKey, insert into db
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
            log.error("send auth CA transaction error", e);
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
        Ca ca = caRepository.getCa(user);
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
        Ca ca = caRepository.getCa(user);
        if (null == ca) {
            log.info("[acquireCA] user={}, ca information is null", user);
            return new RespData<>();
        }
        log.info("[acquireCA] user={}, ca information={}", user, ca.toString());
        RespData resp = new RespData();
        resp.setData(ca);
        return resp;
    }

    private CoreTransaction constructAuthCoreTx(CaVO caVO) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(caVO.getReqNo());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setPolicyId(InitPolicyEnum.CA_AUTH.getPolicyId());
        coreTx.setActionList(buildAuthActionList(caVO));
        return coreTx;
    }

    private List<Action> buildAuthActionList(CaVO caVO) {
        List<Action> actions = new ArrayList<>();
        CaAction caAction = new CaAction();
        caAction.setPeriod(caVO.getPeriod());
        caAction.setPubKey(caVO.getPubKey());
        caAction.setUsage(caVO.getUsage());
        caAction.setVersion(VersionEnum.V1.getCode());
        caAction.setUser(caVO.getUser());
        caAction.setValid(true);
        caAction.setType(ActionTypeEnum.CA_AUTH);
        caAction.setIndex(0);
        actions.add(caAction);

        NodeAction nodeAction = new NodeAction();
        nodeAction.setNodeName(caVO.getUser());
        nodeAction.setType(ActionTypeEnum.NODE_JOIN);
        nodeAction.setIndex(1);
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
        caAction.setUsage(caVO.getUsage());
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
        actions.add(caAction);
        return actions;
    }

    private CaVO generateTmpKeyPair(Ca ca) {

        // generate temp pubKey and priKey and insert into db
        Map<String, String> map = null;
        try {
            map = KeyGeneratorUtils.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            log.error("[generateTmpKeyPair] generate pubKey/priKey has error, no such algorithm");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_GENERATE_KEY_ERROR,
                "[generateTmpKeyPair] generate pubKey/priKey has error, no such algorithm");
        }
        log.info("[generateTmpKeyPair] start to generate tempKeyPairs");
        String pubKey = map.get(PUB_KEY);
        String priKey = map.get(PRI_KEY);
        //store temp pubKey and priKey
        Config config = new Config();
        config.setNodeName(ca.getUser());
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
        caVO.setUsage("consensus");
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
            log.info("[getCa] user is null");
            return null;
        }

        log.info("[getCa] start to getCa, user={}", user);
        RespData resp = caClient.acquireCA(nodeState.notMeNodeNameReg(), user);
        if (!resp.isSuccess() || null == resp.getData()) {
            log.error("[getCa] get ca error");
            return null;
        }
        Ca ca = new Ca();
        log.info("[getCa] success getCa, resp={}", resp.getData());
        BeanUtils.copyProperties((Ca)resp.getData(), ca);

        return ca;
    }

    private CaVO generateKeyPair() {

        // generate pubKey and priKey
        Map<String, String> map = null;
        try {
            map = KeyGeneratorUtils.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            log.error("[generateKeyPair] generate pubKey/priKey has error, no such algorithm");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_GENERATE_KEY_ERROR,
                "[generateKeyPair] generate pubKey/priKey has error, no such algorithm");
        }
        String pubKey = map.get(PUB_KEY);
        String priKey = map.get(PRI_KEY);
        //store pubKey and priKey
        Config config = new Config();
        config.setNodeName(nodeState.getNodeName());
        config.setPubKey(pubKey);
        config.setPriKey(priKey);
        config.setValid(true);
        config.setVersion(VersionEnum.V1.getCode());
        configRepository.insertConfig(config);

        //construct caVO
        CaVO caVO = new CaVO();
        caVO.setVersion(VersionEnum.V1.getCode());
        caVO.setPeriod(calculatePeriod());
        caVO.setPubKey(pubKey);
        caVO.setReqNo(HashUtil.getSHA256S(pubKey));
        caVO.setUsage("consensus");
        caVO.setUser(nodeState.getNodeName());

        return caVO;
    }

}
