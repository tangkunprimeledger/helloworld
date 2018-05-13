package com.higgs.trust.rs.custom.controller.outter.v1;

import com.higgs.trust.rs.custom.api.IPropertiesService;
import com.higgs.trust.rs.custom.api.enums.BankChainExceptionCodeEnum;
import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.rs.custom.model.MaintanenceModeBO;
import com.higgs.trust.rs.custom.model.RespData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.higgs.trust.rs.custom.util.MonitorLogUtils.logBankChainIntMonitorInfo;

/**
 * Properties 控制层
 *
 * @author lingchao
 * @create 2018年02月26日16:08
 */
@RequestMapping(value = "/v1/sys")
@RestController
public class PropertiesController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesController.class);
    @Autowired
    private IPropertiesService iPropertiesService;

    /**
     * 设置系统管控模式，这是用在系统  出现异常时。让系统不接收流量
     * @param maintanenceModeBO
     * @param result
     * @return
     */
    @RequestMapping(value = "/maintanence", method = RequestMethod.POST)
    public RespData<?> maintanenceSetter(@Valid MaintanenceModeBO maintanenceModeBO, BindingResult result) {
        RespData<?> respData = new RespData<>();
        if (result.hasErrors()) {
            respData.setCode(RespCodeEnum.PARAM_NOT_VALID);
            LOGGER.error("管控模式入参异常，具体信息：{}", result.getAllErrors());
            logBankChainIntMonitorInfo(BankChainExceptionCodeEnum.BCRequestParamInvalidException.getMonitorTarget(), 1);
            return respData;
        }
        return iPropertiesService.maintanenceSetter(maintanenceModeBO);
    }
}
