package com.higgs.trust.slave.api;

import com.higgs.trust.slave.api.vo.AccountInfoVO;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-05-09
 */
public interface AccountInfoService {
    /**
     * batch query the account info
     *
     * @param accountNos
     * @return
     */
    List<AccountInfoVO> queryByAccountNos(List<String> accountNos);
}
