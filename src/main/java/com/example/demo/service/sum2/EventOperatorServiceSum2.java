package com.example.demo.service.sum2;

import com.example.demo.model.Event;
import com.example.demo.model.Result;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EventOperatorServiceSum2 {
    private final SchedulerSelector schedulerSelector;
    private final ThreadLocalManager<Integer> sumManager;

    public EventOperatorServiceSum2(SchedulerSelector schedulerSelector, ThreadLocalManager<Integer> sumManager) {
        this.schedulerSelector = schedulerSelector;
        this.sumManager = sumManager;
    }

    public Mono<Result> operate(Event event) {
        return Mono.just(event)
            .publishOn(schedulerSelector.select(event.getContent()))
            .map(this::processEvent);
    }

    private Result processEvent(Event event) {
        ThreadLocal<Integer> currentSumHolder = sumManager.getThreadLocal();
        int currentSum = currentSumHolder.get();
        currentSum += event.getContent();
        currentSumHolder.set(currentSum);

        System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Current Sum: " + currentSum);
        return new Result("Thread ID: " + Thread.currentThread().getId() + ", Current Sum: " + currentSum);
    }
}