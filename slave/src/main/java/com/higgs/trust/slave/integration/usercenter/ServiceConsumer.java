package com.higgs.trust.slave.integration.usercenter;

import com.higgs.trust.slave.integration.usercenter.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对于service的调用，如果启动feign的profile，才会启用
 *
 * @author yangjiyun
 */

@RestController @Profile({"feign"}) public class ServiceConsumer {

    @Autowired ServiceProviderClient serviceProviderClient;

    @RequestMapping(value = "/hi", method = RequestMethod.GET) public String sayHi(@RequestParam String name) {
        return serviceProviderClient.consumeServiceProviderHome(name);
    }

    @RequestMapping(value = "/data", method = RequestMethod.POST) public String sayData(@RequestParam String name) {
        User user = new User(name, 10);
        return serviceProviderClient.consumeServiceProviderData(user);
    }

}