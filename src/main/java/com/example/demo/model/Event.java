package com.example.demo.model;

import lombok.Data;

@Data
public class Event {
    private int content;

    public Event(int content) {
        this.content = content;
    }
}
