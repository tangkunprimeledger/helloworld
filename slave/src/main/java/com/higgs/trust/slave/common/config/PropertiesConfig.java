package com.higgs.trust.slave.common.config;

import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration @Setter @Getter public class PropertiesConfig implements InitializingBean {
    @NotNull @Value("${consensus.p2p.data.dir}") private String p2pDataDir;

    @NotNull @Value("${consensus.p2p.faultNodeNum}") private Integer p2pFaultNodeNum;

    @NotNull @Value("${consensus.p2p.cluster}") private String p2pClusterJson;
    @NotNull @Value("${runMode}") private String runMode;

    @Override public void afterPropertiesSet() throws Exception {
        BeanValidator.validate(this).failThrow();
    }

    /**
     * system is run by mock model
     * @return
     */
    public boolean isMock(){
        return StringUtils.equals("MOCK",runMode);
    }
}
