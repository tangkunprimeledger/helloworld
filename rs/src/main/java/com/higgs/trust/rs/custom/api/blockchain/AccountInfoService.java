package com.higgs.trust.rs.custom.api.blockchain;

import com.higgs.trust.rs.custom.api.vo.blockchain.AccountQueryVO;
import com.higgs.trust.rs.custom.model.RespData;

/**
 * @author tangfashuang
 */
public interface AccountInfoService {
    /**
     * query account
     * @param req
     * @return
     */
    RespData queryAccount(AccountQueryVO req);
}
