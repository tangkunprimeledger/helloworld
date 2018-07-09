package com.higgs.trust.slave.core.service.datahandler.account;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.api.enums.account.AccountFreezeTypeEnum;
import com.higgs.trust.slave.api.enums.account.ChangeDirectionEnum;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.api.enums.account.TradeDirectionEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.AmountUtil;
import com.higgs.trust.slave.core.repository.DataIdentityRepository;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.core.repository.account.AccountRepository;
import com.higgs.trust.slave.core.repository.account.CurrencyRepository;
import com.higgs.trust.slave.core.repository.account.FreezeRepository;
import com.higgs.trust.slave.core.service.action.dataidentity.DataIdentityService;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.model.bo.DataIdentity;
import com.higgs.trust.slave.model.bo.account.*;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author liuyu
 * @description
 * @date 2018-04-16
 */
@Component @Slf4j public class AccountDBHandler implements AccountHandler {
    @Autowired AccountRepository accountRepository;
    @Autowired CurrencyRepository currencyRepository;
    @Autowired DataIdentityRepository dataIdentityRepository;
    @Autowired PolicyRepository policyRepository;
    @Autowired MerkleService merkleService;
    @Autowired DataIdentityService dataIdentityService;
    @Autowired FreezeRepository freezeRepository;

    @Override public AccountInfo getAccountInfo(String accountNo) {
        return accountRepository.queryAccountInfo(accountNo, false);
    }

    @Override public CurrencyInfo queryCurrency(String currency) {
        return currencyRepository.queryByCurrency(currency);
    }

    @Override public void openAccount(OpenAccount openAccount) {
        //account db
        AccountInfo accountInfo = accountRepository.openAccount(openAccount);
        AccountMerkleData _new = BeanConvertor.convertBean(accountInfo,AccountMerkleData.class);
        //operation merkle tree
        MerkleTree merkleTree = merkleService.queryMerkleTree(MerkleTypeEnum.ACCOUNT);
        if (merkleTree == null) {
            merkleTree = merkleService.build(MerkleTypeEnum.ACCOUNT, Arrays.asList(new Object[] {_new}));
        } else {
            merkleService.add(merkleTree, _new);
        }
    }

