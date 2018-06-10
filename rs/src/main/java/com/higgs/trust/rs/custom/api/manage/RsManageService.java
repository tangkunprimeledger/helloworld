package com.higgs.trust.rs.custom.api.manage;

import com.higgs.trust.rs.custom.api.vo.manage.CancelRsVO;
import com.higgs.trust.rs.custom.api.vo.manage.RegisterPolicyVO;
import com.higgs.trust.rs.custom.api.vo.manage.RegisterRsVO;
import com.higgs.trust.slave.api.vo.RespData;

/**
 * @author tangfashuang
 * @date 2018/05/18 13:56
 * @desc rs manage service
 */
public interface RsManageService {

    /**
     * register rs
     * @param registerRsVO
     * @return
     */
    RespData registerRs(RegisterRsVO registerRsVO);

    /**
     * register policy
     * @param registerPolicyVO
     * @return
     */
    RespData registerPolicy(RegisterPolicyVO registerPolicyVO);

    /**
     * cancel RS
     * @param cancelRsVO
     * @return
     */
    RespData cancelRs(CancelRsVO cancelRsVO);
}
