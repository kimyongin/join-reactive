package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.model.JobSession;
import com.example.demo.model.Result;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EventOperatorService {
    @Autowired
    private SchedulerSelector schedulerSelector;
    @Autowired
    private ThreadLocalManager<Map<String/*actorId*/, Integer/*sum*/>> eventAccumulator;

    public Mono<Result> operate(JobSession jobSession, Event event) {
        return Mono.just(jobSession)
            .publishOn(schedulerSelector.select(jobSession.getActorId()))
            .map(ignore -> processEvent(jobSession, event));
    }

    private Result processEvent(JobSession jobSession, Event event) {
        ThreadLocal<Map<String, Integer>> actorSum = eventAccumulator.getThreadLocal();
        Integer currentSum = actorSum.get().compute(event.getActorId(), (actorId, sum) -> {
            if (sum == null)
                sum = 0;
            return sum + event.getEventContent();
        });

        System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Actor: " + jobSession.getActorId() + ", Sum: " + currentSum);
        return new Result(
            jobSession.getActorId(),
            jobSession.getSessionId(),
            currentSum.toString()
        );
    }
}