package dev.lydtech.dispatch.handler;

import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.service.DispatchService;
import dev.lydtech.dispatch.util.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderCreateHandlerTest {
    private OrderCreateHandler orderCreateHandler;
    private DispatchService dispatchServiceMock;

    @BeforeEach
    void setup(){
        dispatchServiceMock=mock(DispatchService.class);
        orderCreateHandler=new OrderCreateHandler(dispatchServiceMock);
    }

    @Test
    void listen(){
        OrderCreated orderCreated= TestEventData.buildOrderCreatedEvent(randomUUID(),randomUUID().toString());
        orderCreateHandler.listen(orderCreated);
        verify(dispatchServiceMock,times(1)).process(orderCreated);
    }


}