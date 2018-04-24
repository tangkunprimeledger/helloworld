package com.higgs.trust.slave.core.scheduler;

import com.higgs.trust.slave.integration.usercenter.ServiceProviderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Spring 的@Scheduled注释定时器
 *
 * @author yuguojia
 * @create 2018-01-04 10:55
 */
@Service @Slf4j @Profile({"scheduler"}) public class SchedulerDemo {
    @Autowired ServiceProviderClient serviceProviderClient;
    @Autowired RestTemplate restTemplate;

    @Scheduled(cron = "0/10 * *  * * ? ")   //每5秒执行一次
    public void executeScheduled() {
        log.info("spring scheduled feign hello world");
        String result = serviceProviderClient.trace2();
        log.info("feign trace2 result: " + result);
    }

    @Scheduled(cron = "0/10 * *  * * ? ")   //每5秒执行一次
    public void executeRestTemplateScheduled() {
        log.info("begin restTemplate trace2");
        String result = restTemplate.getForEntity("http://trace2/trace2", String.class).getBody();
        log.info("restTemplate trace2 result: " + result);
    }

    @Scheduled(cron = "0/10 * *  * * ? ")   //每5秒执行一次
    public void executeRestTemplateScheduled2() {
        log.info("2 begin restTemplate trace2");
        String result = restTemplate.getForEntity("http://trace2/trace2", String.class).getBody();
        log.info("2 restTemplate trace2 result: " + result);
    }

    private void traceFallBack() {
        log.info("restTemplate trace2 error");
    }
}