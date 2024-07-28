package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JobSession {
    String actorId;
    String sessionId;
    String eventType;
    long start;
    long end;

}
