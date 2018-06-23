package com.higgs.trust.rs.custom.util.converter;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.rs.custom.api.enums.BillStatusEnum;
import com.higgs.trust.rs.custom.dao.po.ReceivableBillPO;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxOut;

import java.util.Date;

/**
 * bill convertor
 *
 * @author lingchao
 * @create 2018年05月13日23:30
 */
public class BillConvertor {
    /**
     * build bill for create
     *
     * @param billCreateVO
     * @param actionIndex
     * @param index
     * @return
     */
    public static ReceivableBillPO buildBill(BillCreateVO billCreateVO, Long actionIndex, Long index,
        String contractAddress) {
        ReceivableBillPO receivableBillPO = new ReceivableBillPO();
        receivableBillPO.setBillId(billCreateVO.getBillId());
        receivableBillPO.setHolder(billCreateVO.getHolder());
        receivableBillPO.setStatus(BillStatusEnum.PROCESS.getCode());
        receivableBillPO.setTxId(billCreateVO.getRequestId());
        receivableBillPO.setActionIndex(actionIndex);
        receivableBillPO.setIndex(index);
        receivableBillPO.setContractAddress(contractAddress);

        //build state
        JSONObject state = new JSONObject();
        state.put("billId", billCreateVO.getBillId());
        state.put("amount", billCreateVO.getAmount());
        state.put("dueDate", billCreateVO.getDueDate());
        state.put("finalPayerId", billCreateVO.getFinalPayerId());

        receivableBillPO.setState(state.toJSONString());

        return receivableBillPO;
    }

    /**
     * build bill for transfer
     *
     * @param txId
     * @param utxoAction
     * @param txOut
     * @return
     */
    public static ReceivableBillPO buildBill(String txId, UTXOAction utxoAction, TxOut txOut) {
        ReceivableBillPO receivableBillPO = new ReceivableBillPO();
        receivableBillPO.setContractAddress(utxoAction.getContractAddress());
        receivableBillPO.setState(txOut.getState().toJSONString());
        Date time = new Date();
        receivableBillPO.setCreateTime(time);
        receivableBillPO.setUpdateTime(time);
        receivableBillPO.setBillId(txOut.getState().getString("billId"));
        receivableBillPO.setHolder(txOut.getState().getString("holder"));
        receivableBillPO.setStatus(BillStatusEnum.PROCESS.getCode());
        receivableBillPO.setTxId(txId);
        receivableBillPO.setActionIndex(txOut.getActionIndex().longValue());
        receivableBillPO.setIndex(txOut.getIndex().longValue());
        return receivableBillPO;
    }
}
