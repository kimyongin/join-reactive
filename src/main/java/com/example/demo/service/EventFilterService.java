package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.model.JobSession;
import java.util.HashMap;
import java.util.Map;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
public class EventFilterService {

    @Autowired
    private SchedulerSelector schedulerSelector;
    @Autowired
    private ThreadLocalManager<Map<String/*actorId*/, Map<String/*eventType*/, Tuple2<Long, Long>/*start-end*/>>> eventFilterTLS;
    @Autowired
    private JobSessionService jobSessionService;

    Mono<Map<String/*eventType*/, Tuple2<Long, Long>/*start-end*/>> addItem(JobSession jobSession) {
        return Mono.just(jobSession.getActorId())
            .publishOn(schedulerSelector.select(jobSession.getActorId()))
            .map(s -> {
                System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Actor: " + jobSession.getActorId() + ", Add EventType: " + jobSession.getEventType());
                Map<String, Map<String, Tuple2<Long, Long>>> filter = eventFilterTLS.getThreadLocal().get();
                Map<String, Tuple2<Long, Long>> eventTypeMap = filter.computeIfAbsent(jobSession.getActorId(), k -> new HashMap<>());
                eventTypeMap.put(jobSession.getEventType(), Tuples.of(jobSession.getStart(), jobSession.getEnd()));
                return eventTypeMap;
            });
    }

    Mono<Map<String, Tuple2<Long, Long>>> removeItem(JobSession jobSession) {
        return Mono.just(jobSession.getActorId())
            .publishOn(schedulerSelector.select(jobSession.getActorId()))
            .mapNotNull(s -> {
                System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Actor: " + jobSession.getActorId() + ", Remove EventType: " + jobSession.getEventType());
                Map<String, Map<String, Tuple2<Long, Long>>> filter = eventFilterTLS.getThreadLocal().get();
                Map<String, Tuple2<Long, Long>> eventTypeMap = filter.get(jobSession.getActorId());
                if (eventTypeMap != null) {
                    eventTypeMap.remove(jobSession.getEventType());
                    if (eventTypeMap.isEmpty()) {
                        filter.remove(jobSession.getActorId());
                    }
                }
                return eventTypeMap;
            });
    }

    Mono<Map<String, Tuple2<Long, Long>>> createFilter(Event event) {
        return jobSessionService.querySessions(event.getActorId())
            .flatMap(this::addItem)
            .collectList()
            .filter(list -> !list.isEmpty())
            .map(list -> list.get(list.size() - 1));
    }

    public Publisher<Boolean> filter(Event event) {
        return Mono.just(event.getActorId())
            .publishOn(schedulerSelector.select(event.getActorId()))
            .mapNotNull(s -> eventFilterTLS.getThreadLocal().get().get(event.getActorId()))
            .switchIfEmpty(createFilter(event))
            .map(filter -> {
                Tuple2<Long, Long> period = filter.get(event.getEventType());
                return period.getT1() <= event.getEventTimestamp() && event.getEventTimestamp() < period.getT2();
            });
    }
}
