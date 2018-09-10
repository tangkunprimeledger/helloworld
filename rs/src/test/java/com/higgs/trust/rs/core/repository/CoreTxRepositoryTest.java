package com.higgs.trust.rs.core.repository;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.dao.rocks.CoreTxRocksDao;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.DecisionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.CancelRS;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class CoreTxRepositoryTest extends IntegrateBaseTest {

    @Autowired
    private CoreTxRepository coreTxRepository;

    @Autowired
    private CoreTxRocksDao coreTxRocksDao;

    @Test public void testAdd() throws Exception {
        CoreTransaction coreTx = new CoreTransaction();

        Random r = new Random();

        List<Action> actionList = new ArrayList<>();
        RegisterPolicy action = new RegisterPolicy();
        action.setIndex(0);
        action.setType(ActionTypeEnum.REGISTER_POLICY);
        action.setPolicyName("OPEN_ACCOUNT");
        action.setPolicyId("OPEN_ACCOUNT");
        action.setDecisionType(DecisionTypeEnum.FULL_VOTE);
        action.setContractAddr(null);

        List<String> rsIds = new ArrayList<>();
        rsIds.add("TRUST-TEST0");
        action.setRsIds(rsIds);
        actionList.add(action);

        coreTx.setTxId("test-tx-id-" + r.nextLong());
        coreTx.setPolicyId(InitPolicyEnum.REGISTER_POLICY.getPolicyId());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", actionList);
        coreTx.setBizModel(jsonObject);
        coreTx.setLockTime(new Date());
        coreTx.setSender("TRUST-TEST0");
        coreTx.setVersion("1.0.0");
        coreTx.setSendTime(new Date());
        coreTx.setActionList(actionList);

        List<SignInfo> signInfos = new ArrayList<>();
        SignInfo signInfo = new SignInfo();
        signInfo.setOwner("TRUST-TEST0");
        signInfo.setSign("test-signature");
        signInfos.add(signInfo);

        coreTxRepository.add(coreTx, signInfos, null);
    }

    @Test public void testAdd1() throws Exception {
    }

    @Test public void testQueryByTxId() throws Exception {
        List<String> keys = coreTxRocksDao.keys();
        CoreTransactionPO po = coreTxRepository.queryByTxId(keys.get(0), false);
    }

    @Test public void testQueryByTxIds() throws Exception {
        List<String> txIds = new ArrayList<>();
        txIds.add("test-tx-id-21");
        txIds.add("test-tx-id-23");
        txIds.add("test-tx-id-11");
        txIds.add("test-tx-id-35");
        txIds.add("test-tx-id-81");
        txIds.add("test-tx-id-67");
        txIds.add("test-tx-id-4");
        txIds.add("test-tx-id-78");
        txIds.add("test-tx-id-96");
        txIds.add("test-tx-id-69");

        List<CoreTransactionPO> pos = coreTxRepository.queryByTxIds(txIds);
        System.out.println(pos);
    }

    @Test public void testIsExist() throws Exception {
        Assert.assertEquals(coreTxRepository.isExist("test-tx-id-111"), true);
        Assert.assertEquals(coreTxRepository.isExist("test-tx-id-122"), false);
    }

    @Test public void testUpdateSignDatas() throws Exception {

        List<SignInfo> signInfos = new ArrayList<>();
        SignInfo signInfo = new SignInfo();
        signInfo.setOwner("TRUST-TEST223");
        signInfo.setSign("test update sign datas-tetst");
        signInfos.add(signInfo);

        ThreadLocalUtils.putWriteBatch(new WriteBatch());

        coreTxRepository.updateSignDatas("test-tx-id-113", signInfos);

        RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
        ThreadLocalUtils.clearWriteBatch();
        CoreTransactionPO po = coreTxRepository.queryByTxId("test-tx-id-113", false);
        System.out.println(po);
    }

    @Test public void testSaveExecuteResultAndHeight() throws Exception {
        ThreadLocalUtils.putWriteBatch(new WriteBatch());
        coreTxRepository.saveExecuteResultAndHeight("test-tx-id-113", CoreTxResultEnum.FAIL, "120009", "transaction is invalid", 20L);
        RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
        ThreadLocalUtils.clearWriteBatch();
        CoreTransactionPO po = coreTxRepository.queryByTxId("test-tx-id-113", false);
    }

    @Test public void testBatchInsert() throws Exception {
        List<RsCoreTxVO> rsCoreTxVOS = new ArrayList<>(100);
        for (int i = 100; i < 120; i++) {
            RsCoreTxVO rsCoreTxVO = new RsCoreTxVO();
            rsCoreTxVO.setTxId("test-tx-id-" + i);
            rsCoreTxVO.setStatus(CoreTxStatusEnum.PERSISTED);
            rsCoreTxVO.setErrorCode(null);
            rsCoreTxVO.setExecuteResult(CoreTxResultEnum.SUCCESS);
            rsCoreTxVO.setErrorMsg("");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("txId", "test-tx-id-" + i);
            rsCoreTxVO.setBizModel(jsonObject);
            rsCoreTxVO.setVersion(VersionEnum.V1);
            rsCoreTxVO.setLockTime(new Date());
            rsCoreTxVO.setSendTime(new Date());
            rsCoreTxVO.setSender("TRUST-TEST" + i);
            rsCoreTxVO.setPolicyId(InitPolicyEnum.CANCEL_RS.getPolicyId());
            List<Action> actions = new ArrayList<>();
            CancelRS cancelRS = new CancelRS();
            cancelRS.setRsId("TRUST-TEST0");
            cancelRS.setType(ActionTypeEnum.RS_CANCEL);
            cancelRS.setIndex(0);
            actions.add(cancelRS);
            rsCoreTxVO.setActionList(actions);

            List<SignInfo> signInfos = new ArrayList<>();
            SignInfo signInfo = new SignInfo();
            signInfo.setSign("test-signature" + i);
            signInfo.setOwner("TRUST-TEST" + i);
            signInfos.add(signInfo);
            rsCoreTxVO.setSignDatas(signInfos);
            rsCoreTxVOS.add(rsCoreTxVO);
        }

        ThreadLocalUtils.putWriteBatch(new WriteBatch());
        coreTxRepository.batchInsert(rsCoreTxVOS, 25L);
        RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
        ThreadLocalUtils.clearWriteBatch();
    }

    @Test public void testBatchUpdate() throws Exception {
        List<RsCoreTxVO> rsCoreTxVOS = new ArrayList<>(100);
        for (int i = 25; i < 41; i++) {
            RsCoreTxVO rsCoreTxVO = new RsCoreTxVO();
            rsCoreTxVO.setTxId("test-tx-id-" + i);
            rsCoreTxVO.setStatus(CoreTxStatusEnum.PERSISTED);
            rsCoreTxVO.setErrorCode(null);
            rsCoreTxVO.setExecuteResult(CoreTxResultEnum.SUCCESS);
            rsCoreTxVO.setErrorMsg("");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("txId", "test-tx-id-" + i);
            rsCoreTxVO.setBizModel(jsonObject);
            rsCoreTxVO.setVersion(VersionEnum.V1);
            rsCoreTxVO.setLockTime(new Date());
            rsCoreTxVO.setSendTime(new Date());
            rsCoreTxVO.setSender("TRUST-TEST" + i);
            rsCoreTxVO.setPolicyId(InitPolicyEnum.CANCEL_RS.getPolicyId());
            List<Action> actions = new ArrayList<>();
            CancelRS cancelRS = new CancelRS();
            cancelRS.setRsId("TRUST-TEST0");
            cancelRS.setType(ActionTypeEnum.RS_CANCEL);
            cancelRS.setIndex(0);
            actions.add(cancelRS);
            rsCoreTxVO.setActionList(actions);

            List<SignInfo> signInfos = new ArrayList<>();
            SignInfo signInfo = new SignInfo();
            signInfo.setSign("test-signature" + i);
            signInfo.setOwner("TRUST-TEST" + i);
            signInfos.add(signInfo);
            rsCoreTxVO.setSignDatas(signInfos);
            rsCoreTxVOS.add(rsCoreTxVO);
        }

        ThreadLocalUtils.putWriteBatch(new WriteBatch());
        coreTxRepository.batchUpdate(rsCoreTxVOS, 32L);
        RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
        ThreadLocalUtils.clearWriteBatch();
    }
}