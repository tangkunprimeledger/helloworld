package com.higgs.trust.slave.core.service.action.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.model.bo.contract.Contract;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.contract.ContractCreationAction;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.support.Assert;

public class ContractCreationHandlerTest extends IntegrateBaseTest {

    @Autowired SnapshotService snapshot;
    @Autowired private ContractCreationHandler creationHandler;
    @Autowired private ContractSnapshotAgent agent;


    private ContractCreationAction createContractCreationAction() {
        ContractCreationAction action = new ContractCreationAction();
        action.setCode("function main() { db.put('name', 'higgs trust'); print('>>>>>>>> hello world <<<<<<<<<'); }");
        action.setLanguage("javascript");
        action.setVersion("1");
        action.setIndex(0);
        action.setType(ActionTypeEnum.REGISTER_CONTRACT);
        return action;
    }

    @Test
    public void testValidate() throws Exception {
        Action action = createContractCreationAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.CONTRACT_ISSUE)
                .addAction(action)
                .setTxId("00000000000")
                .signature(ActionDataMockBuilder.privateKey1)
                .signature(ActionDataMockBuilder.privateKey2)
                .makeBlockHeader()
                .build();


        snapshot.startTransaction();
        creationHandler.validate(packContext);
        Contract contract = agent.get("e6f21e41de78458a509abde3ead213502e365adfc7c3c217d428878fc1ff37a6");
        snapshot.commit();
        Assert.isTrue(contract != null);
    }

    @Test
    public void testPersist() throws Exception {
        Action action = createContractCreationAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.CONTRACT_ISSUE)
                .addAction(action)
                .setTxId("tx_00000000001")
                .signature(ActionDataMockBuilder.privateKey1)
                .signature(ActionDataMockBuilder.privateKey2)
                .makeBlockHeader()
                .build();

        System.out.println(JSON.toJSONString(packContext.getCurrentTransaction()));
        creationHandler.persist(packContext);
        try {
            creationHandler.persist(packContext);
            Assert.isTrue(false);
        } catch (Exception ex) {
        }
    }

    public static void main(String[] args) {
        //JSON auto detect class type
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

        Action action = new ContractCreationHandlerTest().createContractCreationAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.CONTRACT_ISSUE)
                .addAction(action)
                .setTxId("tx_00000000001")
                .signature(ActionDataMockBuilder.privateKey1)
                .makeBlockHeader()
                .build();
        System.out.println(JSON.toJSONString(packContext.getCurrentTransaction()));
    }
}