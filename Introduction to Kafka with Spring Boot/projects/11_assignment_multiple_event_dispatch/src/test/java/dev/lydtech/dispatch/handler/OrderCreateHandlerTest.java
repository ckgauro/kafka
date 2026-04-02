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
    void setUp(){
        dispatchServiceMock=mock(DispatchService.class);
        orderCreateHandler=new OrderCreateHandler(dispatchServiceMock);
    }

    @Test
    void listenSuccess()throws Exception{
        String key=randomUUID().toString();
        OrderCreated testEvent= TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        orderCreateHandler.listen(0, key,testEvent);
        verify(dispatchServiceMock, times(1)).process(key, testEvent);
    }

    @Test
    public void listen_ServiceThrowsException() throws Exception{
        String key=randomUUID().toString();
        OrderCreated testEvent=TestEventData.buildOrderCreatedEvent(randomUUID(),randomUUID().toString());
        doThrow(new RuntimeException("Service failure")).when(dispatchServiceMock).process(key,testEvent);
        orderCreateHandler.listen(0,key,testEvent);
        verify(dispatchServiceMock,times(1)).process(key,testEvent);

    }

}