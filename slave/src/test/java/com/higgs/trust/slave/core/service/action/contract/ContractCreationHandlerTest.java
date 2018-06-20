package com.higgs.trust.slave.core.service.action.contract;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.pack.PackageServiceImpl;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.core.service.snapshot.agent.ContractSnapshotAgent;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.contract.ContractCreationAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ContractCreationHandlerTest extends BaseTest {

    @Autowired SnapshotService snapshot;
    @Autowired ContractCreationHandler creationHandler;
    @Autowired PackageServiceImpl packageService;
    @Autowired ContractSnapshotAgent agent;
    @Autowired PlatformTransactionManager platformTransactionManager;


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
    public void testProcess() {
        Action action = createContractCreationAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.CONTRACT_ISSUE)
                .addAction(action)
                .setTxId("Ox00000001" + System.currentTimeMillis())
                .signature("", ActionDataMockBuilder.privateKey1)
                .signature("", ActionDataMockBuilder.privateKey2)
                .makeBlockHeader()
                .setBlockHeight(2)
                .build();

        try {
            TransactionTemplate tx = new TransactionTemplate(platformTransactionManager);
            tx.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_NESTED);
            tx.execute(new TransactionCallbackWithoutResult(){
                @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    try {
                        packageService.process(packContext, false,false);
                    } catch (Exception ex) {
                        transactionStatus.setRollbackOnly();
                    }
                }
            });
            Assert.assertTrue(true);
        } catch (Exception ex) {
           ex.printStackTrace();
        }
    }

    @Test
    public void testProcess2() {
        Action action = createContractCreationAction();
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.CONTRACT_ISSUE)
                .addAction(action)
                .setTxId("Ox00000001" + System.currentTimeMillis())
                .signature("", ActionDataMockBuilder.privateKey1)
                .signature("", ActionDataMockBuilder.privateKey2)
                .makeBlockHeader()
                .setBlockHeight(2)
                .build();

        packageService.process(packContext, false,false);
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
                .signature("", ActionDataMockBuilder.privateKey1)
                .makeBlockHeader()
                .build();
        System.out.println(JSON.toJSONString(packContext.getCurrentTransaction()));
    }
}