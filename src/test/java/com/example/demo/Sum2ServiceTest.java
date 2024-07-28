package com.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.demo.model.Event;
import com.example.demo.service.EventStorageService;
import com.example.demo.service.sum2.EventOperatorServiceSum2;
import com.example.demo.service.sum2.EventProcessorServiceSum2;
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
class Sum2ServiceTest {

    @Autowired
    private EventProcessorServiceSum2 eventProcessorServiceSum2;

    @SpyBean
    private EventStorageService eventStorageService;

    @SpyBean
    private EventOperatorServiceSum2 eventOperatorServiceSum2;

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
        StepVerifier.create(eventProcessorServiceSum2.processEvents(Flux.fromIterable(events)))
            .expectNextCount(100)
            .verifyComplete();

        // Verification of mocks
        verify(eventOperatorServiceSum2, times(100)).operate(any(Event.class));
    }
}