package com.higgs.trust.rs.custom.model.convertor.identity;

import com.higgs.trust.rs.custom.api.vo.identity.IdentityVO;
import com.higgs.trust.rs.custom.dao.po.identity.IdentityPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

/*
 * @desc 存证相关数据对象之间的转换
 * @author WangQuanzhou
 * @date 2018/3/5 15:09
 */
@Slf4j
public class POToVOConvertor {

    /**
     * @param identityPO
     * @return IdentityVO
     * @desc identityPO转成IdentityVO
     */
    public static IdentityVO convertidentityPOToVO(IdentityPO identityPO) {
        if (null == identityPO) {
            log.warn("[convertIdentityVOToBO] identityPO is null");
            return null;
        }
        IdentityVO identityVO = new IdentityVO();
        BeanUtils.copyProperties(identityPO,identityVO);
        return identityVO;
    }

}

