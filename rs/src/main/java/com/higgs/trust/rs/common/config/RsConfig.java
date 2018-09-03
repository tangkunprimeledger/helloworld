package com.higgs.trust.rs.common.config;

import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration @Setter @Getter public class RsConfig implements InitializingBean {
    @NotNull @Value("${higgs.trust.nodeName}") private String rsName;
    @NotNull @Value("${rs.core.useHttpChannel:false}") private boolean useHttpChannel;
    @NotNull @Value("${rs.core.syncRequestTimeout:200}") private long syncRequestTimeout;
    @NotNull @Value("${rs.core.batchCallback:true}") private boolean batchCallback;
    @NotNull @Value("${server.port}") private int serverPort;
    @NotNull @Value("${trust.useMySQL:true}") private boolean useMySQL;


    @Override public void afterPropertiesSet() throws Exception {
        BeanValidator.validate(this).failThrow();
    }
}
