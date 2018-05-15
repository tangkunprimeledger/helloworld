package com.higgs.trust.rs.custom.controller.outter.v1;

import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.rs.custom.api.identity.IdentityService;
import com.higgs.trust.rs.custom.api.vo.identity.IdentityRequestVO;
import com.higgs.trust.rs.custom.model.RespData;
import com.higgs.trust.rs.custom.model.convertor.identity.VOToBOConvertor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;

/*
 * @desc 存证相关对外接口
 * @author WangQuanzhou
 * @date 2018/3/2 17:56
 */
@RequestMapping(value = "/v1/identity")
@RestController
@Slf4j
public class IdentityController {

    @Autowired
    private IdentityService identityService;

    @RequestMapping(value = "/storage")
    public RespData storageIdentity(@RequestBody @Valid IdentityRequestVO identityRequestVO, BindingResult result) {
        log.info("[storageIdentity] storage identity request receive. {}", identityRequestVO.toString());

        if (result.hasErrors()) {
            log.error("[storageIdentity] storage identity request param is invalid, errMsg={}", result.getAllErrors());
            return new RespData(RespCodeEnum.PARAM_NOT_VALID);
        }

        try{
            if (identityRequestVO.getValue().getBytes("utf-8").length>8192){
                log.error("[storageIdentity] storage identity request param is invalid, the length (utf-8 format) of value exceed 8192, size={}", identityRequestVO.getValue().getBytes("utf-8").length);
                return new RespData(RespCodeEnum.PARAM_NOT_VALID);
            }
        }catch (UnsupportedEncodingException e){
            log.error("[storageIdentity] unsupported encoding exception,", e);
            return new RespData(RespCodeEnum.SYS_FAIL);
        }


        log.info("[storageIdentity] storage identity request handle start. reqNo={}", identityRequestVO.getReqNo());
        return identityService.acceptRequest(VOToBOConvertor.convertIdentityRequestVOToBO(identityRequestVO));
    }

    @RequestMapping(value = "/query/key")
    public RespData queryIdentityByKey(String key) {
        log.info("[queryIdentityByKey] query request receive. {}", key);

        if (StringUtils.isBlank(key)) {
            log.error("[queryIdentityByKey] query request param is invalid, key should not be null");
            return new RespData(RespCodeEnum.PARAM_NOT_VALID);
        }

        log.info("[queryIdentityByKey] query request handle start. key={}", key);
        return identityService.queryIdentityByKey(key);
    }


    @RequestMapping(value = "/query/reqNo")
    public RespData queryIdentityByReqNo(String reqNo) {
        log.info("[queryIdentityByReqNo] query request receive. {}", reqNo);

        if (StringUtils.isBlank(reqNo)) {
            log.error("[queryIdentityByReqNo] query request param is invalid, key should not be null");
            return new RespData(RespCodeEnum.PARAM_NOT_VALID);
        }

        log.info("[queryIdentityByReqNo] query request handle start. reqNo={}", reqNo);
        return identityService.queryIdentityByReqNo(reqNo);
    }
}
