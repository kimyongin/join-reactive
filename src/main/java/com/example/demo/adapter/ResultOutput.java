package com.example.demo.adapter;

import com.example.demo.model.Result;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ResultOutput {

    public void publishResults(Flux<Result> results) {
        results.subscribe(result -> System.out.println("Processed Result: " + result.getResult()));
    }
}
