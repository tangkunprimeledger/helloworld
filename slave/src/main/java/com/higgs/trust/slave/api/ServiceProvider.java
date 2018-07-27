package com.higgs.trust.slave.api;

import com.higgs.trust.slave.common.constant.LoggerName;
import com.higgs.trust.slave.common.util.MonitorLogUtils;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.integration.usercenter.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j @RestController public class ServiceProvider {

    private static final Logger PERF_LOGGER = LoggerFactory.getLogger(LoggerName.PERF_LOGGER);

    @Value("${server.port}") String port;

    @RequestMapping("/provider_home") public String providerHome(@RequestParam String name) {
        Profiler.start("profile_log_start");
        Profiler.enter("第一阶段");
        log.info("hello");
        Profiler.release();
        Profiler.enter("第二阶段");
        MonitorLogUtils.logIntMonitorInfo("hello_target", 1);
        Profiler.release();
        Profiler.enter("第三阶段");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }
        Profiler.release();
        Profiler.enter("第四阶段");
        log.info("forth phase");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        Profiler.release();
        Profiler.release();
        if (Profiler.getDuration() > 0) {
            PERF_LOGGER.info(Profiler.dump());
        }
        return "hi " + name + ",i am from port:" + port;
    }

    @RequestMapping("/provider_data") public String providerData(@RequestBody User user) {
        return "hi " + user.getName() + ",your age:" + user.getAge();
    }
}
