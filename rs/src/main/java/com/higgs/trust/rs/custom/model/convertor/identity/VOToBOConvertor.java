package com.higgs.trust.rs.custom.model.convertor.identity;

import com.higgs.trust.rs.custom.api.vo.identity.IdentityRequestVO;
import com.higgs.trust.rs.custom.api.vo.identity.IdentityVO;
import com.higgs.trust.rs.custom.model.bo.identity.Identity;
import com.higgs.trust.rs.custom.model.bo.identity.IdentityRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

/*
 * @desc 存证相关数据对象之间的转换
 * @author WangQuanzhou
 * @date 2018/3/5 15:09
 */
@Slf4j
public class VOToBOConvertor {

    /**
     * @param identityVO
     * @return Identity
     * @desc IdentityVO转成IdentityBO
     */
    public static Identity convertIdentityVOToBO(IdentityVO identityVO) {
        if (null == identityVO) {
            log.warn("[convertIdentityVOToBO] identityVO is null");
            return null;
        }
        Identity identity = new Identity();
        BeanUtils.copyProperties(identity,identity);
        return identity;
    }

    public static IdentityRequest convertIdentityRequestVOToBO(IdentityRequestVO identityRequestVO) {
        if (null == identityRequestVO){
            log.warn("[convertIdentityRequestVOToBO] identityRequestVO is null");
            return null;
        }
        IdentityRequest identityRequest = new IdentityRequest();
        BeanUtils.copyProperties(identityRequestVO,identityRequest);
        return identityRequest;
    }
}
