package com.example.demo.model;

import lombok.Data;

@Data
public class Result {
    private String result;

    public Result(String result) {
        this.result = result;
    }
}
