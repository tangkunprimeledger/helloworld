package com.higgs.trust.slave.core.repository.account;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.model.bo.account.CurrencyInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: com.higgs.trust.slave.core.repository.account
 * @author: lingchao
 * @datetime:2018年12月03日11:07
 **/
public class CurrencyRepositoryTest extends BaseTest {
    @Autowired
    private CurrencyRepository currencyRepository;
    @Test
    public void batchInsertTest(){
        List<CurrencyInfo> currencyInfos = new ArrayList<>(4);
        CurrencyInfo currencyInfo = new CurrencyInfo();
        currencyInfo.setCurrency("BTO");
        currencyInfo.setHomomorphicPk("123123");
        currencyInfo.setContractAddress("qqqqqqqqqqqqqqqqqqqqqq");

        CurrencyInfo currencyInfo1 = new CurrencyInfo();
        BeanUtils.copyProperties(currencyInfo, currencyInfo1);
        currencyInfo1.setContractAddress(null);
        currencyInfo1.setCurrency("BTOC");

        CurrencyInfo currencyInfo2 = new CurrencyInfo();
        BeanUtils.copyProperties(currencyInfo, currencyInfo2);
        currencyInfo2.setHomomorphicPk(null);
        currencyInfo2.setCurrency("BTOCC");

        CurrencyInfo currencyInfo3 = new CurrencyInfo();
        BeanUtils.copyProperties(currencyInfo, currencyInfo3);
        currencyInfo3.setHomomorphicPk(null);
        currencyInfo3.setContractAddress(null);
        currencyInfo3.setCurrency("BTOCCC");

        currencyInfos.add(currencyInfo);
        currencyInfos.add(currencyInfo1);
        currencyInfos.add(currencyInfo2);
        currencyInfos.add(currencyInfo3);

        currencyRepository.batchInsert(currencyInfos);
    }

    @Test
    public void queryByCurrencyTest(){
        System.out.println(currencyRepository.queryByCurrency("BTO"));
    }


}
