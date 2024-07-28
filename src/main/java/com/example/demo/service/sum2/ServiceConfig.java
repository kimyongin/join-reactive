package com.example.demo.service.sum2;

import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class ServiceConfig {

    @Bean
    public SchedulerSelector schedulerSelector() {
        Scheduler evenScheduler = Schedulers.fromExecutor(Executors.newSingleThreadExecutor());
        Scheduler oddScheduler = Schedulers.fromExecutor(Executors.newSingleThreadExecutor());
        return value -> value % 2 == 0 ? evenScheduler : oddScheduler;
    }

    @Bean
    public ThreadLocalManager<Integer> sumManager() {
        ThreadLocal<Integer> threadLocalSum = ThreadLocal.withInitial(() -> 0);
        return () -> threadLocalSum;
    }

    @Bean
    public EventOperatorServiceSum2 eventOperatorServiceSum2(SchedulerSelector selector, ThreadLocalManager<Integer> manager) {
        return new EventOperatorServiceSum2(selector, manager);
    }
}
