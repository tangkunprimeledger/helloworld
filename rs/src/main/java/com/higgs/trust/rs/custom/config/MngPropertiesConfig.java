package com.higgs.trust.rs.custom.config;

import com.higgs.trust.rs.custom.api.enums.UpperRequestRunningStatus;
import com.higgs.trust.rs.custom.model.enums.identity.IdentityEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Configuration
public class MngPropertiesConfig implements InitializingBean {
    @NotNull
    @Value("${bankchain.async.identity.request.status}")
    private volatile IdentityEnum acceptAsyncRequestStatus;


    @NotNull
    @Value("${rs.failover.user.request.status}")
    private volatile UpperRequestRunningStatus acceptBizRequestStatus = UpperRequestRunningStatus.RUNNING;

    /**
     * 当挂起的操作放开之后会把请求往下发，一次放最大的数量
     */
    @Min(10)
    @Max(1000)
    @Value("${rs.failover.request.async.max.request}")
    private int asyncProcessingMaxNum;

    @Override
    public void afterPropertiesSet() throws Exception {
//        CoinChainBeanValidator.validate(this).failThrow();
    }

    public IdentityEnum getAcceptAsyncRequestStatus() {
        return acceptAsyncRequestStatus;
    }

    public void setAcceptAsyncRequestStatus(IdentityEnum acceptAsyncRequestStatus) {
        this.acceptAsyncRequestStatus = acceptAsyncRequestStatus;
    }

    public int getAsyncProcessingMaxNum() {
        return asyncProcessingMaxNum;
    }

    public void setAsyncProcessingMaxNum(int asyncProcessingMaxNum) {
        this.asyncProcessingMaxNum = asyncProcessingMaxNum;
    }

    public UpperRequestRunningStatus getAcceptBizRequestStatus() {
        return acceptBizRequestStatus;
    }

    public void setAcceptBizRequestStatus(UpperRequestRunningStatus acceptBizRequestStatus) {
        this.acceptBizRequestStatus = acceptBizRequestStatus;
    }
}

