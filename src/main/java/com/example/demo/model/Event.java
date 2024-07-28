package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Event {
    private String actorId;
    private String eventType;
    private long eventTimestamp;
    private int eventContent;
}
