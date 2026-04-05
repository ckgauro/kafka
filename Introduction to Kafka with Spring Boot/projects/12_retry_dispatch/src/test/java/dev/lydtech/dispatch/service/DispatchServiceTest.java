package dev.lydtech.dispatch.service;

import dev.lydtech.dispatch.client.StockServiceClient;
import dev.lydtech.dispatch.message.DispatchCompleted;
import dev.lydtech.dispatch.message.DispatchPreparing;
import dev.lydtech.dispatch.message.OrderCreated;
import dev.lydtech.dispatch.message.OrderDispatched;
import dev.lydtech.dispatch.util.TestEventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DispatchServiceTest {

    private DispatchService service;
    private StockServiceClient stockServiceClientMock;
    private KafkaTemplate kafkaProducerMock;


    @BeforeEach
    void setUp() {
        kafkaProducerMock = mock(KafkaTemplate.class);
        stockServiceClientMock=mock(StockServiceClient.class);
        service = new DispatchService(kafkaProducerMock,stockServiceClientMock);
    }

    @Test
    void process_Success() throws Exception {
        when(kafkaProducerMock.send(anyString(), anyString(), any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));
        when(kafkaProducerMock.send(anyString(), anyString(), any(OrderDispatched.class))).thenReturn(mock(CompletableFuture.class));
        when(kafkaProducerMock.send(anyString(),anyString(),any(DispatchCompleted.class))).thenReturn(mock(CompletableFuture.class));
        when(stockServiceClientMock.checkAvailability(anyString())).thenReturn("true");



        String key = randomUUID().toString();
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        service.process(key, testEvent);

        verify(kafkaProducerMock, times(1)).send(eq("dispatch.tracking"), eq(key), any(DispatchPreparing.class));
        verify(kafkaProducerMock, times(1)).send(eq("order.dispatched"), eq(key), any(OrderDispatched.class));
        verify(kafkaProducerMock, times(1)).send(eq("dispatch.tracking"), eq(key), any(DispatchCompleted.class));
        verify(stockServiceClientMock,times(1)).checkAvailability(testEvent.getItem());

    }

    @Test
    public void testProcess_StockUnavailable() throws Exception {
        when(stockServiceClientMock.checkAvailability(anyString())).thenReturn("false");
        String key= randomUUID().toString();
        OrderCreated testEvent=TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        service.process(key,testEvent);
        verifyNoMoreInteractions(kafkaProducerMock);
        verify(stockServiceClientMock, times(1)).checkAvailability(testEvent.getItem());

    }


    @Test
    void process_DispatchTrackingProducerThrowsException() {
        String key=randomUUID().toString();
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        when(stockServiceClientMock.checkAvailability(anyString())).thenReturn("true");


        doThrow(new RuntimeException("dispatch tracking producer failure"))
                .when(kafkaProducerMock)
                .send(eq("dispatch.tracking"), eq(key), any(DispatchPreparing.class));
        Exception exception = assertThrows(RuntimeException.class, () -> service.process(key,testEvent));

        verify(kafkaProducerMock, times(1))
                .send( eq("dispatch.tracking"), eq(key), any(DispatchPreparing.class));
        verifyNoMoreInteractions(kafkaProducerMock);
        verify(stockServiceClientMock,times(1)).checkAvailability(testEvent.getItem());
        assertThat(exception.getMessage(), equalTo("dispatch tracking producer failure"));

    }

    @Test
    void process_OrderDispatchedThrowsException() {
        String key=randomUUID().toString();
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        when(kafkaProducerMock.send(anyString(), anyString(), any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));
        when(stockServiceClientMock.checkAvailability(anyString())).thenReturn("true");

        doThrow(new RuntimeException("order dispatch producer failure")).when(kafkaProducerMock)
                .send(eq("order.dispatched"), eq(key), any(OrderDispatched.class));
        Exception exception=assertThrows(RuntimeException.class,()->service.process(key,testEvent));

        verify(kafkaProducerMock,times(1)).send(eq("dispatch.tracking"),eq(key), any(DispatchPreparing.class));
        verify(kafkaProducerMock,times(1)).send(eq("order.dispatched"), eq(key), any(OrderDispatched.class));

        verifyNoMoreInteractions(kafkaProducerMock);
        verify(stockServiceClientMock,times(1)).checkAvailability(testEvent.getItem());
        assertThat(exception.getMessage(), equalTo("order dispatch producer failure"));

    }

    @Test
    void process_SecondDispatchedProducerThrowException(){
        String key=randomUUID().toString();
        OrderCreated testEvent=TestEventData.buildOrderCreatedEvent(randomUUID(),randomUUID().toString());
        when(kafkaProducerMock.send(anyString(),anyString(), any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));
        when(kafkaProducerMock.send(anyString(),anyString(),any(OrderDispatched.class))).thenReturn(mock(CompletableFuture.class));
        when(stockServiceClientMock.checkAvailability(anyString())).thenReturn("true");


        doThrow(new RuntimeException("Dispatch completed producer failure")). when(kafkaProducerMock)
                .send(anyString(),anyString(), any(DispatchCompleted.class));
        Exception exception=assertThrows(RuntimeException.class,()->service.process(key, testEvent));

        verify(kafkaProducerMock,times(1)).send(eq("dispatch.tracking"),eq(key),any(DispatchPreparing.class));
        verify(kafkaProducerMock,times(1)).send(eq("order.dispatched"),eq(key), any(OrderDispatched.class));
        verify(kafkaProducerMock,times(1)).send(eq("dispatch.tracking"),eq(key),any(DispatchCompleted.class));
        verify(stockServiceClientMock,times(1)).checkAvailability(testEvent.getItem());


        assertThat(exception.getMessage(),equalTo("Dispatch completed producer failure"));
    }

    @Test
    public void testProcess_StockServiceClient_ThrowsException(){
        String key=randomUUID().toString();
        OrderCreated testEvent=TestEventData.buildOrderCreatedEvent(randomUUID(),randomUUID().toString());

        doThrow(new RuntimeException("stock service client failure")).when(stockServiceClientMock).checkAvailability(testEvent.getItem());
        Exception exception=assertThrows(RuntimeException.class,()->service.process(key,testEvent));
        assertThat(exception.getMessage(),equalTo("stock service client failure"));

        verifyNoInteractions(kafkaProducerMock);
        verify(stockServiceClientMock,times(1)).checkAvailability(testEvent.getItem());


    }



}