package com.higgs.trust.rs.custom.api;

import com.higgs.trust.rs.custom.model.MaintanenceModeBO;
import com.higgs.trust.rs.custom.model.RespData;

/**
 * Created by lingchao on 2018/2/26.
 */
public interface IPropertiesService {
    /**
     * 管控模式设置
     *
     * @param maintanenceModeBO
     * @return
     */
    RespData<?> maintanenceSetter(MaintanenceModeBO maintanenceModeBO);
}
