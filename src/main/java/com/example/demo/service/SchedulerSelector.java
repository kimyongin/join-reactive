package com.example.demo.service;

import reactor.core.scheduler.Scheduler;

@FunctionalInterface
public interface SchedulerSelector {
    Scheduler select(String actorId);
}