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
    @Autowired
    private EventStorageService eventStorageService;

    public Mono<Result> operate(JobSession jobSession, Event event) {
        return Mono.just(jobSession)
            .publishOn(schedulerSelector.select(jobSession.getActorId()))
            .flatMap(js -> processEvent(js, event));
    }

    private Mono<Result> processEvent(JobSession jobSession, Event event) {
        // 중간집계값 조회
        Map<String, Integer> sumMap = eventAccumulator.getThreadLocal().get();
        Mono<Integer> sumMono = Mono.justOrEmpty(sumMap.get(event.getActorId()))
            .switchIfEmpty(Mono.defer(() -> getSumByEvents(jobSession, event))); // 없으면, 이벤트들을 조회하여 중간집계값을 계산

        // 중간집계값에 신규값을 누적
        return sumMono.map(sum -> {
            int currentSum = sum + event.getEventContent();
            sumMap.put(event.getActorId(), currentSum);
            return new Result(
                jobSession.getActorId(),
                jobSession.getSessionId(),
                String.valueOf(currentSum)
            );
        }).doOnNext(this::debugPrint);
    }

    private Mono<Integer> getSumByEvents(JobSession jobSession, Event event) {
        return eventStorageService.queryEvents(event.getActorId(), event.getEventType(), jobSession.getStart(), jobSession.getEnd())
            .map(Event::getEventContent)
            .reduce(0, Integer::sum);
    }

    private void debugPrint(Result result) {
        System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Actor: " + result.getActorId() + ", Sum: " + result.getResult());
    }
}