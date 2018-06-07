package com.higgs.trust.rs.core.service;

import com.higgs.trust.common.utils.HashUtil;
import com.higgs.trust.common.utils.KeyGeneratorUtils;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.CaService;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.integration.CaClient;
import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.vo.CaVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.ca.CaAction;
import com.higgs.trust.slave.model.bo.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 15:48
 */
@Service @Slf4j public class CaServiceImpl implements CaService {

    public static final String PUB_KEY = "pubKey";
    public static final String PRI_KEY = "priKey";

    @Autowired ConfigRepository configRepository;
    @Autowired NodeState nodeState;
    @Autowired private CaClient caClient;
    @Autowired private CoreTransactionService coreTransactionService;
    @Value("${bftSmart.systemConfigs.myId}") private String myId;

    /**
     * @return
     * @desc generate pubKey and PriKey ,then insert into db
     */
    @Override public void initKeyPair(String user) {
        //check nodeName
        if (!nodeState.getNodeName().equals(user)) {
            log.error("[initKeyPair] invalid node name");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_INVALID_NODE_NAME_EXIST_ERROR,
                "[initKeyPair] invalid node name");
        }

        // TODO 注册CA之前，应该检测CA是否存在

        // generate pubKey and priKey
        Map<String, String> map = null;
        try {
            map = KeyGeneratorUtils.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            log.error("[initKeyPair] generate pubKey/priKey has error, no such algorithm");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_GENERATE_KEY_ERROR,
                "[initKeyPair] generate pubKey/priKey has error, no such algorithm");
        }
        String pubKey = map.get(PUB_KEY);
        String priKey = map.get(PRI_KEY);
        //store pubKey and priKey
        Config config = new Config();
        config.setNodeName(nodeState.getNodeName());
        config.setPubKey(pubKey);
        config.setPriKey(priKey);
        config.setValid("FALSE");
        config.setVersion(VersionEnum.V1.getCode());
        configRepository.insertConfig(config);

        //construct caVO
        CaVO caVO = new CaVO();
        caVO.setVersion(VersionEnum.V1.getCode());
        caVO.setPeriod(new Date());
        caVO.setPubKey(pubKey);
        caVO.setReqNo(HashUtil.getSHA256S(pubKey));
        caVO.setUsage("consensus");
        caVO.setUser(nodeState.getNodeName());

        // send CA auth request
        caClient.caAuth(nodeState.getNodeName(), caVO);
    }

    /**
     * @return
     * @desc construct ca tx and send to slave
     */
    @Override public RespData authCaTx(CaVO caVO) {
        RespData respData;

        //send and get callback result
        try {
            coreTransactionService.submitTx(BizTypeEnum.CA_AUTH, constructAuthCoreTx(caVO));
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                log.error("create bill error", e);
                return new RespData<>(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
            }
        }
        return new RespData<>();
    }

    /**
     * @return
     * @desc generate pubKey and PriKey ,then insert into db
     */
    @Override public void updateKeyPair(String user) {

        //check nodeName
        if (!nodeState.getNodeName().equals(user)) {
            log.error("[updateKeyPair] invalid node name");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_INVALID_NODE_NAME_EXIST_ERROR,
                "[updateKeyPair] invalid node name");
        }

        // TODO 更新CA之前，应该检测CA是否存在，存在就可以更新，否则不可以

        // generate pubKey and priKey
        Map<String, String> map = null;
        try {
            map = KeyGeneratorUtils.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            log.error("[updateKeyPair] generate pubKey/priKey has error, no such algorithm");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_GENERATE_KEY_ERROR,
                "[updateKeyPair] generate pubKey/priKey has error, no such algorithm");
        }
        String pubKey = map.get(PUB_KEY);
        String priKey = map.get(PRI_KEY);
        //store temp pubKey and priKey
        Config config = new Config();
        config.setNodeName(nodeState.getNodeName());
        config.setTmpPubKey(pubKey);
        config.setTmpPriKey(priKey);
        configRepository.updateConfig(config);

        //construct caVO
        CaVO caVO = new CaVO();
        caVO.setVersion(VersionEnum.V1.getCode());
        caVO.setPeriod(new Date());
        caVO.setPubKey(pubKey);
        caVO.setReqNo(HashUtil.getSHA256S(pubKey));
        caVO.setUsage("consensus");
        caVO.setUser(nodeState.getNodeName());

        // send CA auth request
        caClient.caUpdate(nodeState.getNodeName(), caVO);
    }

    /**
     * @return
     * @desc construct ca tx and send to slave
     */
    @Override public RespData updateCaTx(CaVO caVO) {
        // TODO 更新CA的过程中应该伴随着RS节点的下线和上线过程，或者是该RS节点先暂停给交易签名，CA更新完成后，再重新恢复签名

        RespData respData;
        //send and get callback result
        try {
            // TODO 需要重写构造coretx的方法，是CA更新类型的交易
            coreTransactionService.submitTx(BizTypeEnum.CA_UPDATE, constructUpdateCoreTx(caVO));
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                log.error("create bill error", e);
                return new RespData<>(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
            }
        }
        return new RespData<>();
    }

    /**
     * @return
     * @desc generate pubKey and PriKey ,then insert into db
     */
    @Override public void cancelKeyPair(String user) {

        //check nodeName
        if (!nodeState.getNodeName().equals(user)) {
            log.error("[cancelKeyPair] invalid node name");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_INVALID_NODE_NAME_EXIST_ERROR,
                "[cancelKeyPair] invalid node name");
        }

        // TODO 注销CA之前，应该检测CA是否存在，存在就可以注销，否则不可以

        //set pubKey and priKey to invalid
        Config config = new Config();
        config.setNodeName(nodeState.getNodeName());
        config.setValid("FALSE");
        configRepository.updateConfig(config);

        //construct caVO
        CaVO caVO = new CaVO();
        String pubKey = configRepository.getConfig(user).getPubKey();
        caVO.setReqNo(HashUtil.getSHA256S(pubKey));
        caVO.setUser(nodeState.getNodeName());

        // send CA auth request
        caClient.caCancel(nodeState.getNodeName(), caVO);
    }

    /**
     * @return
     * @desc construct ca tx and send to slave
     */
    @Override public RespData cancelCaTx(CaVO caVO) {
        RespData respData;

        //send and get callback result
        try {
            coreTransactionService.submitTx(BizTypeEnum.CA_CANCEL, constructCancelCoreTx(caVO));
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                log.error("create bill error", e);
                return new RespData<>(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
            }
        }
        return new RespData<>();
    }

    /**
     * @return
     * @desc after ca tx has bean authoritied by the current cluster, then update table config column valid to TRUE
     */
    @Override public void callbackCa() {

    }

    public CoreTransaction constructAuthCoreTx(CaVO caVO) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(caVO.getReqNo());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setPolicyId(BizTypeEnum.CA_AUTH.getCode());
        coreTx.setActionList(buildAuthActionList(caVO));
        return coreTx;
    }

    private List<Action> buildAuthActionList(CaVO caVO) {
        List<Action> actions = new ArrayList<>();
        CaAction caAction = new CaAction();
        caAction.setPeriod(caVO.getPeriod());
        caAction.setPubKey(caVO.getPubKey());
        caAction.setUsage(caVO.getUsage());
        caAction.setUser(caVO.getUser());
        caAction.setType(ActionTypeEnum.CA_AUTH);
        caAction.setIndex(0);
        actions.add(caAction);
        return actions;
    }

    public CoreTransaction constructUpdateCoreTx(CaVO caVO) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(caVO.getReqNo());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setPolicyId(BizTypeEnum.CA_UPDATE.getCode());
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
        actions.add(caAction);
        return actions;
    }

    public CoreTransaction constructCancelCoreTx(CaVO caVO) {
        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setTxId(caVO.getReqNo());
        coreTx.setSender(nodeState.getNodeName());
        coreTx.setVersion(VersionEnum.V1.getCode());
        coreTx.setPolicyId(BizTypeEnum.CA_UPDATE.getCode());
        coreTx.setActionList(buildCancelActionList(caVO));
        return coreTx;
    }

    private List<Action> buildCancelActionList(CaVO caVO) {
        List<Action> actions = new ArrayList<>();
        CaAction caAction = new CaAction();
        caAction.setUser(caVO.getUser());
        caAction.setType(ActionTypeEnum.CA_CANCEL);
        caAction.setIndex(0);
        actions.add(caAction);
        return actions;
    }

    /**
     * @param pubKey
     * @return
     * @desc write file
     */
    private void fileWriter(String pubKey) {
        String path = "config" + System.getProperty("file.separator") + "keys" + System.getProperty("file.separator");
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(path + "publickey" + myId, false));
            w.write(pubKey);
            w.flush();
            w.close();
        } catch (IOException e) {
            log.error("[fileWriter]write pubKey to file error", e);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_WRITE_FILE_ERROR,
                "[fileWriter]write pubKey to file error");
        }
    }

}
