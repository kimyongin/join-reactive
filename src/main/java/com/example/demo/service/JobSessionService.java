package com.example.demo.service;

import com.example.demo.model.JobSession;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class JobSessionService {

    public Flux<JobSession> querySessions(String actorId) {
        return Flux.empty(); // mock
    }
}
