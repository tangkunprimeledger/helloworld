package com.higgs.trust.slave.core.service.datahandler.account;

import com.higgs.trust.slave.api.enums.account.AccountStateEnum;
import com.higgs.trust.slave.api.enums.account.ChangeDirectionEnum;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.api.enums.account.TradeDirectionEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * @author liuyu
 * @description
 * @date 2018-04-16
 */
@Slf4j public class AccountOperationHelper {
    /**
     * validate trade info
     * <p>
     * 1.validate account status
     * 2.validate account usable balance by change direction
     * 3.return the after amount of trade
     *
     * @param accountInfo
     * @param tradeDirectionEnum
     * @param happenAmount
     * @return
     */
    public static BigDecimal validateTradeInfo(AccountInfo accountInfo, TradeDirectionEnum tradeDirectionEnum,
        BigDecimal happenAmount) {
        if (!StringUtils.equals(AccountStateEnum.NORMAL.getCode(), accountInfo.getStatus())) {
            log.error("[accountOperation.validateTradeInfo] account info status is not equals NORMAL by accountNo:{}",
                accountInfo.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_STATUS_IS_DESTROY_ERROR);
        }
        //the balance change direction
        ChangeDirectionEnum changeDirectionEnum =
            getBalanceChangeDirection(tradeDirectionEnum, FundDirectionEnum.getBycode(accountInfo.getFundDirection()));
        //the happened amount of trade
        BigDecimal afterAmount = BigDecimal.ZERO;
        if (StringUtils.equals(changeDirectionEnum.getCode(), ChangeDirectionEnum.INCREASE.getCode())) {
            afterAmount = accountInfo.getBalance().add(happenAmount);
        } else if (StringUtils.equals(changeDirectionEnum.getCode(), ChangeDirectionEnum.DECREASE.getCode())) {
            afterAmount = accountInfo.getBalance().subtract(happenAmount);
        }
        //check usable balance = afterAmount - freeze
        if (afterAmount.subtract(accountInfo.getFreezeAmount()).compareTo(BigDecimal.ZERO) < 0) {
            log.error("[accountOperation.validateTradeInfo] account balance is not enough,accountNo:{}",
                accountInfo.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_BALANCE_IS_NOT_ENOUGH_ERROR);
        }
        return afterAmount;
    }

    /**
     * get balance change direction
     *
     * @param tradeDirectionEnum
     * @param fundDirectionEnum
     * @return
     */
    public static ChangeDirectionEnum getBalanceChangeDirection(TradeDirectionEnum tradeDirectionEnum,
        FundDirectionEnum fundDirectionEnum) {
        if (fundDirectionEnum == null) {
            log.error("[accountOperation.getBalanceChangeDirection] fundDirectionEnum is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_FUND_DIRECTION_IS_NULL_ERROR);
        }
        ChangeDirectionEnum changeDirectionEnum = null;
        if (StringUtils.equals(tradeDirectionEnum.getCode(), TradeDirectionEnum.DEBIT.getCode())) {
            //default INCREASE
            changeDirectionEnum = ChangeDirectionEnum.INCREASE;
            //when account fund direction is credit
            if (StringUtils.equals(fundDirectionEnum.getCode(),FundDirectionEnum.CREDIT.getCode())) {
                changeDirectionEnum = ChangeDirectionEnum.DECREASE;
            }
        } else if (StringUtils.equals(tradeDirectionEnum.getCode(), TradeDirectionEnum.CREDIT.getCode())) {
            //default INCREASE
            changeDirectionEnum = ChangeDirectionEnum.INCREASE;
            //when account fund direction is debit
            if (StringUtils.equals(fundDirectionEnum.getCode(),FundDirectionEnum.DEBIT.getCode())) {
                changeDirectionEnum = ChangeDirectionEnum.DECREASE;
            }
        }
        return changeDirectionEnum;
    }
}
