package com.example.demo.service;

import static org.mockito.ArgumentMatchers.any;
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
class EventProcessorServiceTest {

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
    String ACTOR_ID_2 = "actor:2";
    String ACTOR_ID_3 = "actor:3";
    String ACTOR_ID_4 = "actor:4";
    String SESSION_ID_1 = "session:1";
    String SESSION_ID_2 = "session:2";
    String EVENT_BOSS_KILL = "boss-kill";
    String EVENT_MONSTER_KILL = "monster-kill";

    @Test
    void testProcessEvents() {
        // 이벤트 준비
        List<Event> events = IntStream.range(0, 1000)
            .mapToObj(value -> {
                if (value % 4 == 0) {
                    return new Event(ACTOR_ID_1, EVENT_BOSS_KILL, 1, 1);
                } else if(value % 4 == 1) {
                    return new Event(ACTOR_ID_2, EVENT_MONSTER_KILL, 1, 1);
                } else if(value % 4 == 2) {
                    return new Event(ACTOR_ID_3, EVENT_MONSTER_KILL, 1, 1);
                } else {
                    return new Event(ACTOR_ID_4, EVENT_MONSTER_KILL, 1, 1);
                }
            })
            .collect(Collectors.toList());
        Flux<Event> eventFlux = Flux.fromIterable(events);

        // 작업 세션 MOCK 처리
        Mockito.when(jobSessionService.querySessions(ACTOR_ID_1))
            .thenReturn(Flux.just(new JobSession(ACTOR_ID_1, SESSION_ID_1, EVENT_BOSS_KILL)));
        Mockito.when(jobSessionService.querySessions(ACTOR_ID_2))
            .thenReturn(Flux.just(new JobSession(ACTOR_ID_2, SESSION_ID_1, EVENT_MONSTER_KILL)));
        Mockito.when(jobSessionService.querySessions(ACTOR_ID_3))
            .thenReturn(Flux.empty());
        Mockito.when(jobSessionService.querySessions(ACTOR_ID_4))
            .thenReturn(Flux.empty());

        // 프로세싱
        Flux<Result> resultFlux = eventProcessorService.processEvents(eventFlux).cache();

        // 검증#1
        Flux<GroupedFlux<String, Result>> groupedResults = resultFlux.groupBy(Result::getActorId);
        Flux<Result> lastResults = groupedResults.flatMap(group ->
            group.reduce((previous, current) -> current) // 각 그룹의 마지막 Result 객체를 추출
        );
        StepVerifier.create(lastResults)
            .expectNextMatches(result -> result.getResult().equals("250"))
            .expectNextMatches(result -> result.getResult().equals("250"))
            .verifyComplete();

        // 검증#2
        StepVerifier.create(resultFlux)
            .expectNextCount(500)
            .verifyComplete();

        // 검증#3
        verify(eventFilterService, times(1)).addItem(eq(ACTOR_ID_1), eq(EVENT_BOSS_KILL));
        verify(eventFilterService, times(1)).addItem(eq(ACTOR_ID_2), eq(EVENT_MONSTER_KILL));
        verify(eventFilterService, never()).addItem(eq(ACTOR_ID_4), any());
        verify(eventFilterService, never()).addItem(eq(ACTOR_ID_3), any());
        verify(eventOperatorService, times(500)).operate(any(JobSession.class), any(Event.class));
        verify(eventStorageService, times(500 / 25)).saveBatch(any());

    }
}