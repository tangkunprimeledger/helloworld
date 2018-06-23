package com.higgs.trust.rs.custom.util.converter;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * coreTransaction convertor
 *
 * @author lingchao
 * @create 2018年05月13日17:49
 */
@Service
public class CoreTransactionConvertor {
    @Autowired
    private NodeState nodeState;

    /**
     * 构建coreTransaction
     * @param txId
     * @param bizModel
     * @param actionList
     * @return
     */
    public CoreTransaction buildBillCoreTransaction(String txId, JSONObject bizModel, List<Action> actionList,String policyId){
        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setTxId(txId);
        coreTransaction.setBizModel(bizModel);
        coreTransaction.setActionList(actionList);
        coreTransaction.setVersion(VersionEnum.V1.getCode());
        coreTransaction.setSender(nodeState.getNodeName());
        coreTransaction.setSendTime(new Date());
        coreTransaction.setPolicyId(policyId);
        return coreTransaction;
    }

    /**
     * build core transaction
     * @param txId
     * @param actionList
     * @return
     */
    public CoreTransaction buildCoreTransaction(String txId, JSONObject bizModel, List<Action> actionList, String policyId) {
        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setTxId(txId);
        coreTransaction.setBizModel(bizModel);
        coreTransaction.setActionList(actionList);
        coreTransaction.setVersion(VersionEnum.V1.getCode());
        coreTransaction.setSender(nodeState.getNodeName());
        coreTransaction.setSendTime(new Date());
        coreTransaction.setPolicyId(policyId);
        return coreTransaction;
    }
}
