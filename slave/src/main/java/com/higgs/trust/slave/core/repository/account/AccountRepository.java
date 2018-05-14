package com.higgs.trust.slave.core.repository.account;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.api.enums.account.AccountStateEnum;
import com.higgs.trust.slave.api.enums.account.ChangeDirectionEnum;
import com.higgs.trust.slave.api.vo.AccountInfoVO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.DataIdentityRepository;
import com.higgs.trust.slave.dao.account.AccountDcRecordDao;
import com.higgs.trust.slave.dao.account.AccountDetailDao;
import com.higgs.trust.slave.dao.account.AccountInfoDao;
import com.higgs.trust.slave.dao.po.account.AccountDcRecordPO;
import com.higgs.trust.slave.dao.po.account.AccountDetailPO;
import com.higgs.trust.slave.dao.po.account.AccountInfoPO;
import com.higgs.trust.slave.dao.po.account.AccountInfoWithOwnerPO;
import com.higgs.trust.slave.model.bo.DataIdentity;
import com.higgs.trust.slave.model.bo.account.AccountDcRecord;
import com.higgs.trust.slave.model.bo.account.AccountDetail;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import com.higgs.trust.slave.model.bo.account.OpenAccount;
import com.higgs.trust.slave.model.convert.DataIdentityConvert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-10
 */
@Repository @Slf4j public class AccountRepository {
    @Autowired AccountInfoDao accountInfoDao;
    @Autowired AccountDetailDao accountDetailDao;
    @Autowired AccountDcRecordDao accountDcRecordDao;
    @Autowired DataIdentityRepository dataIdentityRepository;

    /**
     * query account info by account no
     *
     * @param accountNo
     * @param forUpdate
     * @return
     */
    public AccountInfo queryAccountInfo(String accountNo, boolean forUpdate) {
        AccountInfoPO accountInfo = accountInfoDao.queryByAccountNo(accountNo, forUpdate);
        return BeanConvertor.convertBean(accountInfo, AccountInfo.class);
    }

    /**
     * batch query the account info
     *
     * @param accountNos
     * @return
     */
    public List<AccountInfoVO> queryByAccountNos(List<String> accountNos) {
        List<AccountInfoPO> list = accountInfoDao.queryByAccountNos(accountNos);
        return BeanConvertor.convertList(list, AccountInfoVO.class);
    }

    /**
     * build an new account info
     *
     * @param openAccount
     * @return
     */
    public AccountInfo buildAccountInfo(OpenAccount openAccount) {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setAccountNo(openAccount.getAccountNo());
        accountInfo.setCurrency(openAccount.getCurrency());
        accountInfo.setBalance(BigDecimal.ZERO);
        accountInfo.setFreezeAmount(BigDecimal.ZERO);
        accountInfo.setFundDirection(openAccount.getFundDirection().getCode());
        accountInfo.setDetailNo(0L);
        accountInfo.setDetailFreezeNo(0L);
        accountInfo.setStatus(AccountStateEnum.NORMAL.getCode());
        accountInfo.setCreateTime(new Date());
        return accountInfo;
    }

    /**
     * open an new account
     *
     * @param openAccount
     */
    public AccountInfo openAccount(OpenAccount openAccount) {
        // build and add account info
        AccountInfo accountInfo = buildAccountInfo(openAccount);
        AccountInfoPO accountInfoPO = BeanConvertor.convertBean(accountInfo, AccountInfoPO.class);
        try {
            accountInfoDao.add(accountInfoPO);
        } catch (DuplicateKeyException e) {
            log.error("[openAccount.persist] is idempotent for accountNo:{}", openAccount.getAccountNo());
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
        // add data identity
        DataIdentity dataIdentity = DataIdentityConvert
            .buildDataIdentity(openAccount.getAccountNo(), openAccount.getChainOwner(), openAccount.getDataOwner());
        dataIdentityRepository.save(dataIdentity);
        return accountInfo;
    }

    /**
     * operation account balance,increase or decrease
     *
     * @param accountNo
     * @param changeDirectionEnum
     * @param happenAmount
     */
    public void operationAccountBalance(String accountNo, ChangeDirectionEnum changeDirectionEnum,
        BigDecimal happenAmount) {
        int r = 0;
        //change account balance
        if (StringUtils.equals(changeDirectionEnum.getCode(), ChangeDirectionEnum.INCREASE.getCode())) {
            r = accountInfoDao.increaseBalance(accountNo, happenAmount);
        } else if (StringUtils.equals(changeDirectionEnum.getCode(), ChangeDirectionEnum.DECREASE.getCode())) {
            r = accountInfoDao.decreaseBalance(accountNo, happenAmount);
        }
        if (r == 0) {
            log.error("[operationAccountBalance]change account balance is fail,accountNo:{}", accountNo);
            throw new SlaveException(SlaveErrorEnum.SLAVE_ACCOUNT_CHANGE_BALANCE_ERROR);
        }
    }

    /**
     * create new account detail
     *
     * @param accountDetail
     */
    public void createAccountDetail(AccountDetail accountDetail) {
        AccountDetailPO detailPO = BeanConvertor.convertBean(accountDetail, AccountDetailPO.class);
        accountDetailDao.add(detailPO);
    }

    /**
     * create new account DC record
     *
     * @param accountDcRecord
     */
    public void createAccountDCRecord(AccountDcRecord accountDcRecord) {
        AccountDcRecordPO accountDcRecordPO = BeanConvertor.convertBean(accountDcRecord, AccountDcRecordPO.class);
        accountDcRecordDao.add(accountDcRecordPO);
    }

    public List<AccountInfoVO> queryAccountInfoWithOwner(String accountNo, String dataOwner, Integer pageNo, Integer pageSize) {
        if (null != accountNo) {
            accountNo = accountNo.trim();
        }

        if (null != dataOwner) {
            dataOwner = dataOwner.trim();
        }

        List<AccountInfoWithOwnerPO> list = accountInfoDao.queryAccountInfoWithOwner(accountNo, dataOwner, (pageNo - 1) * pageSize, pageSize);
        return BeanConvertor.convertList(list, AccountInfoVO.class);
    }

    public long countAccountInfoWithOwner(String accountNo, String dataOwner) {
        if (null != accountNo) {
            accountNo = accountNo.trim();
        }

        if (null != dataOwner) {
            dataOwner = dataOwner.trim();
        }

        return accountInfoDao.countAccountInfoWithOwner(accountNo, dataOwner);
    }
}
