package com.higgs.trust.rs.custom.util.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.rs.custom.api.enums.BillStatusEnum;
import com.higgs.trust.rs.custom.dao.ReceivableBillDao;
import com.higgs.trust.rs.custom.dao.po.ReceivableBillPO;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import com.higgs.trust.rs.custom.vo.TransferDetailVO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * UTXO actionConvertor
 *
 * @author lingchao
 * @create 2018年05月13日18:11
 */
@Slf4j
@Service
public class UTXOActionConvertor {
    private static final String CHAIN_OWNER = "BANK_CHAIN";
    @Autowired
    private NodeState nodeState;

    @Autowired
    private ReceivableBillDao receivableBillDao;

    @Autowired
    private RsConfig rsConfig;

    @Autowired
    private RsBlockChainService rsBlockChainService;


    /**
     * build  create bill and identity actionList
     *
     * @param billCreateVO
     * @return
     */
    public List<Action> buildCreateBillWithIdentityActionList(BillCreateVO billCreateVO) {
        List<Action> actionList = new ArrayList<>();

        //build dataIdentityAction
        DataIdentityAction dataIdentityAction = new DataIdentityAction();
        dataIdentityAction.setDataOwner(nodeState.getNodeName());
        dataIdentityAction.setChainOwner(CHAIN_OWNER);
        dataIdentityAction.setIdentity(billCreateVO.getHolder());
        dataIdentityAction.setIndex(0);
        dataIdentityAction.setType(ActionTypeEnum.CREATE_DATA_IDENTITY);
        actionList.add(dataIdentityAction);

        //build state
        JSONObject state = new JSONObject();
        state.put("billId", billCreateVO.getBillId());
        state.put("amount", billCreateVO.getAmount());
        state.put("dueDate", billCreateVO.getDueDate());
        state.put("finalPayerId", billCreateVO.getFinalPayerId());

        //build UTXO
        TxOut txOut = new TxOut();
        txOut.setIdentity(billCreateVO.getHolder());
        txOut.setActionIndex(1);
        txOut.setIndex(0);
        txOut.setState(state);

        List<TxOut> txOutList = new ArrayList<>();
        txOutList.add(txOut);

        //build UTXOAction
        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setOutputList(txOutList);
        utxoAction.setContractAddress(rsConfig.getContractAddress());
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setStateClass("com.alibaba.fastjson.JSONObject");
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.ISSUE);
        utxoAction.setIndex(1);

        actionList.add(utxoAction);

        return actionList;
    }

    /**
     * build CreateBill  ActionList
     *
     * @param billCreateVO
     * @return
     */
    public List<Action> buildCreateBillActionList(BillCreateVO billCreateVO) {
        List<Action> actionList = new ArrayList<>();

        //build state
        JSONObject state = new JSONObject();
        state.put("billId", billCreateVO.getBillId());
        state.put("amount", billCreateVO.getAmount());
        state.put("dueDate", billCreateVO.getDueDate());
        state.put("finalPayerId", billCreateVO.getFinalPayerId());

        //build UTXO
        TxOut txOut = new TxOut();
        txOut.setIdentity(billCreateVO.getHolder());
        txOut.setActionIndex(0);
        txOut.setIndex(0);
        txOut.setState(state);

        List<TxOut> txOutList = new ArrayList<>();
        txOutList.add(txOut);

        //build UTXOAction
        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setOutputList(txOutList);
        utxoAction.setContractAddress(rsConfig.getContractAddress());
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setStateClass("com.alibaba.fastjson.JSONObject");
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.ISSUE);
        utxoAction.setIndex(0);
        actionList.add(utxoAction);

        return actionList;
    }


    /**
     * build  transfer bill and identity actionList
     *
     * @param billTransferVO
     * @return
     */
    public List<Action> buildTransferBillWithIdentityActionList(BillTransferVO billTransferVO) {

        ReceivableBillPO receivableBillParam = new ReceivableBillPO();
        receivableBillParam.setBillId(billTransferVO.getBillId());
        receivableBillParam.setHolder(billTransferVO.getHolder());
        receivableBillParam.setStatus(BillStatusEnum.UNSPENT.getCode());
        List<ReceivableBillPO> receivableBillPOList = receivableBillDao.queryByList(receivableBillParam);

        if (CollectionUtils.isEmpty(receivableBillPOList)) {
            log.error("build Transfer Bill   error, transfer bill is not exist exception");
            throw new RuntimeException("build Transfer Bill   error, transfer bill is not exist exception");
        }
        ReceivableBillPO receivableBillPO = receivableBillPOList.get(0);

        List<Action> actionList = new ArrayList<>();
        int actionIndex = 0;

        //build dataIdentityAction
        for (TransferDetailVO transferDetailVO : billTransferVO.getTransferList()) {
            boolean isIdentityExist = rsBlockChainService.isExistedIdentity(transferDetailVO.getNextHolder());
            if (!isIdentityExist) {
                DataIdentityAction dataIdentityAction = new DataIdentityAction();
                dataIdentityAction.setDataOwner(nodeState.getNodeName());
                dataIdentityAction.setChainOwner(CHAIN_OWNER);
                dataIdentityAction.setIdentity(transferDetailVO.getNextHolder());
                dataIdentityAction.setIndex(actionIndex++);
                dataIdentityAction.setType(ActionTypeEnum.CREATE_DATA_IDENTITY);
                actionList.add(dataIdentityAction);
            }
        }

        //build TxIn
        TxIn txIn = new TxIn();
        txIn.setTxId(receivableBillPO.getTxId());
        txIn.setActionIndex(receivableBillPO.getActionIndex().intValue());
        txIn.setIndex(receivableBillPO.getIndex().intValue());
        List<TxIn> inputList = new ArrayList<>();
        inputList.add(txIn);

        List<TxOut> txOutList = new ArrayList<>();
        int index = 0;
        //build UTXO
        for (TransferDetailVO transferDetailVO : billTransferVO.getTransferList()) {
            TxOut txOut = new TxOut();
            txOut.setIdentity(transferDetailVO.getNextHolder());
            txOut.setActionIndex(actionIndex);
            txOut.setIndex(index++);

            JSONObject state = JSON.parseObject(receivableBillPO.getState());
            state.put("amount", transferDetailVO.getAmount());
            state.put("billId", transferDetailVO.getNextBillId());

            txOut.setState(state);
            txOutList.add(txOut);
        }

        //build UTXOAction
        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(txOutList);
        utxoAction.setContractAddress(receivableBillPO.getContractAddress());
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setStateClass("com.alibaba.fastjson.JSONObject");
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setIndex(actionIndex);
        actionList.add(utxoAction);

        return actionList;
    }
}
