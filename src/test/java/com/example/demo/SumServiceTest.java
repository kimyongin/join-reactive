package com.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.demo.model.Event;
import com.example.demo.service.sum.EventOperatorServiceSum;
import com.example.demo.service.sum.EventProcessorServiceSum;
import com.example.demo.service.EventStorageService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@SpringBootTest
class SumServiceTest {

    @Autowired
    private EventProcessorServiceSum eventProcessorServiceSum;

    @SpyBean
    private EventStorageService eventStorageService;

    @SpyBean
    private EventOperatorServiceSum eventOperatorServiceSum;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessEvents() {
        List<Event> events = IntStream.range(1, 101)
            .mapToObj(Event::new)
            .collect(Collectors.toList());

        // Processing and verifying
        StepVerifier.create(eventProcessorServiceSum.processEvents(Flux.fromIterable(events)))
            .expectNextCount(100)
            .verifyComplete();

        // Verification of mocks
        verify(eventOperatorServiceSum, times(100)).operate(any(Event.class));
    }
}