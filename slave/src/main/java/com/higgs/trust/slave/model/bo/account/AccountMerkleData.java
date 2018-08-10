package com.higgs.trust.slave.model.bo.account;

import com.higgs.trust.slave.common.util.AmountUtil;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author liuyu
 * @description
 * @date 2018-04-16
 */
@Getter @Setter public class AccountMerkleData extends BaseBO implements MerkleTreeSnapshotAgent.MerkleDataNode {
    /**
     * number of account
     */
    private String accountNo;
    /**
     * currency of account
     */
    private String currency;
    /**
     * balance of account
     */
    private BigDecimal balance;
    /**
     * freeze amount of account
     */
    private BigDecimal freezeAmount;
    /**
     * fund direction-DEBIT,CREDIT
     */
    private String fundDirection;
    /**
     * status,NORMAL,DESTROY
     */
    private String status;
    /**
     * override get method,ensure the same amount by formatter
     *
     * @return
     */
    public BigDecimal getBalance() {
        String amount = AmountUtil.formatAmount(balance);
        balance = AmountUtil.convert(amount);
        return balance;
    }

    /**
     * override get method,ensure the same amount by formatter
     *
     * @return
     */
    public BigDecimal getFreezeAmount() {
        String amount = AmountUtil.formatAmount(freezeAmount);
        freezeAmount = AmountUtil.convert(amount);
        return freezeAmount;
    }

    @Override public String getUniqKey() {
        return accountNo;
    }
}
