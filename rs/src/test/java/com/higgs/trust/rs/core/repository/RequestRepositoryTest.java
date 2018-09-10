package com.higgs.trust.rs.core.repository;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.common.enums.RequestEnum;
import com.higgs.trust.rs.common.enums.RespCodeEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.po.RequestPO;
import com.higgs.trust.rs.core.dao.rocks.RequestRocksDao;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.CancelRS;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.*;

public class RequestRepositoryTest extends IntegrateBaseTest{

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestRocksDao requestRocksDao;

    @Test public void testRequestIdempotent() throws Exception {
        System.out.println(requestRepository.requestIdempotent("test-request-id-1"));
    }

    @Test public void testInsertRequest() throws Exception {
//        RequestPO req = new RequestPO();
//        req.setRequestId("test-request-id-1");
//        req.setRespCode("000000");
//        req.setRespMsg("resp msg");
//        req.setData("test-data");
//        req.setStatus(RequestEnum.PROCESS.getCode());
//
//        requestRepository.insertRequest("test-request-id-1", req);

        for (int i = 2; i < 100; i++) {
            RequestPO po = new RequestPO();
            po.setRequestId("test-tx-id-" + i);
            po.setRespCode("000000");
            po.setRespMsg("resp msg");
            po.setData("test-data");
            po.setStatus(RequestEnum.PROCESS.getCode());

            requestRepository.insertRequest("test-tx-id-" + i, po);
        }
    }

    @Test public void testUpdateRequest() throws Exception {
        ThreadLocalUtils.putWriteBatch(new WriteBatch());
        requestRepository.updateRequest("test-request-id-1", RequestEnum.PROCESS, RequestEnum.DONE, RespCodeEnum.SYS_FAIL.getRespCode(), "服务异常");
        RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
        ThreadLocalUtils.clearWriteBatch();
    }

    @Test public void testBatchUpdateStatus() throws Exception {
        List<RsCoreTxVO> rsCoreTxVOS = new ArrayList<>(100);
        for (int i = 20; i < 50; i += 3) {
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
        requestRepository.batchUpdateStatus(rsCoreTxVOS, RequestEnum.PROCESS, RequestEnum.DONE);
        RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
        ThreadLocalUtils.clearWriteBatch();

        List<RequestPO> list = requestRocksDao.queryByPrefix("test-tx-id");
        System.out.println(list);
    }
}