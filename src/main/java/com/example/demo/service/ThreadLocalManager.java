package com.example.demo.service;

@FunctionalInterface
public interface ThreadLocalManager<T> {
    ThreadLocal<T> getThreadLocal();
}