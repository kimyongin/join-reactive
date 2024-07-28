package com.example.demo.service.sum;

import com.example.demo.model.Event;
import com.example.demo.model.Result;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class EventOperatorServiceSum {
    // ThreadLocal을 사용하여 각 스레드의 누적 합계를 저장
    private static final ThreadLocal<Integer> sumHolder = ThreadLocal.withInitial(() -> 0);

    public Mono<Result> operate(Event event) {
        return Mono.fromCallable(() -> {
                // 현재 스레드의 누적 합계 가져오기
                int currentSum = sumHolder.get();
                // 이벤트의 숫자를 현재 합계에 더하기
                currentSum += event.getContent();
                // ThreadLocal에 업데이트된 합계 저장
                sumHolder.set(currentSum);

                // 결과 로깅 및 반환
                System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Current Sum: " + currentSum);
                return new Result("Thread ID: " + Thread.currentThread().getId() + ", Current Sum: " + currentSum);
            })
            .subscribeOn(Schedulers.single()); // 모든 작업을 단일 스레드에서 실행
    }
}