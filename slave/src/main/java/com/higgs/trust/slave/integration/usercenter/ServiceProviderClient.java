package com.higgs.trust.slave.integration.usercenter;

import com.higgs.trust.slave.integration.usercenter.vo.User;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import feign.Feign;
import feign.Target;
import feign.hystrix.FallbackFactory;
import feign.hystrix.HystrixFeign;
import feign.hystrix.SetterFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;

/**
 * 对于service的调用，FeignClient中的value是调用service的名字
 *
 * @author young001
 */
@FeignClient(name = "trace2") @Profile({"feign"}) public interface ServiceProviderClient {

    int DEFAULT_TIME_OUT = 1000;
    int TRACE_2_TIME_OUT = 400;
    int TRACE_3_TIME_OUT = 800;

    @RequestMapping(value = "/provider_home", method = RequestMethod.POST) String consumeServiceProviderHome(
        @RequestParam(value = "name") String name);

    @RequestMapping(value = "/provider_data", method = RequestMethod.POST) String consumeServiceProviderData(
        @RequestBody User user);

    @RequestMapping(value = "/trace2", method = RequestMethod.GET) String trace2();

    @RequestMapping(value = "/trace3", method = RequestMethod.GET) String trace3();

    @Slf4j @Configuration class HystrixConfig {
        @Bean @Scope("prototype") public Feign.Builder feignHystrixBuilder() {
            return HystrixFeign.builder().setterFactory(new SetterFactory() {
                @Override public HystrixCommand.Setter create(Target<?> target, Method method) {
                    int timeOut = DEFAULT_TIME_OUT;
                    if (StringUtils.equals("trace2", method.getName())) {
                        timeOut = TRACE_2_TIME_OUT;
                    } else if (StringUtils.equals("trace3", method.getName())) {
                        timeOut = TRACE_3_TIME_OUT;
                    }
                    log.debug("set {}.{} HystrixCommand timeout:{}ms", ServiceProviderClient.class.getSimpleName(),
                        method.getName(), timeOut);
                    return HystrixCommand.Setter
                        .withGroupKey(HystrixCommandGroupKey.Factory.asKey(ServiceProviderClient.class.getSimpleName()))
                        .andCommandKey(HystrixCommandKey.Factory.asKey(method.getName())).andCommandPropertiesDefaults(
                            HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(timeOut)

                        );
                }
            });
        }
    }

    @Slf4j @Component class ServiceProviderClientFallbackFactory implements FallbackFactory<ServiceProviderClient> {
        @Override public ServiceProviderClient create(Throwable cause) {
            return new ServiceProviderClient() {
                @Override public String consumeServiceProviderHome(@RequestParam(value = "name") String name) {
                    return "fallback info";
                }

                @Override public String consumeServiceProviderData(@RequestBody User user) {
                    return "fallback info";
                }

                @Override public String trace2() {
                    log.error("access trace2 fallback: ", cause);
                    return "trace2 fallback info";
                }

                @Override public String trace3() {
                    log.error("access trace3 fallback: ", cause);
                    return "trace3 fallback info";
                }
            };
        }
    }
}
