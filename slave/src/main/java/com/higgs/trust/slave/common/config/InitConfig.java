package com.higgs.trust.slave.common.config;

import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

/**
 * @author tangfashuang
 */
@Configuration @Setter @Getter
public class InitConfig implements InitializingBean{
    @NotNull @Value("${trust.useMySQL:true}") private boolean useMySQL;
    @Value("${trust.utxo.display:2}")
    private int DISPLAY;

    @Override public void afterPropertiesSet() throws Exception {
        BeanValidator.validate(this).failThrow();
    }
}
