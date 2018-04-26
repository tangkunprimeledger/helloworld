package com.higgs.trust.slave.core.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * test
 *
 * @author lingchao
 * @create 2018年04月26日14:57
 */
@Service
@Slf4j
public class SchedulerTest {
    @Scheduled(fixedRate = 10000)
    public void testFixedRate1() {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date())+"*********A任务睡20秒每10秒执行一次进入测试");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Scheduled(fixedRate = 5000)
    public void testFixedRate2() {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date())+"*********B任务睡0秒每5秒执行一次进入测试");
    }
}
