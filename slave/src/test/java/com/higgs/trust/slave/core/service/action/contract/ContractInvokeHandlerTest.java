package com.higgs.trust.slave.core.service.action.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractStateSnapshotAgent;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeAction;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.support.Assert;

public class ContractInvokeHandlerTest extends IntegrateBaseTest {

    @Autowired private ContractInvokeHandler invokeHandler;
    @Autowired SnapshotService snapshot;

    private ContractInvokeAction createContractInvokeAction() {
        ContractInvokeAction action = new ContractInvokeAction();
        action.setAddress("4835ce31f929a234b2c7bd4aeb195b9134a6f81abc95e6a6f41d6f656d1930da");
        //action.setMethod("main");
        action.setIndex(0);
        action.setType(ActionTypeEnum.TRIGGER_CONTRACT);
        return action;
    }

    @Test
    public void testValidate() throws Exception {
        ContractInvokeAction action = createContractInvokeAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.REGISTER)
                .addAction(action)
                .setTxId(String.format("tx_id_invoke_contract_%s", System.currentTimeMillis()))
                .signature(ActionDataMockBuilder.privateKey1)
                .signature(ActionDataMockBuilder.privateKey2)
//                .signature(ActionDataMockBuilder.privateKey1)
//                .signature(ActionDataMockBuilder.privateKey1)
                .makeBlockHeader()
                .build();

        System.out.println(JSON.toJSONString(packContext.getCurrentTransaction()));
        snapshot.startTransaction();
        invokeHandler.validate(packContext);
        Assert.isTrue(snapshot.get(SnapshotBizKeyEnum.CONTRACT_SATE,
                new ContractStateSnapshotAgent.ContractStateCacheKey(action.getAddress())) != null, "");
        snapshot.commit();
    }

    @Test
    public void testPersist() throws Exception {
        Action action = createContractInvokeAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.REGISTER)
                .addAction(action)
                .setTxId(String.format("tx_id_invoke_contract_%s", System.currentTimeMillis()))
                .signature(ActionDataMockBuilder.privateKey1)
                .signature(ActionDataMockBuilder.privateKey2)
                .makeBlockHeader()
                .build();
        invokeHandler.persist(packContext);
    }

    public static void main(String[] args) {

        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //JSON不做循环引用检测
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        //JSON输出NULL属性
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        //toJSONString的时候对一级key进行按照字母排序
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.SortField.getMask();
        //toJSONString的时候对嵌套结果进行按照字母排序
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.MapSortField.getMask();
        //toJSONString的时候记录Class的name
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteClassName.getMask();

        Action action = new ContractInvokeHandlerTest().createContractInvokeAction();
        JSONObject bizModel = new JSONObject();
        bizModel.put("data", action);
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.REGISTER)
                .addAction(action)
                .setBizModel(bizModel)
                .setTxId(String.format("tx_id_invoke_contract_%s", System.currentTimeMillis()))
//                .signature(ActionDataMockBuilder.privateKey1)
//                .signature(ActionDataMockBuilder.privateKey1)
//                .signature(ActionDataMockBuilder.privateKey1)
//                .signature(ActionDataMockBuilder.privateKey1)

                .signature(ActionDataMockBuilder.privateKey1)
                .signature(ActionDataMockBuilder.privateKey2)
                .makeBlockHeader()
                .build();

        System.out.println(JSON.toJSONString(packContext.getCurrentTransaction()));
    }
}