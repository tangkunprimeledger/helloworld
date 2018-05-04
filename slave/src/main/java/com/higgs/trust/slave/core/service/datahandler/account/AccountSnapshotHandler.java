package com.higgs.trust.slave.core.service.datahandler.account;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.api.enums.account.TradeDirectionEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.AmountUtil;
import com.higgs.trust.slave.core.repository.account.FreezeRepository;
import com.higgs.trust.slave.core.service.action.dataidentity.DataIdentityService;
import com.higgs.trust.slave.core.service.snapshot.agent.*;
import com.higgs.trust.slave.model.bo.DataIdentity;
import com.higgs.trust.slave.model.bo.account.*;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author liuyu
 * @description
 * @date 2018-04-16
 */
@Component @Slf4j public class AccountSnapshotHandler implements AccountHandler {
    @Autowired AccountSnapshotAgent accountSnapshotAgent;
    @Autowired MerkleTreeSnapshotAgent merkleTreeSnapshotAgent;
    @Autowired FreezeSnapshotAgent freezeSnapshotAgent;
    @Autowired DataIdentitySnapshotAgent dataIdentitySnapshotAgent;
    @Autowired ManageSnapshotAgent manageSnapshotAgent;
    @Autowired DataIdentityService dataIdentityService;
    @Autowired FreezeRepository freezeRepository;

    @Override public AccountInfo getAccountInfo(String accountNo) {
        return accountSnapshotAgent.getAccountInfo(accountNo);
    }

    @Override public CurrencyInfo queryCurrency(String currency) {
        return accountSnapshotAgent.queryCurrency(currency);
    }

