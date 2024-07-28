package com.example.demo.service;

import com.example.demo.model.Event;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EventStorageService {
    private static final int BATCH_SIZE = 25;
    private static final Duration MAX_BATCH_DURATION = Duration.ofSeconds(1);

    public Flux<Event> saveEvents(Flux<Event> events) {
        return events.bufferTimeout(BATCH_SIZE, MAX_BATCH_DURATION)  // 25개의 이벤트를 모으거나 최대 1초 동안 기다림
            .flatMap(this::saveBatch).flatMap(Flux::fromIterable);  // 모인 배치를 저장
    }

    Mono<List<Event>> saveBatch(List<Event> batch) {
        return Mono.fromCallable(() -> {
            System.out.println("Saving batch of " + batch.size() + " events...");
            // 여기에 배치 저장 로직 구현 (예: 데이터베이스 배치 쓰기)
            return batch;
        });
    }

    public Flux<Event> queryEvents(String actorId, String eventType, long start, long end) {
        return Flux.empty(); // mock
    }
}
