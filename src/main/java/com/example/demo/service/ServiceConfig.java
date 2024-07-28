package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

@Configuration
public class ServiceConfig {

    @Bean
    public SchedulerSelector schedulerSelector() {
        Scheduler evenScheduler = Schedulers.fromExecutor(Executors.newSingleThreadExecutor());
        Scheduler oddScheduler = Schedulers.fromExecutor(Executors.newSingleThreadExecutor());
        return actorId -> actorId.hashCode() % 2 == 0 ? evenScheduler : oddScheduler;
    }

    @Bean
    public ThreadLocalManager<Map<String/*actorId*/, Integer/*sum*/>> eventAccumulatorTLS() {
        ThreadLocal<Map<String/*actorId*/, Integer/*sum*/>> threadLocal =
            ThreadLocal.withInitial(HashMap::new);
        return () -> threadLocal;
    }

    @Bean
    public ThreadLocalManager<Map<String/*actorId*/, Map<String/*eventType*/, Tuple2<Long, Long>/*start-end*/>>> eventFilterTLS() {
        ThreadLocal<Map<String/*actorId*/, Map<String/*eventType*/, Tuple2<Long, Long>/*start-end*/>>> threadLocal =
            ThreadLocal.withInitial(HashMap::new);
        return () -> threadLocal;
    }
}
