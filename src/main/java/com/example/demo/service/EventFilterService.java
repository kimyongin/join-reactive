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

    // 필터 처리
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

    // 액터의 작업세션들을 조회해서 필터 등록
    private Mono<Map<String, Tuple2<Long, Long>>> createFilter(Event event) {
        return jobSessionService.querySessions(event.getActorId())
            .flatMap(this::addItem)
            .collectList()
            .filter(list -> !list.isEmpty())
            .map(list -> list.get(list.size() - 1));
    }


    // 로컬 캐시에 필터 등록
    private Mono<Map<String/*eventType*/, Tuple2<Long, Long>/*start-end*/>> addItem(JobSession jobSession) {
        Map<String, Map<String, Tuple2<Long, Long>>> filter = eventFilterTLS.getThreadLocal().get();
        Map<String, Tuple2<Long, Long>> eventTypeMap = filter.computeIfAbsent(jobSession.getActorId(), k -> new HashMap<>());
        eventTypeMap.put(jobSession.getEventType(), Tuples.of(jobSession.getStart(), jobSession.getEnd()));
        return Mono.just(eventTypeMap).doOnNext(s -> debugPrint(jobSession));
    }

    // 로컬 캐시에 필터 제거
    private Mono<Map<String, Tuple2<Long, Long>>> removeItem(JobSession jobSession) {
        Map<String, Map<String, Tuple2<Long, Long>>> filter = eventFilterTLS.getThreadLocal().get();
        Map<String, Tuple2<Long, Long>> eventTypeMap = filter.get(jobSession.getActorId());
        if (eventTypeMap != null) {
            eventTypeMap.remove(jobSession.getEventType());
            if (eventTypeMap.isEmpty()) {
                filter.remove(jobSession.getActorId());
            }
        }
        return Mono.justOrEmpty(eventTypeMap).doOnNext(s -> debugPrint(jobSession));
    }

    private void debugPrint(JobSession jobSession) {
        System.out.println("Thread ID: " + Thread.currentThread().getId() + ", " + "Actor: " + jobSession.getActorId() + ", " + "Add EventType: " + jobSession.getEventType());
    }
}
