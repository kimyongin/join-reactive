package com.example.demo.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EventFilterService {

    @Autowired
    private SchedulerSelector schedulerSelector;
    @Autowired
    private ThreadLocalManager<Map<String/*actorId*/, Set<String/*eventType*/>>> eventFilterTLS;

    public Mono<Set<String>> addItem(String actorId, String eventType) {
        return Mono.just(actorId)
            .publishOn(schedulerSelector.select(actorId))
            .map(s -> {
                System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Actor: " + actorId + ", Add EventType: " + eventType);
                Set<String> eventTypes = eventFilterTLS.getThreadLocal().get()
                    .computeIfAbsent(actorId, k -> new HashSet<>());
                eventTypes.add(eventType);
                return eventTypes;
            });
    }

    public Mono<Set<String>> removeItem(String actorId, String eventType) {
        return Mono.just(actorId)
            .publishOn(schedulerSelector.select(actorId))
            .mapNotNull(s -> {
                Set<String> eventTypes = eventFilterTLS.getThreadLocal().get().get(actorId);
                if (eventTypes != null) {
                    System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Actor: " + actorId + ", Remove EventType: " + eventType);
                    eventTypes.remove(eventType);
                    if (eventTypes.isEmpty()) {
                        eventFilterTLS.getThreadLocal().get().remove(actorId);
                    }
                }
                return eventTypes;
            });
    }

    public Mono<Set<String>> getFilter(String actorId) {
        return Mono.just(actorId)
            .publishOn(schedulerSelector.select(actorId))
            .mapNotNull(s -> eventFilterTLS.getThreadLocal().get().get(actorId));
    }
}
