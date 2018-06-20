package com.higgs.trust.slave.core.service.action.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.contract.JsonHelper;
import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.pack.PackageServiceImpl;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeAction;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContractInvokeHandlerTest extends IntegrateBaseTest {

    @Autowired private ContractInvokeHandler invokeHandler;
    @Autowired SnapshotService snapshot;
    @Autowired PackageServiceImpl packageService;

    private ContractInvokeAction createContractInvokeAction() {
        ContractInvokeAction action = new ContractInvokeAction();
        action.setAddress("a7d0c2779d627cfc7e931c35060d1dcb6d5c63c13862323bedf2a4f3c352f956");
        //action.setMethod("main");
        action.setIndex(0);
        action.setType(ActionTypeEnum.TRIGGER_CONTRACT);
        return action;
    }

    @Test
    public void testProcess() {
        Action action = createContractInvokeAction();
        Action action2 = JsonHelper.clone(action, ContractInvokeAction.class);
        action2.setIndex(1);

        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.CONTRACT_ISSUE)
                .addAction(action)
                .addAction(action2)
                .setTxId(String.format("tx_id_invoke_contract_%s", System.currentTimeMillis()))
                .signature("", ActionDataMockBuilder.privateKey1)
                .signature("", ActionDataMockBuilder.privateKey2)
                .makeBlockHeader()
                .setBlockHeight(9)
                .build();

        packageService.process(packContext, true,false);

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
                .createSignedTransaction(InitPolicyEnum.REGISTER_POLICY)
                .addAction(action)
                .setBizModel(bizModel)
                .setTxId(String.format("tx_id_invoke_contract_%s", System.currentTimeMillis()))
//                .signature(ActionDataMockBuilder.privateKey1)
//                .signature(ActionDataMockBuilder.privateKey1)
//                .signature(ActionDataMockBuilder.privateKey1)
//                .signature(ActionDataMockBuilder.privateKey1)

                .signature("", ActionDataMockBuilder.privateKey1)
                .signature("", ActionDataMockBuilder.privateKey2)
                .makeBlockHeader()
                .build();

        System.out.println(JSON.toJSONString(packContext.getCurrentTransaction()));
    }
}