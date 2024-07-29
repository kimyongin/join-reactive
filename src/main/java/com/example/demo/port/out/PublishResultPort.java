package com.example.demo.port.out;

import com.example.demo.model.Result;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface PublishResultPort {
  Mono<Result> publishResult(Result result);
}