    @Override public void validateForOperation(AccountOperation accountOperation, String policyId) {
        // validate business
        List<AccountTradeInfo> debitTradeInfo = accountOperation.getDebitTradeInfo();
        List<AccountTradeInfo> creditTradeInfo = accountOperation.getCreditTradeInfo();
        //data identity
        List<DataIdentity> dataIdentitys = new ArrayList<>();
        //the account currency,to ensure the consistent
        Set<String> currencySet = new HashSet<>();
        //for the trial balance
        BigDecimal debitAmounts = BigDecimal.ZERO;
        BigDecimal creditAmounts = BigDecimal.ZERO;
        //DEBIT trade
        for (AccountTradeInfo info : debitTradeInfo) {
            if(!AmountUtil.isLegal(String.valueOf(info.getAmount()),true)){
                log.error("[validateForOperation] amount:{} is illegal",info.getAmount());
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }
            AccountInfo accountInfo = accountRepository.queryAccountInfo(info.getAccountNo(), false);
            if (accountInfo == null) {
                log.error("[validateForOperation] account info is not exists by accountNo:{}", info.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_IS_NOT_EXISTS_ERROR);
            }
            //validate happen amount and return after amount
            AccountOperationHelper.validateTradeInfo(accountInfo, TradeDirectionEnum.DEBIT, info.getAmount());
            //check last chain owner
            DataIdentity dataIdentity = dataIdentityRepository.queryDataIdentity(accountInfo.getAccountNo());
            if (dataIdentity == null) {
                log.error("[validateForOperation] dataIdentityPO is not exists by accountNo:{}", info.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_DATA_IDENTITY_NOT_EXISTS_ERROR);
            }
            //hold dataIdentityPO
            dataIdentitys.add(dataIdentity);
            //hold currency
            currencySet.add(accountInfo.getCurrency());
            //for the trial balance
            debitAmounts = debitAmounts.add(info.getAmount());
        }
        //CREDIT trade
        for (AccountTradeInfo info : creditTradeInfo) {
            if(!AmountUtil.isLegal(String.valueOf(info.getAmount()),true)){
                log.error("[validateForOperation] amount:{} is illegal",info.getAmount());
                throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
            }
            AccountInfo accountInfo = accountRepository.queryAccountInfo(info.getAccountNo(), false);
            if (accountInfo == null) {
                log.error("[validateForOperation] account info is not exists by accountNo:{}", info.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_IS_NOT_EXISTS_ERROR);
            }
            //validate happen amount and return after amount
            AccountOperationHelper.validateTradeInfo(accountInfo, TradeDirectionEnum.CREDIT, info.getAmount());
            //check last chain owner
            DataIdentity dataIdentity = dataIdentityRepository.queryDataIdentity(accountInfo.getAccountNo());
            if (dataIdentity == null) {
                log.error("[validateForOperation] dataIdentityPO is not exists by accountNo:{}", info.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_DATA_IDENTITY_NOT_EXISTS_ERROR);
            }
            //hold dataIdentityPO
            dataIdentitys.add(dataIdentity);
            //hold currency
            currencySet.add(accountInfo.getCurrency());
            //for the trial balance
            creditAmounts = creditAmounts.add(info.getAmount());
        }
        //check currency
        if (currencySet.size() != 1) {
            log.error("[validateForOperation] account currency is not consistent");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_CURRENCY_IS_NOT_CONSISTENT_ERROR);
        }
        //get policy
        Policy policy = policyRepository.getPolicyById(policyId);
        if (policy == null) {
            log.error("[validateForOperation] policy is not exists exception");
            throw new SlaveException(SlaveErrorEnum.SLAVE_POLICY_IS_NOT_EXISTS_EXCEPTION);
        }
        //check data identity
        boolean r = dataIdentityService.validate(policy.getRsIds(), dataIdentitys);
        if (!r) {
            log.error("[validateForOperation] account check data owner is error,rsIds:{}", policy.getRsIds());
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_CHECK_DATA_OWNER_ERROR);
        }
        //check the trial balance
        if (debitAmounts.compareTo(creditAmounts) != 0) {
            log.error("[validateForOperation] the trial balance check is fail");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_TRIAL_BALANCE_ERROR);
        }
    }

    @Override public void persistForOperation(AccountOperation accountOperation, Long blockHeight) {
        List<AccountTradeInfo> debitTradeInfo = accountOperation.getDebitTradeInfo();
        List<AccountTradeInfo> creditTradeInfo = accountOperation.getCreditTradeInfo();
        //DEBIT trade
        for (AccountTradeInfo info : debitTradeInfo) {
            //lock this account info
            AccountInfo accountInfo = accountRepository.queryAccountInfo(info.getAccountNo(), false);
            if (accountInfo == null) {
                log.error("[persistForOperation] account info is not exists by accountNo:{}",
                    info.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_IS_NOT_EXISTS_ERROR);
            }
            persistEachAccount(accountOperation, accountInfo, TradeDirectionEnum.DEBIT, info.getAmount(),
                blockHeight);
        }
        //CREDIT trade
        for (AccountTradeInfo info : creditTradeInfo) {
            //lock this account info
            AccountInfo accountInfo = accountRepository.queryAccountInfo(info.getAccountNo(), false);
            if (accountInfo == null) {
                log.error("[persistForOperation] account info is not exists by accountNo:{}",
                    info.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_IS_NOT_EXISTS_ERROR);
            }
            persistEachAccount(accountOperation, accountInfo, TradeDirectionEnum.CREDIT, info.getAmount(),
                blockHeight);
        }
    }

    @Override public AccountFreezeRecord getAccountFreezeRecord(String bizFlowNo, String accountNo) {
        return freezeRepository.queryByFlowNoAndAccountNo(bizFlowNo,accountNo);
    }

    @Override public void freeze(AccountFreeze accountFreeze,Long blockHeight) {
        AccountInfo accountInfo = getAccountInfo(accountFreeze.getAccountNo());
        Date currentDate = new Date();
        AccountFreezeRecord freezeRecord = new AccountFreezeRecord();
        freezeRecord.setBizFlowNo(accountFreeze.getBizFlowNo());
        freezeRecord.setAccountNo(accountFreeze.getAccountNo());
        freezeRecord.setBlockHeight(blockHeight);
        freezeRecord.setAmount(accountFreeze.getAmount());
        freezeRecord.setContractAddr(accountFreeze.getContractAddr());
        freezeRecord.setCreateTime(currentDate);
        freezeRepository.createFreezeRecord(freezeRecord);
        //freeze account balance
        freezeRepository.freeze(accountFreeze.getAccountNo(), accountFreeze.getAmount());
        //save freezeDetail
        AccountDetailFreeze detailFreeze = new AccountDetailFreeze();
        detailFreeze.setBizFlowNo(accountFreeze.getBizFlowNo());
        detailFreeze.setAccountNo(accountFreeze.getAccountNo());
        detailFreeze.setBlockHeight(blockHeight);
        detailFreeze.setAmount(accountFreeze.getAmount());
        detailFreeze.setBeforeAmount(accountInfo.getFreezeAmount());
        detailFreeze.setAfterAmount(accountInfo.getFreezeAmount().add(accountFreeze.getAmount()));
        detailFreeze.setFreezeDetailNo(accountInfo.getDetailFreezeNo() + 1);
        detailFreeze.setFreezeType(AccountFreezeTypeEnum.FREEZE.getCode());
        detailFreeze.setRemark(accountFreeze.getRemark());
        detailFreeze.setCreateTime(currentDate);
        freezeRepository.createFreezeDetail(detailFreeze);
        //merkle tree
        AccountInfo _new = BeanConvertor.convertBean(accountInfo,AccountInfo.class);
        _new.setFreezeAmount(accountInfo.getFreezeAmount().add(accountFreeze.getAmount()));
        updateMerkleTree(accountInfo,_new);
    }

    @Override
    public void unfreeze(AccountUnFreeze accountUnFreeze, AccountFreezeRecord freezeRecord, Long blockHeight) {
        AccountInfo accountInfo = getAccountInfo(accountUnFreeze.getAccountNo());
        //unfreeze record`s amount
        freezeRepository.decreaseAmount(freezeRecord.getId(), accountUnFreeze.getAmount());
        //unfreeze account balance
        freezeRepository.unfreeze(accountUnFreeze.getAccountNo(), accountUnFreeze.getAmount());
        //save freezeDetail
        AccountDetailFreeze detailFreeze = new AccountDetailFreeze();
        detailFreeze.setBizFlowNo(accountUnFreeze.getBizFlowNo());
        detailFreeze.setAccountNo(accountUnFreeze.getAccountNo());
        detailFreeze.setBlockHeight(blockHeight);
        detailFreeze.setAmount(accountUnFreeze.getAmount());
        detailFreeze.setBeforeAmount(accountInfo.getFreezeAmount());
        detailFreeze.setAfterAmount(accountInfo.getFreezeAmount().subtract(accountUnFreeze.getAmount()));
        detailFreeze.setFreezeDetailNo(accountInfo.getDetailFreezeNo() + 1);
        detailFreeze.setFreezeType(AccountFreezeTypeEnum.UNFREEZE.getCode());
        detailFreeze.setRemark(accountUnFreeze.getRemark());
        detailFreeze.setCreateTime(new Date());
        freezeRepository.createFreezeDetail(detailFreeze);
        //merkle tree
        AccountInfo _new = BeanConvertor.convertBean(accountInfo,AccountInfo.class);
        _new.setFreezeAmount(accountInfo.getFreezeAmount().subtract(accountUnFreeze.getAmount()));
        updateMerkleTree(accountInfo,_new);
    }

    @Override public void issueCurrency(IssueCurrency bo) {
        CurrencyInfo currencyInfo = currencyRepository.buildCurrencyInfo(bo.getCurrencyName(),bo.getRemark());
        currencyRepository.create(currencyInfo);
    }

    /**
     * persist each account info
     * <p>
     * 1.validate account:status, usable balance
     * 2.persist account balance by happenAmount
     * 3.persist account detail record
     * 4.persist account DC record
     *
     * @param bo
     * @param accountInfo
     * @param tradeDirectionEnum
     * @param happenAmount
     */
    private void persistEachAccount(AccountOperation bo, AccountInfo accountInfo, TradeDirectionEnum tradeDirectionEnum,
        BigDecimal happenAmount, Long blockHeight) {
        //validate trade info
        BigDecimal afterAmount =
            AccountOperationHelper.validateTradeInfo(accountInfo, tradeDirectionEnum, happenAmount);
        //process account balance and save account detail record
        processAccountBalance(bo, accountInfo, tradeDirectionEnum, happenAmount, afterAmount, blockHeight);
        //save account DC record
        saveAccountDCRecord(bo, accountInfo.getAccountNo(), tradeDirectionEnum, happenAmount);
        //update balance
        AccountInfo _new = BeanConvertor.convertBean(accountInfo, AccountInfo.class);
        _new.setBalance(afterAmount);
        //update merkle-tree
        updateMerkleTree(accountInfo, _new);
    }

    /**
     * process account balance and save account detail record
     *
     * @param bo
     * @param accountInfo
     * @param tradeDirectionEnum
     * @param happenAmount
     * @param afterAmount
     */
    private void processAccountBalance(AccountOperation bo, AccountInfo accountInfo,
        TradeDirectionEnum tradeDirectionEnum, BigDecimal happenAmount, BigDecimal afterAmount, Long blockHeight) {
        //get change direction
        ChangeDirectionEnum changeDirectionEnum = AccountOperationHelper
            .getBalanceChangeDirection(tradeDirectionEnum, FundDirectionEnum.getBycode(accountInfo.getFundDirection()));
        //change account balance
        accountRepository.operationAccountBalance(accountInfo.getAccountNo(), changeDirectionEnum, happenAmount);
        //save account detail
        AccountDetail detail = new AccountDetail();
        detail.setBizFlowNo(bo.getBizFlowNo());
        detail.setBlockHeight(blockHeight);
        detail.setAccountNo(accountInfo.getAccountNo());
        detail.setChangeDirection(changeDirectionEnum.getCode());
        detail.setAmount(happenAmount);
        detail.setBeforeAmount(accountInfo.getBalance());
        detail.setAfterAmount(afterAmount);
        detail.setDetailNo(accountInfo.getDetailNo() + 1);
        detail.setRemark(bo.getRemark());
        detail.setCreateTime(bo.getAccountDate());
        accountRepository.createAccountDetail(detail);
        log.info("[accountOperation.processAccountBalance]is success,accountNo:{}", accountInfo.getAccountNo());
    }

    /**
     * save account DC record
     *
     * @param bo
     * @param accountNo
     * @param tradeDirectionEnum
     * @param amount
     */
    private void saveAccountDCRecord(AccountOperation bo, String accountNo, TradeDirectionEnum tradeDirectionEnum,
        BigDecimal amount) {
        AccountDcRecord accountDcRecord = new AccountDcRecord();
        accountDcRecord.setBizFlowNo(bo.getBizFlowNo());
        accountDcRecord.setAccountNo(accountNo);
        accountDcRecord.setAmount(amount);
        accountDcRecord.setDcFlag(tradeDirectionEnum.getCode());
        accountDcRecord.setCreateTime(bo.getAccountDate());
        accountRepository.createAccountDCRecord(accountDcRecord);
        log.info("[accountOperation.saveAccountDCRecord]is success,accountNo:{}", accountNo);
    }

    /**
     * update merkle tree
     *
     * @param _old
     * @param _new
     */
    private void updateMerkleTree(AccountInfo _old, AccountInfo _new) {
        MerkleTree merkleTree = merkleService.queryMerkleTree(MerkleTypeEnum.ACCOUNT);
        if (merkleTree == null) {
            log.error("[updateMerkleTree] the ACCOUNT merkle tree does not exist");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_MERKLE_TREE_NOT_EXIST_ERROR);
        }
        //update
        merkleService.update(merkleTree, BeanConvertor.convertBean(_old, AccountMerkleData.class),
            BeanConvertor.convertBean(_new, AccountMerkleData.class));
    }
}
