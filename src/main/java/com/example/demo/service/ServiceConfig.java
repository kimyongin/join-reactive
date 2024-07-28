package com.example.demo.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        return actorId -> actorId.hashCode() % 2 == 0 ? evenScheduler : oddScheduler;
    }

    @Bean
    public ThreadLocalManager<Map<String/*actorId*/, Integer/*sum*/>> eventAccumulatorTLS() {
        ThreadLocal<Map<String/*actorId*/, Integer/*sum*/>> threadLocal =
            ThreadLocal.withInitial(HashMap::new);
        return () -> threadLocal;
    }

    @Bean
    public ThreadLocalManager<Map<String/*actorId*/, Set<String/*eventType*/>>> eventFilterTLS() {
        ThreadLocal<Map<String/*actorId*/, Set<String/*eventType*/>>> threadLocal =
            ThreadLocal.withInitial(HashMap::new);
        return () -> threadLocal;
    }
}
