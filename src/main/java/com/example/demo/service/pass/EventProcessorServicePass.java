package com.example.demo.service.pass;

import com.example.demo.model.Event;
import com.example.demo.model.Result;
import com.example.demo.service.EventStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class EventProcessorServicePass {

    @Autowired
    private EventOperatorServicePass operatorService;
    @Autowired
    private EventStorageService storageService;

    public Flux<Result> processEvents(Flux<Event> events) {
        return storageService.saveEvents(events)
            .flatMap(Flux::fromIterable)
            .flatMap(operatorService::operate);
    }
}