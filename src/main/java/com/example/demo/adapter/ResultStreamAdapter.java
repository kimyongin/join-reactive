package com.example.demo.adapter;

import com.example.demo.model.Result;
import com.example.demo.port.out.PublishResultPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ResultStreamAdapter implements PublishResultPort {

  @Override
  public Mono<Result> publishResult(Result result) {
    return Mono.fromCallable(() -> {
      System.out.println("Processed Result: " + result.getResult());
      // 여기에 발행 로직 구현
      return result;
    });
  }
}
