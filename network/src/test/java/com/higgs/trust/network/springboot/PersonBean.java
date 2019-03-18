package com.higgs.trust.network.springboot;

import com.higgs.trust.network.NetworkManage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author duhongming
 * @date 2018/9/12
 */
@Service
public class PersonBean implements InitializingBean {
    @Autowired
    NetworkManage networkManage;
    @Autowired
    ContextAware contextAware;

    public int port;

    @Override
    public void afterPropertiesSet() throws Exception {
//        System.out.println(Thread.currentThread().toString());
//        Arrays.stream(Thread.currentThread().getStackTrace()).forEach(System.out::println);
        System.out.println("PersonBean InitializingBean afterPropertiesSet..." + port);
    }
}
