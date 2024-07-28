package com.example.demo.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.model.Event;
import com.example.demo.model.JobSession;
import com.example.demo.model.Result;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
class EventProcessorService2Test {

    @Autowired
    private EventProcessorService eventProcessorService;

    @MockBean
    private JobSessionService jobSessionService;

    @SpyBean
    private EventFilterService eventFilterService;

    @SpyBean
    private EventStorageService eventStorageService;

    @SpyBean
    private EventOperatorService eventOperatorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    String ACTOR_ID_1 = "actor:1";
    String SESSION_ID_1 = "session:1";
    String EVENT_BOSS_KILL = "boss-kill";

    @Test
    void testProcessEvents() {
        // 이벤트 준비
        List<Event> events = IntStream.range(0, 1000)
            .mapToObj(eventTimestamp -> new Event(ACTOR_ID_1, EVENT_BOSS_KILL, eventTimestamp, 1))
            .collect(Collectors.toList());
        Flux<Event> eventFlux = Flux.fromIterable(events);

        // MOCK 처리
        when(jobSessionService.querySessions(ACTOR_ID_1))
            .thenReturn(Flux.just(new JobSession(ACTOR_ID_1, SESSION_ID_1, EVENT_BOSS_KILL, 100, 200))); // = 100
        when(eventStorageService.queryEvents(eq(ACTOR_ID_1), eq(EVENT_BOSS_KILL), anyLong(), anyLong()))
            .thenReturn(Flux.just(new Event(ACTOR_ID_1, EVENT_BOSS_KILL, 5L, 3))); // = 3

        // 코드 수행
        Flux<Result> resultFlux = eventProcessorService.processEvents(eventFlux).cache();

        // 검증
        StepVerifier.create(resultFlux.last())
            .expectNextMatches(result -> result.getResult().equals("103"))// = 100 + 3
            .verifyComplete();
        StepVerifier.create(resultFlux)
            .expectNextCount(100)
            .verifyComplete();
        verify(eventOperatorService, times(100)).operate(any(JobSession.class), any(Event.class));
        verify(eventStorageService, times(100 / 25)).saveBatch(any());
        verify(eventStorageService, times(1)).queryEvents(eq(ACTOR_ID_1), eq(EVENT_BOSS_KILL), eq(100L), eq(200L));
    }
}