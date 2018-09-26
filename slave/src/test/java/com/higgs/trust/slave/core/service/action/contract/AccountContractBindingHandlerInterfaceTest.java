package com.higgs.trust.slave.core.service.action.contract;

import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.contract.AccountContractBindingAction;
import com.higgs.trust.slave.model.bo.contract.ContractCreationAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.testng.annotations.Test;
import reactor.core.support.Assert;

import java.util.Map;

/**
 * @author duhongming
 * @date 2018/5/8
 */
public class AccountContractBindingHandlerInterfaceTest extends ContractBaseTest {

    @Autowired private SnapshotService snapshot;
    @Autowired private AccountContractBindingHandler bindingHandler;

    private AccountContractBindingAction createAccountContractBindingAction(Map<?, ?> param) {
        AccountContractBindingAction action = getBody(param, AccountContractBindingAction.class);
        if (action != null) {
            action.setIndex(0);
            action.setType(ActionTypeEnum.BIND_CONTRACT);
        }
        return action;
    }

    private PackContext createPackContext(Map<?, ?> param) {
        Action action = createAccountContractBindingAction(param);
        return createPackContext(action);
    }

    private PackContext createPackContext(Action action) {
        PackContext packContext = ActionDataMockBuilder.getBuilder()
                .createSignedTransaction(InitPolicyEnum.CONTRACT_ISSUE)
                .addAction(action)
                .setTxId("00000000000" + System.currentTimeMillis())
                .signature("", ActionDataMockBuilder.privateKey1)
                .signature("", ActionDataMockBuilder.privateKey2)
                .makeBlockHeader()
                .build();
        return packContext;
    }

    //@BeforeClass
    public void insertAccountAndContract() {
        System.out.println("begin insertAccountAndContract");
        String sql = "DELETE FROM account_info WHERE account_no='40adef8565fe4e7a33a4d39cfa3797b356bab5491bd580bed50504127004c5dc';\n" +
                "INSERT INTO account_info (account_no, currency, balance, freeze_amount, fund_direction, detail_no, detail_freeze_no, status, create_time, update_time)\n" +
                "VALUES ('40adef8565fe4e7a33a4d39cfa3797b356bab5491bd580bed50504127004c5dc', 'xx', 0, 0, 'DEBIT', 0, 0, 'NORMAL', now(3), now(3));\n" +
                "\n" +
                "DELETE FROM contract WHERE address='4835ce31f929a234b2c7bd4aeb195b9134a6f81abc95e6a6f41d6f656d1930da';\n" +
                "INSERT INTO contract (address, `language`, code, create_time)\n" +
                "VALUES ('4835ce31f929a234b2c7bd4aeb195b9134a6f81abc95e6a6f41d6f656d1930da', 'javascript', 'function main(){print(\"trsut\"); db.put(\"name\",\"trust\");}', now(3));";
        executeDelete(sql);
        System.out.println("end insertAccountAndContract");
    }

    @Override
    public String getProviderRootPath() {
        return "java/com/higgs/trust/slave/core/service/contract/binding/";
    }

    @Test(dataProvider = "defaultProvider", priority = 0)
    public void testValidate(Map<?, ?> param) {
        insertAccountAndContract();
        PackContext packContext = null;
        try {
            packContext = createPackContext(param);
        } catch (Exception ex) {
            String asserts = String.valueOf(param.get("assert"));
            if (StringUtils.isEmpty(asserts)) {
                throw ex;
            }
            ex.printStackTrace();
            Assert.isTrue(ex.getMessage().equals(asserts));
            return;
        }
        snapshot.startTransaction();
        doTestValidate(param, packContext, bindingHandler);
        snapshot.commit();
    }

    @Test(dataProvider = "defaultProvider", priority = 1)
    public void testPersist(Map<?, ?> param) {
        insertAccountAndContract();
        PackContext packContext = null;
        try {
            packContext = createPackContext(param);
        } catch (Exception ex) {
            String asserts = String.valueOf(param.get("assert"));
            if (StringUtils.isEmpty(asserts)) {
                throw ex;
            }
            ex.printStackTrace();
            Assert.isTrue(ex.getMessage().equals(asserts));
            return;
        }
        doTestPersist(param, packContext, bindingHandler);
    }

    @Test
    public void testValidate_ActionType_Not_Expect() {
        insertAccountAndContract();
        try {
            ContractCreationAction action = new ContractCreationAction();
            action.setIndex(0);
            action.setType(ActionTypeEnum.REGISTER_CONTRACT);
            PackContext packContext = createPackContext(action);
            bindingHandler.process(packContext);
            Assert.isTrue(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.isInstanceOf(IllegalArgumentException.class, ex, "throws IllegalArgumentException");
        }
    }

    @Test
    public void testPersist_Action_IsNot_ContractCreationAction() {
        insertAccountAndContract();
        try {
            ContractCreationAction action = new ContractCreationAction();
            action.setIndex(0);
            action.setType(ActionTypeEnum.REGISTER_CONTRACT);
            PackContext packContext = createPackContext(action);
            bindingHandler.process(packContext);
            Assert.isTrue(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.isInstanceOf(IllegalArgumentException.class, ex, "throws IllegalArgumentException");
        }
    }
}
