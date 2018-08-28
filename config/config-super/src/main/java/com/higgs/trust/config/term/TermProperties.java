package com.higgs.trust.config.term;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Zhu_Yuanxiang
 * @create 2018-08-28
 */
@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties(prefix = "config.term")
public class TermProperties {
    private int maxTermsSize=64;
}
