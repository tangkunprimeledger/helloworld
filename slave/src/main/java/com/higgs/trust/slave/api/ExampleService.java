package com.higgs.trust.slave.api;

import com.higgs.trust.slave.dao.po.account.AccountInfoPO;

/**
 * @Description:
 * @author: pengdi
 **/
public interface ExampleService {
    /**
     * 保存
     *
     * @param accountInfo 保存对象
     * @return false
     */
    boolean save(AccountInfoPO accountInfo);

    /**
     * 删除
     *
     * @param userId 用户id
     * @return false
     */
    boolean remove(Long userId);
}
