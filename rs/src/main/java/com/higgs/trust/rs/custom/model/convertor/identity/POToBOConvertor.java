package com.higgs.trust.rs.custom.model.convertor.identity;

import com.higgs.trust.rs.custom.dao.po.BankChainRequestPO;
import com.higgs.trust.rs.custom.dao.po.identity.IdentityRequestPO;
import com.higgs.trust.rs.custom.model.bo.BankChainRequest;
import com.higgs.trust.rs.custom.model.bo.identity.IdentityRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/*
 * @desc 存证相关数据对象之间的转换
 * @author WangQuanzhou
 * @date 2018/3/8 15:30
 */
@Slf4j
public class POToBOConvertor {

    public static BankChainRequest convertBankChainRequestPOToBO(BankChainRequestPO bankChainRequestPO) {
        if (null == bankChainRequestPO) {
            log.warn("[convertBankChainRequestPOToBO] bankChainRequestPO is null");
            return null;
        }
        BankChainRequest bankChainRequest = new BankChainRequest();
        BeanUtils.copyProperties(bankChainRequestPO,bankChainRequest);
        return bankChainRequest;
    }


    public static IdentityRequest convertIdentityRequestPOToBO(IdentityRequestPO identityRequestPO) {
        if (null == identityRequestPO) {
            log.warn("[convertIdentityRequestPOToBO] identityRequestPO is null");
            return null;
        }
        IdentityRequest identityRequest = new IdentityRequest();
        BeanUtils.copyProperties(identityRequestPO,identityRequest);
        return identityRequest;
    }

}
