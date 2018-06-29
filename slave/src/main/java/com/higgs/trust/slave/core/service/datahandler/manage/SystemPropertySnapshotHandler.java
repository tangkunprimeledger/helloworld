package com.higgs.trust.slave.core.service.datahandler.manage;

import com.higgs.trust.slave.api.vo.SystemPropertyVO;
import com.higgs.trust.slave.core.service.snapshot.agent.SystemPropertySnapshotAgent;
import com.higgs.trust.slave.model.bo.config.SystemProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SystemProperty snapshot handler
 *
 * @author lingchao
 * @create 2018年06月29日10:51
 */
@Service
public class SystemPropertySnapshotHandler {
    @Autowired
    private SystemPropertySnapshotAgent systemPropertySnapshotAgent;

    /**
     * query System Property by key
     *
     * @param key
     * @return
     */
    public SystemPropertyVO querySystemPropertyByKey(String key) {
        SystemPropertyVO systemPropertyVO = null;
        if (StringUtils.isBlank(key)) {
            return systemPropertyVO;
        }
        SystemProperty systemProperty = systemPropertySnapshotAgent.querySystemPropertyByKey(key);
        if (null != systemProperty) {
            systemPropertyVO = new SystemPropertyVO();
            BeanUtils.copyProperties(systemProperty, systemPropertyVO);
        }
        return systemPropertyVO;
    }

}
