package com.higgs.trust.slave.core.service.action.utxo;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * UTXOActionHandler
 *
 * @author lingchao
 * @create 2018年06月11日21:46
 */
public class UTXOActionHandlerTest extends BaseTest {
    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    UTXOActionHandler utxoActionHandler;

    @Test
    private void TestProcess() {

        snapshotService.clear();
        snapshotService.destroy();
        snapshotService.startTransaction();
        utxoActionHandler.process(buildActionData());
        snapshotService.commit();

   //     snapshotService.startTransaction();
   //     utxoActionHandler.process(buildActionData1());
   //     snapshotService.commit();

        snapshotService.flush();
    }


    private ActionData buildDestroyActionData() {
        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setContractAddress("123456780");

        TxIn txIn = new TxIn();
        txIn.setIndex(0);
        txIn.setTxId("UTXOlingchao1528799659919");
        txIn.setActionIndex(0);

        List<TxIn> inputList = new ArrayList<>();
        inputList.add(txIn);


        TxOut txOut = new TxOut();
        JSONObject state = new JSONObject();
        state.put("amount", new BigDecimal("2000"));
        state.put("currency", "BUC");
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        txOut.setIdentity("12312312321");
        txOut.setState(state);

        TxOut txOut1 = new TxOut();
        JSONObject state1 = new JSONObject();
        state1.put("amount", new BigDecimal("500"));
        txOut1.setIndex(1);
        txOut1.setActionIndex(0);
        txOut1.setIdentity("12312312321");
        txOut1.setState(state1);


        List<TxOut> outList = new ArrayList<>();
        outList.add(txOut);
        outList.add(txOut1);
       // utxoAction.setOutputList(outList);
        utxoAction.setInputList(inputList);
        utxoAction.setIndex(0);
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.DESTRUCTION);
        utxoAction.setStateClass("12312");
        utxoAction.setType(ActionTypeEnum.UTXO);
        SignedTransaction signedTransaction = new SignedTransaction();

        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setPolicyId("TRANSFER_UTXO");
        coreTx.setTxId("UTXOlingchao"+System.currentTimeMillis());
        signedTransaction.setCoreTx(coreTx);
        ActionData ActionData1 = new ActionData() {
            @Override
            public Block getCurrentBlock() {
                return null;
            }

            @Override
            public Package getCurrentPackage() {
                return null;
            }

            @Override
            public SignedTransaction getCurrentTransaction() {
                return signedTransaction;
            }

            @Override
            public Action getCurrentAction() {
                return utxoAction;
            }
        };
        return ActionData1;
    }


    private ActionData buildActionData() {
        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setContractAddress("123456780");

        TxIn txIn = new TxIn();
        txIn.setIndex(1);
        txIn.setTxId("UTXOlingchaoyiyi凌");
        txIn.setActionIndex(0);

        List<TxIn> inputList = new ArrayList<>();
        inputList.add(txIn);


        TxOut txOut = new TxOut();
        JSONObject state = new JSONObject();
        state.put("amount", "1GAJDrKnqVhPweoN8Dggg7TxYjnAxnoLTVp1jWozYDUf1MdiqX9DLTNPDWieQJP6jNhd2XZyr45Z1f4gGNg8mPq5mtBEAVPgsrBv6mqysJJNmudcuSbsmMxAf7mf2FjFAM3SLcTrHWryM3rEMPJrAdmqHsyKaNDb7PGqqsZMiCztZxmfMmmWY9CwKEFVeq9WUBaypU1i1U3XzWqb2DQo8ty4ax6RP66d432hRhTZaJwotzRp2XEA9Hqc5misj1xjazdCVXHR542Ck2nJ7DmSjYnEHe7VKxuA78zHt6yZDGY4bVGFDH2x74KEgtH9DQKh6fVMyGPWFu2QikTedLq5xvuYAbqbm12SWU");
        state.put("currency", "BUC");
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        txOut.setIdentity("12312312321");
        txOut.setState(state);

        TxOut txOut1 = new TxOut();
        JSONObject state1 = new JSONObject();
        state1.put("amount", "253evJ5U5ebNgD4zUGWJPJtJUn2hu3jfqF3W2XTfo1kmqY1GvLgR27Z5xkbRMA19pTgzennvPvNKgUNUkv69BhMEdkjSif4PDeAWRbXjBDtHG92AgB3Yv6QwPCpSQJ9stTjC6WzRLMM5jWoq6D6WRvLdVkwuxEzr8QFboThKznLtGpLv4axC8UNWU8SePRyPU9f9FGWvhXWryHZQd415rvCFJtFGm4ReFTktnDCmDvQy8N6ZhyTqVNSWYd8oHTvZ6GDE66nNLAKhB8K4izUxZRDoGRRukk5V2mWRuUxPp1H6sZD8dX6iMgEPRdNx8Jay2CtShKyodfxhbJvcFdPFYrn2seCYpCpVF2a");
        state1.put("currency", "BUC");
        txOut1.setIndex(1);
        txOut1.setActionIndex(0);
        txOut1.setIdentity("12312312321");
        txOut1.setState(state1);


        List<TxOut> outList = new ArrayList<>();
        outList.add(txOut);
        outList.add(txOut1);
        utxoAction.setOutputList(outList);
        utxoAction.setInputList(inputList);
        utxoAction.setIndex(0);
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setStateClass("12312");
        utxoAction.setType(ActionTypeEnum.UTXO);
        SignedTransaction signedTransaction = new SignedTransaction();

        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setPolicyId("1");
        coreTx.setTxId("UTXOlingchaoyiyi凌超");
        signedTransaction.setCoreTx(coreTx);
        ActionData ActionData1 = new ActionData() {
            @Override
            public Block getCurrentBlock() {
                return null;
            }

            @Override
            public Package getCurrentPackage() {
                return null;
            }

            @Override
            public SignedTransaction getCurrentTransaction() {
                return signedTransaction;
            }

            @Override
            public Action getCurrentAction() {
                return utxoAction;
            }
        };
        return ActionData1;
    }


    private ActionData buildActionData1() {
        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setContractAddress("123456780");

        TxIn txIn = new TxIn();
        txIn.setIndex(0);
        txIn.setTxId("UTXOlingchaoyiyi凌超");
        txIn.setActionIndex(0);

        List<TxIn> inputList = new ArrayList<>();
        inputList.add(txIn);


        TxOut txOut = new TxOut();
        JSONObject state = new JSONObject();
        state.put("amount", new BigDecimal("1"));
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        txOut.setIdentity("12312312321");
        txOut.setState(state);

        TxOut txOut1 = new TxOut();
        JSONObject state1 = new JSONObject();
        state1.put("amount", new BigDecimal("99"));
        txOut1.setIndex(1);
        txOut1.setActionIndex(0);
        txOut1.setIdentity("12312312321");
        txOut1.setState(state1);


        List<TxOut> outList = new ArrayList<>();
        outList.add(txOut);
        outList.add(txOut1);
        utxoAction.setOutputList(outList);
        utxoAction.setInputList(inputList);
        utxoAction.setIndex(0);
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setStateClass("12312");
        utxoAction.setType(ActionTypeEnum.UTXO);
        SignedTransaction signedTransaction = new SignedTransaction();

        CoreTransaction coreTx = new CoreTransaction();
        coreTx.setPolicyId("1");
        coreTx.setTxId("UTXO" + System.currentTimeMillis());
        signedTransaction.setCoreTx(coreTx);
        ActionData ActionData1 = new ActionData() {
            @Override
            public Block getCurrentBlock() {
                return null;
            }

            @Override
            public Package getCurrentPackage() {
                return null;
            }

            @Override
            public SignedTransaction getCurrentTransaction() {
                return signedTransaction;
            }

            @Override
            public Action getCurrentAction() {
                return utxoAction;
            }
        };
        return ActionData1;
    }
}
