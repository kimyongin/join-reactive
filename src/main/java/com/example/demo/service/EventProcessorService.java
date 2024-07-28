package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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
        return events
            .filterWhen(event -> eventFilterService.filter(event))  // 이벤트 필터
            .transform(filteredEvents -> storageService.saveEvents(filteredEvents)) // 이벤트 저장
            .flatMap(event ->
                jobSessionService.querySessions(event.getActorId()) // 작업세션 조회
                .filter(jobSession -> jobSession.getEventType().equals(event.getEventType())) // 작업세션 필터
                .flatMap(jobSession -> operatorService.operate(jobSession, event))); // 작업세션 수행
    }
}