package com.example.demo.service.sum2;

@FunctionalInterface
public interface ThreadLocalManager<T> {
    ThreadLocal<T> getThreadLocal();
}