    @Override public void openAccount(OpenAccount openAccount) {
        AccountInfo accountInfo = accountSnapshotAgent.openAccount(openAccount);
        // operation merkle tree
        MerkleTree merkleTree = merkleTreeSnapshotAgent.getMerkleTree(MerkleTypeEnum.ACCOUNT);
        AccountMerkleData data = BeanConvertor.convertBean(accountInfo, AccountMerkleData.class);
        if (merkleTree == null) {
            merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.ACCOUNT, new Object[] {data});
        } else {
            merkleTreeSnapshotAgent.appendChild(merkleTree, data);
        }
    }

    @Override public void validateForOperation(AccountOperation accountOperation, String policyId) {
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
            AccountInfo accountInfo = accountSnapshotAgent.getAccountInfo(info.getAccountNo());
            if (accountInfo == null) {
                log.error("[validateForOperation] account info is not exists by accountNo:{}", info.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_IS_NOT_EXISTS_ERROR);
            }
            //validate happen amount and return after amount
            AccountOperationHelper.validateTradeInfo(accountInfo, TradeDirectionEnum.DEBIT, info.getAmount());
            //check last chain owner
            DataIdentity dataIdentity = dataIdentitySnapshotAgent.getDataIdentity(accountInfo.getAccountNo());
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
            AccountInfo accountInfo = accountSnapshotAgent.getAccountInfo(info.getAccountNo());
            if (accountInfo == null) {
                log.error("[validateForOperation] account info is not exists by accountNo:{}", info.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_IS_NOT_EXISTS_ERROR);
            }
            //validate happen amount and return after amount
            AccountOperationHelper.validateTradeInfo(accountInfo, TradeDirectionEnum.CREDIT, info.getAmount());
            //check last chain owner
            DataIdentity dataIdentity = dataIdentitySnapshotAgent.getDataIdentity(accountInfo.getAccountNo());
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
        Policy policy = manageSnapshotAgent.getPolicy(policyId);
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
            AccountInfo accountInfo = accountSnapshotAgent.getAccountInfo(info.getAccountNo());
            if (accountInfo == null) {
                log.error("[persistForOperation] account info is not exists by accountNo:{}", info.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_IS_NOT_EXISTS_ERROR);
            }
            //validate happen amount and return after amount
            BigDecimal afterAmount =
                AccountOperationHelper.validateTradeInfo(accountInfo, TradeDirectionEnum.DEBIT, info.getAmount());
            AccountInfo _new = BeanConvertor.convertBean(accountInfo, AccountInfo.class);
            _new.setBalance(afterAmount);
            //update the balance to merkle tree
            updateMerkleTree(accountInfo, _new);
            //update the new balance to snapshot
            accountSnapshotAgent.updateAccountInfo(BeanConvertor.convertBean(_new, AccountInfo.class));
        }
        //CREDIT trade
        for (AccountTradeInfo info : creditTradeInfo) {
            AccountInfo accountInfo = accountSnapshotAgent.getAccountInfo(info.getAccountNo());
            if (accountInfo == null) {
                log.error("[persistForOperation] account info is not exists by accountNo:{}", info.getAccountNo());
                throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_IS_NOT_EXISTS_ERROR);
            }
            //validate happen amount and return after amount
            BigDecimal afterAmount =
                AccountOperationHelper.validateTradeInfo(accountInfo, TradeDirectionEnum.CREDIT, info.getAmount());
            AccountInfo _new = BeanConvertor.convertBean(accountInfo, AccountInfo.class);
            _new.setBalance(afterAmount);
            //update the balance to merkle tree
            updateMerkleTree(accountInfo, _new);
            //update the new balance to snapshot
            accountSnapshotAgent.updateAccountInfo(BeanConvertor.convertBean(_new, AccountInfo.class));
        }
    }

    @Override public AccountFreezeRecord getAccountFreezeRecord(String bizFlowNo, String accountNo) {
        return freezeSnapshotAgent.getAccountFreezeRecord(bizFlowNo, accountNo);
    }

    @Override public void freeze(AccountFreeze accountFreeze, Long blockHeight) {
        AccountInfo accountInfo = accountSnapshotAgent.getAccountInfo(accountFreeze.getAccountNo());
        AccountInfo _new = BeanConvertor.convertBean(accountInfo, AccountInfo.class);
        _new.setFreezeAmount(accountInfo.getFreezeAmount().add(accountFreeze.getAmount()));
        //modify merkle tree
        updateMerkleTree(accountInfo, _new);
        //save to snapshot
        accountSnapshotAgent.updateAccountInfo(BeanConvertor.convertBean(_new, AccountInfo.class));
        //save freeze record
        freezeSnapshotAgent.createAccountFreezeRecord(freezeRepository.build(accountFreeze, blockHeight));
    }

    @Override
    public void unfreeze(AccountUnFreeze accountUnFreeze, AccountFreezeRecord freezeRecord, Long blockHeight) {
        AccountInfo accountInfo = accountSnapshotAgent.getAccountInfo(accountUnFreeze.getAccountNo());
        AccountInfo _new = BeanConvertor.convertBean(accountInfo, AccountInfo.class);
        BigDecimal afterOfAccount = accountInfo.getFreezeAmount().subtract(accountUnFreeze.getAmount());
        _new.setFreezeAmount(afterOfAccount);
        //modify merkle tree
        updateMerkleTree(accountInfo, _new);
        //update account info to snapshot
        accountSnapshotAgent.updateAccountInfo(BeanConvertor.convertBean(_new, AccountInfo.class));
        //save to snapshot
        freezeRecord.setAmount(freezeRecord.getAmount().subtract(accountUnFreeze.getAmount()));
        freezeSnapshotAgent.updateAccountFreezeRecord(freezeRecord);
    }

    @Override public void issueCurrency(IssueCurrency bo) {
        accountSnapshotAgent.issueCurrency(bo);
    }

    /**
     * update merkle tree
     *
     * @param _old
     * @param _new
     */
    private void updateMerkleTree(AccountInfo _old, AccountInfo _new) {
        MerkleTree merkleTree = merkleTreeSnapshotAgent.getMerkleTree(MerkleTypeEnum.ACCOUNT);
        if (merkleTree == null) {
            log.error("[updateMerkleTree] the ACCOUNT merkle tree does not exist");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_MERKLE_TREE_NOT_EXIST_ERROR);
        }
        AccountMerkleData from = BeanConvertor.convertBean(_old, AccountMerkleData.class);
        AccountMerkleData to = BeanConvertor.convertBean(_new, AccountMerkleData.class);
        merkleTreeSnapshotAgent.modifyMerkleTree(merkleTree, from, to);
    }
}
