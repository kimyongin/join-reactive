package com.example.demo.adapter;

import com.example.demo.model.Event;
import com.example.demo.port.in.CommitEventPort;
import com.example.demo.port.in.SubscribeEventPort;
import reactor.core.publisher.Flux;

public class EventStreamAdapter implements CommitEventPort, SubscribeEventPort {

  @Override
  public Flux<Event> commitEvent(Flux<Event> events) {
    return Flux.empty(); // mock
  }

  @Override
  public Flux<Event> subscribeEvent() {
    return Flux.empty(); // mock
  }
}
