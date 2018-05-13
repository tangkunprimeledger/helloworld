package com.higgs.trust.rs.custom.biz.api.impl;

import com.higgs.trust.rs.custom.api.IPropertiesService;
import com.higgs.trust.rs.custom.api.enums.BankChainExceptionCodeEnum;
import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.rs.custom.config.RsPropertiesConfig;
import com.higgs.trust.rs.custom.model.MaintanenceModeBO;
import com.higgs.trust.rs.custom.model.RespData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.higgs.trust.rs.custom.util.MonitorLogUtils.logBankChainIntMonitorInfo;

/**
 * Created by lingchao on 2018/2/26.
 */

@Service
public class PropertiesServiceImpl implements IPropertiesService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesServiceImpl.class);

    @Autowired
    private RsPropertiesConfig propertiesConfig;

    @Autowired
    private MaintenanceModeService maintenanceModeService;

    @Override
    public RespData<?> maintanenceSetter(MaintanenceModeBO maintanenceModeBO) {
        RespData<?> respData = null;
        try {
            LOGGER.info("{}业务修改维护模式为:{}", propertiesConfig.getBizName(), maintanenceModeBO.getMaintanenceMode());
            if (maintanenceModeBO.getMaintanenceMode()) {
                maintenanceModeService.startOrResume();
            } else {
                maintenanceModeService.pause();
            }
            respData = new RespData<>(RespCodeEnum.PROPERTIES_SETTER_SUCCESS);
        } catch (Throwable e) {
            LOGGER.error("系统异常", e);
            logBankChainIntMonitorInfo(BankChainExceptionCodeEnum.BCSystemException.getMonitorTarget(), 1);
            respData = new RespData<>(RespCodeEnum.SYS_FAIL);
        }

        return respData;
    }

}
