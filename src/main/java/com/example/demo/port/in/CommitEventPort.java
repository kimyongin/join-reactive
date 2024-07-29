package com.example.demo.port.in;

import com.example.demo.model.Event;
import reactor.core.publisher.Flux;

public interface CommitEventPort {
  Flux<Event> commitEvent(Flux<Event> events);
}
