package com.example.demo.port.in;

import com.example.demo.model.Event;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public interface SubscribeEventPort {
  Flux<Event> subscribeEvent();
}
