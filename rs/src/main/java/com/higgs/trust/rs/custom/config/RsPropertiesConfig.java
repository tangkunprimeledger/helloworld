package com.higgs.trust.rs.custom.config;

import cn.primeledger.pl.crypto.CryptoUtils;
import cn.primeledger.pl.crypto.ECKey;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * PropertySource加载方式：ref:https://blog.jayway.com/2014/02/16/spring-propertysource/
 * http://docs.spring.io/spring/docs/4.0.0.RELEASE/javadoc-api/org/springframework/context/annotation/PropertySource.html
 * 线上必须是用file:,必须通行证为-DappConf="file:/data/home/admin/bankchain/conf/application.json11"
 */
@Configuration
public class RsPropertiesConfig implements InitializingBean {

    @NotNull
    @Value("${bankchain.maintenance.mode}")
    private boolean coinchainMaintenanceMode;

    /**
     * 队列最大请求数
     */
    @NotNull
    @Value("${bankchain.request.processing.max}")
    private Integer maxProcessingRequest;
    /**
     * 最大请求处理线程数
     */
    @NotNull
    @Value("${bankchain.request.processing.threadNum}")
    private Integer maxProcessingThreadNum;

    @NotNull
    @Value("${bankchain.bizName}")
    private String bizName;

    /**
     * vn回调的时候重试的最大次数
     */
    @NotNull
    @Min(0)
    @Max(10)
    @Value("${bankchain.vn.retry.max}")
    private Integer vnRetryMax;

    /**
     * vn回调的间隔，单位为ms
     */
    @NotNull
    @Min(30)
    @Max(200)
    @Value("${bankchain.vn.retry.interval}")
    private Integer vnRetryInterval;

    /**
     * vn回调的最大间隔，单位为ms
     */
    @NotNull
    @Value("${bankchain.vn.retry.maxinterval}")
    private Integer vnRetryMaxInterval;

    @NotNull
    @Max(30000)
    @Min(500)
    @Value("${bankchain.vn.request.timeout}")
    private Long vnRequestTimeout;

    @Value("${bankchain.schedule.issue_init}")
    private Long schedule_issue_init;
    @Value("${bankchain.schedule.issue_process}")
    private Long schedule_issue_process;
    @Value("${bankchain.schedule.issue_notify}")
    private Long schedule_issue_notify;

    @NotNull
    @Value("${core.status.rootPath}")
    private String coreStatusRootPath;
    @NotBlank
    @Value("${core.rs.id}")
    private String coreRsId;
    @NotBlank
    @Value("${core.dubbo.registry.address}")
    private String coreRsDubboAddr;
    @NotBlank
    @Value("${core.rs.merchantNo}")
    private String coreRsMerchantNo;


    /**
     * cas realm与钱包对接  使用的公钥
     */
    @NotBlank
    @Value("${cas.pubkey}")
    private String pubKey;

    /**
     *     cas realm与钱包对接  使用的私钥
     */

    @NotBlank
    @Value("${cas.prikey}")
    private String priKey;

    /**
     *     cas realm与钱包对接  使用的aesKey
     */
    @NotBlank
    @Value("${cas.aeskey}")
    private String aesKey;

    private ECKey ecKey;

    @Override
    public void afterPropertiesSet() throws Exception {
//        CoinChainBeanValidator.validate(this).failThrow();
    }

    public String getCoreRsId() {
        return coreRsId;
    }

    public void setCoreRsId(String coreRsId) {
        this.coreRsId = coreRsId;
    }

    public String getCoreRsDubboAddr() {
        return coreRsDubboAddr;
    }

    public void setCoreRsDubboAddr(String coreRsDubboAddr) {
        this.coreRsDubboAddr = coreRsDubboAddr;
    }

    public String getCoreRsMerchantNo() {
        return coreRsMerchantNo;
    }

    public void setCoreRsMerchantNo(String coreRsMerchantNo) {
        this.coreRsMerchantNo = coreRsMerchantNo;
    }

    public String getCoreStatusRootPath() {
        return coreStatusRootPath;
    }

    public void setCoreStatusRootPath(String coreStatusRootPath) {
        this.coreStatusRootPath = coreStatusRootPath;
    }

    public Integer getMaxProcessingRequest() {
        return maxProcessingRequest;
    }

    public void setMaxProcessingRequest(Integer maxProcessingRequest) {
        this.maxProcessingRequest = maxProcessingRequest;
    }

    public Integer getMaxProcessingThreadNum() {
        return maxProcessingThreadNum;
    }

    public void setMaxProcessingThreadNum(Integer maxProcessingThreadNum) {
        this.maxProcessingThreadNum = maxProcessingThreadNum;
    }

    public Integer getVnRetryMax() {
        return vnRetryMax;
    }

    public void setVnRetryMax(Integer vnRetryMax) {
        this.vnRetryMax = vnRetryMax;
    }

    public Integer getVnRetryInterval() {
        return vnRetryInterval;
    }

    public void setVnRetryInterval(Integer vnRetryInterval) {
        this.vnRetryInterval = vnRetryInterval;
    }

    public Integer getVnRetryMaxInterval() {
        return vnRetryMaxInterval;
    }

    public void setVnRetryMaxInterval(Integer vnRetryMaxInterval) {
        this.vnRetryMaxInterval = vnRetryMaxInterval;
    }

    public Long getVnRequestTimeout() {
        return vnRequestTimeout;
    }

    public void setVnRequestTimeout(Long vnRequestTimeout) {
        this.vnRequestTimeout = vnRequestTimeout;
    }

    public boolean isCoinchainMaintenanceMode() {
        return coinchainMaintenanceMode;
    }

    public void setCoinchainMaintenanceMode(boolean coinchainMaintenanceMode) {
        this.coinchainMaintenanceMode = coinchainMaintenanceMode;
    }

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public Long getSchedule_issue_init() {
        return schedule_issue_init;
    }

    public void setSchedule_issue_init(Long schedule_issue_init) {
        this.schedule_issue_init = schedule_issue_init;
    }

    public Long getSchedule_issue_process() {
        return schedule_issue_process;
    }

    public void setSchedule_issue_process(Long schedule_issue_process) {
        this.schedule_issue_process = schedule_issue_process;
    }

    public Long getSchedule_issue_notify() {
        return schedule_issue_notify;
    }

    public void setSchedule_issue_notify(Long schedule_issue_notify) {
        this.schedule_issue_notify = schedule_issue_notify;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public ECKey getEcKey() {
        return ECKey.fromPrivate(CryptoUtils.HEX.decode(priKey));
    }

    public void setEcKey(ECKey ecKey) {
        this.ecKey = ecKey;
    }
}

