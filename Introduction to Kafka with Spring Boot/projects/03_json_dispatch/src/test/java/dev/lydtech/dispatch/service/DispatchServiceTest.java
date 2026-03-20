package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.util.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

class DispatchServiceTest {
    private DispatchService dispatchService;

    @BeforeEach
    void  setUp(){
        dispatchService=new DispatchService();
    }

    @Test
    void process(){
        OrderCreated orderCreated= TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        dispatchService.process(orderCreated);
    }

}