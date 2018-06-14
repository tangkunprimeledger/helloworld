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

        snapshotService.startTransaction();
        utxoActionHandler.process(buildActionData1());
        snapshotService.commit();

        snapshotService.flush();
    }


    private ActionData buildDestroyActionData() {
        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setContractAddress("1234567");

        TxIn txIn = new TxIn();
        txIn.setIndex(0);
        txIn.setTxId("UTXOlingchao1528799659919");
        txIn.setActionIndex(0);

        List<TxIn> inputList = new ArrayList<>();
        inputList.add(txIn);


        TxOut txOut = new TxOut();
        JSONObject state = new JSONObject();
        state.put("amount", new BigDecimal("2000"));
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
        coreTx.setPolicyId("000004");
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
        utxoAction.setContractAddress("1234567");

        TxIn txIn = new TxIn();
        txIn.setIndex(1);
        txIn.setTxId("UTXOlingchaoyiyi凌");
        txIn.setActionIndex(0);

        List<TxIn> inputList = new ArrayList<>();
        inputList.add(txIn);


        TxOut txOut = new TxOut();
        JSONObject state = new JSONObject();
        state.put("amount", new BigDecimal("100"));
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        txOut.setIdentity("12312312321");
        txOut.setState(state);

        TxOut txOut1 = new TxOut();
        JSONObject state1 = new JSONObject();
        state1.put("amount", new BigDecimal("400"));
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
        utxoAction.setContractAddress("1234567");

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
