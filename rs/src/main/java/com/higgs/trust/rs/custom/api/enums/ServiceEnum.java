package com.higgs.trust.rs.custom.api.enums;

/**
 * Created by young001 on 2017/6/17.
 */
public enum ServiceEnum {

    OPENACCOUNT(ServiceCategoryEnum.BIZ, "api.user.openAccount", "1003", "coinchain_open_account", "coinchain_open_account_latency"),
    ISSUECOIN(ServiceCategoryEnum.BIZ, "api.user.issueCoin", "1006", "coinchain_issue_coin", "coinchain_issue_coin_latency"),

    TRANSFER(ServiceCategoryEnum.BIZ, "api.user.transfer", "1007", "coinchain_transfer_coin", "coinchain_transfer_coin_latency"),
    SENDIDENTITY(ServiceCategoryEnum.BIZ, "api.user.storageIdentity", "1008", "bankchain_async_storageIdentity", "bankchain_async_storageIdentity_latency");

    // 对于正常业务请求和管理接口进行区分，服务接口设置为service，管理接口设置为mng
    private ServiceCategoryEnum category;
    private String name;

    //给每个业务RS分配一个业务编号，这个看未来是否有分配逻辑，目前保证不冲突即可
    private String bizCode;

    private String tpsMonitorTarget;

    private String latencyMonitorTarget;

    ServiceEnum(ServiceCategoryEnum category, String name, String bizCode, String tpsMonitorTarget, String latencyMonitorTarget) {
        this.category = category;
        this.name = name;
        this.bizCode = bizCode;
        this.tpsMonitorTarget = tpsMonitorTarget;
        this.latencyMonitorTarget = latencyMonitorTarget;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ServiceEnum getServiceByName(String name) {
        for (ServiceEnum serviceEnum : ServiceEnum.values()) {
            if (serviceEnum.getName().equals(name)) {
                return serviceEnum;
            }
        }
        return null;
    }

    public static ServiceEnum getMngServiceByName(String name) {
        for (ServiceEnum serviceEnum : ServiceEnum.values()) {
            if (serviceEnum.getName().equals(name)) {
                if (serviceEnum.category.equals(ServiceCategoryEnum.MNG)) {
                    return serviceEnum;
                }
            }
        }
        return null;
    }

    public static ServiceEnum getBizServiceByName(String name) {
        for (ServiceEnum serviceEnum : ServiceEnum.values()) {
            if (serviceEnum.getName().equals(name)) {
                if (serviceEnum.category.equals(ServiceCategoryEnum.BIZ)) {
                    return serviceEnum;
                }
            }
        }
        return null;
    }

    public static ServiceEnum getServiceByBizCode(String bizCode) {
        for (ServiceEnum serviceEnum : ServiceEnum.values()) {
            if (serviceEnum.getBizCode().equals(bizCode)) {
                return serviceEnum;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getBizCode() {
        return bizCode;
    }

    public String getTpsMonitorTarget() {
        return tpsMonitorTarget;
    }

    public String getLatencyMonitorTarget() {
        return latencyMonitorTarget;
    }
}

