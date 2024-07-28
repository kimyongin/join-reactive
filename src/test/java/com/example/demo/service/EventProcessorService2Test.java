package com.example.demo.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.demo.model.Event;
import com.example.demo.model.JobSession;
import com.example.demo.model.Result;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
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
            .mapToObj(value -> new Event(ACTOR_ID_1, EVENT_BOSS_KILL, value, 1))
            .collect(Collectors.toList());
        Flux<Event> eventFlux = Flux.fromIterable(events);

        // 작업 세션 MOCK 처리
        Mockito.when(jobSessionService.querySessions(ACTOR_ID_1))
            .thenReturn(Flux.just(new JobSession(ACTOR_ID_1, SESSION_ID_1, EVENT_BOSS_KILL, 100, 200)));

        // 프로세싱
        Flux<Result> resultFlux = eventProcessorService.processEvents(eventFlux).cache();

        // 검증#1
        Flux<GroupedFlux<String, Result>> groupedResults = resultFlux.groupBy(Result::getActorId);
        Flux<Result> lastResults = groupedResults.flatMap(group ->
            group.reduce((previous, current) -> current) // 각 그룹의 마지막 Result 객체를 추출
        );
        StepVerifier.create(lastResults)
            .expectNextMatches(result -> result.getResult().equals("100"))
            .verifyComplete();

        // 검증#2
        StepVerifier.create(resultFlux)
            .expectNextCount(100)
            .verifyComplete();

        // 검증#3
        verify(eventFilterService, times(1)).addItem(
            argThat(jobSession -> jobSession.getActorId().equals(ACTOR_ID_1) &&
                jobSession.getSessionId().equals(SESSION_ID_1) &&
                jobSession.getEventType().equals(EVENT_BOSS_KILL) &&
                jobSession.getStart() == 100 &&
                jobSession.getEnd() == 200)
        );
        verify(eventOperatorService, times(100)).operate(any(JobSession.class), any(Event.class));
        verify(eventStorageService, times(100 / 25)).saveBatch(any());

    }
}