package com.example.demo.service.pass;

import com.example.demo.model.Event;
import com.example.demo.model.Result;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EventOperatorServicePass {
    public Mono<Result> operate(Event event) {
        return Mono.fromCallable(() -> {
            System.out.println(event.getContent());
            return new Result("Thread ID: " + Thread.currentThread().getId() + ", Current Pass: " + event.getContent());
        });
    }
}