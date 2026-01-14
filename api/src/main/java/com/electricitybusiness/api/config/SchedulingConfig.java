package com.electricitybusiness.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Clock;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(8);
        ts.setThreadNamePrefix("booking-scheduler-");
        ts.initialize();
        return ts;
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
