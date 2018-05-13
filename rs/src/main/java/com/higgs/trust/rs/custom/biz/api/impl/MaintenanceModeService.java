package com.higgs.trust.rs.custom.biz.api.impl;

import com.higgs.trust.rs.custom.api.StatefulService;
import com.higgs.trust.rs.custom.api.enums.MaintenanceModeSwitchEnum;
import com.higgs.trust.rs.custom.config.PropertiesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MaintenanceModeService extends StatefulService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceModeService.class);
    private volatile MaintenanceModeSwitchEnum maintenanceModeSwitch = MaintenanceModeSwitchEnum.OFF;
    @Autowired
    private PropertiesConfig propertiesConfig;

    @Override
    public String getStatefulServiceName() {
        return "maintenance_mode";
    }

    @Override
    protected void doStart() {
        maintenanceModeSwitch = MaintenanceModeSwitchEnum.ON;
        propertiesConfig.setCoinchainMaintenanceMode(maintenanceModeSwitch.isOn());

    }

    @Override
    protected void doPause() {
        maintenanceModeSwitch = MaintenanceModeSwitchEnum.OFF;
        propertiesConfig.setCoinchainMaintenanceMode(maintenanceModeSwitch.isOn());
    }

    @Override
    protected void doResume() {
        maintenanceModeSwitch = MaintenanceModeSwitchEnum.ON;
        propertiesConfig.setCoinchainMaintenanceMode(maintenanceModeSwitch.isOn());
    }

    public MaintenanceModeSwitchEnum getMaintenanceModeSwitch() {
        return maintenanceModeSwitch;
    }


}
