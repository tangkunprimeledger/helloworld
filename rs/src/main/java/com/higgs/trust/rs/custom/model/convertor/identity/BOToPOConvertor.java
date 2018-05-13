package com.higgs.trust.rs.custom.model.convertor.identity;

import com.higgs.trust.rs.custom.dao.po.identity.IdentityRequestPO;
import com.higgs.trust.rs.custom.model.bo.identity.IdentityRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

/*
 * @desc 存证相关数据对象之间的转换
 * @author WangQuanzhou
 * @date 2018/3/5 15:09
 */
@Slf4j
public class BOToPOConvertor {

    public static IdentityRequestPO convertIdentityRequestBOToPO(IdentityRequest identityRequest) {
        if (null == identityRequest) {
            log.warn("[convertIdentityRequestBOToPO] identityRequestBO is null");
            return null;
        }
        IdentityRequestPO identityRequestPO = new IdentityRequestPO();
        BeanUtils.copyProperties(identityRequest,identityRequestPO);
        return identityRequestPO;
    }
}
