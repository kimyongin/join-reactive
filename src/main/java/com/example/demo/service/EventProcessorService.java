package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.model.Result;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
public class EventProcessorService {

    @Autowired
    private JobSessionService jobSessionService;
    @Autowired
    private EventFilterService eventFilterService;
    @Autowired
    private EventOperatorService operatorService;
    @Autowired
    private EventStorageService storageService;

    public Flux<Result> processEvents(Flux<Event> events) {
        // 이벤트 필터
        Flux<Event> filteredEvents = events.filterWhen(event -> eventFilterService.getFilter(event.getActorId())
            .switchIfEmpty(createFilter(event)) // 없으면 필터 생성
            .map(filter -> {
                Tuple2<Long, Long> period = filter.get(event.getEventType());
                return period.getT1() <= event.getEventTimestamp() && event.getEventTimestamp() < period.getT2();
            })
        );

        // 이벤트 저장
        Flux<Event> savedEvents = storageService.saveEvents(filteredEvents)
            .flatMap(Flux::fromIterable);

        // 이벤트 연산
        Flux<Result> results = savedEvents.flatMap(
            event -> jobSessionService.querySessions(event.getActorId())
                .filter(jobSession -> jobSession.getEventType().equals(event.getEventType()))
                .flatMap(jobSession -> operatorService.operate(jobSession, event)));

        return results;
    }

    Mono<Map<String, Tuple2<Long, Long>>> createFilter(Event event) {
        return jobSessionService.querySessions(event.getActorId())
            .flatMap(jobSession -> eventFilterService.addItem(jobSession.getActorId(), jobSession.getEventType(), jobSession.getStart(), jobSession.getEnd()))
            .collectList()
            .filter(list -> !list.isEmpty())
            .map(list -> list.get(list.size() - 1));
    }
}