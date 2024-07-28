package com.example.demo.service.sum2;

import reactor.core.scheduler.Scheduler;

@FunctionalInterface
public interface SchedulerSelector {
    Scheduler select(int value);
}