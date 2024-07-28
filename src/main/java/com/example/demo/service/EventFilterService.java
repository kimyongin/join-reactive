package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;
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

    public Mono<Map<String/*eventType*/, Tuple2<Long, Long>/*start-end*/>> addItem(String actorId, String eventType, long start, long end) {
        return Mono.just(actorId)
            .publishOn(schedulerSelector.select(actorId))
            .map(s -> {
                System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Actor: " + actorId + ", Add EventType: " + eventType);
                Map<String, Map<String, Tuple2<Long, Long>>> filter = eventFilterTLS.getThreadLocal().get();
                Map<String, Tuple2<Long, Long>> eventTypeMap = filter.computeIfAbsent(actorId, k -> new HashMap<>());
                eventTypeMap.put(eventType, Tuples.of(start, end));
                return eventTypeMap;
            });
    }

    public Mono<Map<String, Tuple2<Long, Long>>> removeItem(String actorId, String eventType) {
        return Mono.just(actorId)
            .publishOn(schedulerSelector.select(actorId))
            .mapNotNull(s -> {
                System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Actor: " + actorId + ", Remove EventType: " + eventType);
                Map<String, Map<String, Tuple2<Long, Long>>> filter = eventFilterTLS.getThreadLocal().get();
                Map<String, Tuple2<Long, Long>> eventTypeMap = filter.get(actorId);
                if (eventTypeMap != null) {
                    eventTypeMap.remove(eventType);
                    if (eventTypeMap.isEmpty()) {
                        filter.remove(actorId);
                    }
                }
                return eventTypeMap;
            });
    }

    public Mono<Map<String, Tuple2<Long, Long>>> getFilter(String actorId) {
        return Mono.just(actorId)
            .publishOn(schedulerSelector.select(actorId))
            .mapNotNull(s -> eventFilterTLS.getThreadLocal().get().get(actorId));
    }
}
