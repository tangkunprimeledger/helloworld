package com.higgs.trust.network.springboot;

import com.higgs.trust.network.NetworkManage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author duhongming
 * @date 2018/9/11
 */
@RestController
@RequestMapping(path = "/demo")
public class DemoController {
//    @Autowired
//    NetworkManage networkManage;

    @Autowired
    PersonBean personBean;

    public DemoController() {
        System.out.println("DemoController ...");
    }

    @GetMapping(path = "/hello")
    public String hello() {
        NetworkManage.getInstance().start();
        return NetworkManage.getInstance().config().nodeName() +
                " " + personBean.toString() + " " + ContextAware.port ;
    }
}
