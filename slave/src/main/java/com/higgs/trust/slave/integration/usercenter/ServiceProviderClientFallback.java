package com.higgs.trust.slave.integration.usercenter;

import com.higgs.trust.slave.integration.usercenter.vo.User;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ServiceProviderClient feign接口类的fallback类，与该接口声明的接口一一对应
 *
 * @author young001
 */
@Service public class ServiceProviderClientFallback implements ServiceProviderClient {

    @Override public String consumeServiceProviderHome(@RequestParam(value = "name") String name) {
        return "fallback info";
    }

    @Override public String consumeServiceProviderData(@RequestBody User user) {
        return "fallback info";
    }

    @Override public String trace2() {
        return "fallback info";

    }

    @Override public String trace3() {
        return "fallback info";
    }
}
