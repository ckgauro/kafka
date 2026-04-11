package dev.lydtech.dispatch.handler;

import dev.lydtech.dispatch.exception.NotRetryableException;
import dev.lydtech.dispatch.exception.RetryableException;
import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.service.DispatchService;
import dev.lydtech.dispatch.util.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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
        Exception exception=assertThrows(NotRetryableException.class,()->orderCreateHandler.listen(0,key,testEvent));

        assertThat(exception.getMessage(),equalTo("java.lang.RuntimeException: Service failure"));

       // orderCreateHandler.listen(0,key,testEvent);
        verify(dispatchServiceMock,times(1)).process(key,testEvent);

    }

    @Test
    public void testListen_ServiceThrowsRetryableException() throws Exception{
        String key=randomUUID().toString();
        OrderCreated orderCreated=TestEventData.buildOrderCreatedEvent(randomUUID(),randomUUID().toString());
        doThrow(new RetryableException("Service failure")).when(dispatchServiceMock).process(key,orderCreated);

        Exception exception=assertThrows(RuntimeException.class,()->orderCreateHandler.listen(0,key,orderCreated));
        assertThat(exception.getMessage(),equalTo("Service failure"));
        verify(dispatchServiceMock,times(1)).process(key,orderCreated);
    }



}