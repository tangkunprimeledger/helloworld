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
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.CancelRS;
import org.rocksdb.Transaction;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RequestRepositoryTest extends IntegrateBaseTest {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestRocksDao requestRocksDao;

    @Test
    public void test() {
        String requestId = System.currentTimeMillis() + "";
        System.out.println("add:" + requestRepository.insertRequest(requestId, RequestEnum.PROCESS, "000001", "msg"));
        System.out.println("query: " + requestRepository.queryByRequestId(requestId));
        System.out.println("requestIdempotent:" + requestRepository.requestIdempotent(requestId));
        System.out.println("query: " + requestRepository.queryByRequestId(requestId + "-"));
        requestRepository.updateCode(requestId, "000002", "");
        System.out.println("query: " + requestRepository.queryByRequestId(requestId));
        requestRepository.updateCode(requestId, "000003", "ooo");
        System.out.println("query: " + requestRepository.queryByRequestId(requestId));
        requestRepository.updateStatusAndCode(requestId, RequestEnum.PROCESS, RequestEnum.DONE,"000004", "ok");
        System.out.println("query: " + requestRepository.queryByRequestId(requestId));
    }

    @Test
    public void requestIdempotent(){
        System.out.println("requestIdempotent:" + requestRepository.requestIdempotent("12321"));
    }
}