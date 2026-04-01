package dev.lydtech.dispatch.service;

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
    private KafkaTemplate kafkaProducerMock;

    @BeforeEach
    void setUp() {
        kafkaProducerMock = mock(KafkaTemplate.class);
        service = new DispatchService(kafkaProducerMock);
    }

    @Test
    void process_Success() throws Exception {
        when(kafkaProducerMock.send(anyString(), anyString(), any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));
        when(kafkaProducerMock.send(anyString(), anyString(), any(OrderDispatched.class))).thenReturn(mock(CompletableFuture.class));

        String key = randomUUID().toString();
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        service.process(key, testEvent);

        verify(kafkaProducerMock, times(1)).send(eq("dispatch.tracking"), eq(key), any(DispatchPreparing.class));
        verify(kafkaProducerMock, times(1)).send(eq("order.dispatched"), eq(key), any(OrderDispatched.class));
    }

    @Test
    void process_DispatchTrackingProducerThrowsException() {
        String key=randomUUID().toString();
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        doThrow(new RuntimeException("dispatch tracking producer failure"))
                .when(kafkaProducerMock)
                .send(eq("dispatch.tracking"), eq(key), any(DispatchPreparing.class));
        Exception exception = assertThrows(RuntimeException.class, () -> service.process(key,testEvent));

        verify(kafkaProducerMock, times(1))
                .send( eq("dispatch.tracking"), eq(key), any(DispatchPreparing.class));
        verifyNoMoreInteractions(kafkaProducerMock);
        assertThat(exception.getMessage(), equalTo("dispatch tracking producer failure"));

    }

    @Test
    void process_OrderDispatchedThrowsException() {
        String key=randomUUID().toString();
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        when(kafkaProducerMock.send(anyString(), anyString(), any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));
        doThrow(new RuntimeException("order dispatch producer failure")).when(kafkaProducerMock)
                .send(eq("order.dispatched"), eq(key), any(OrderDispatched.class));
        Exception exception=assertThrows(RuntimeException.class,()->service.process(key,testEvent));
        verify(kafkaProducerMock,times(1)).send(eq("dispatch.tracking"),eq(key), any(DispatchPreparing.class));
        verify(kafkaProducerMock,times(1)).send(eq("order.dispatched"), eq(key), any(OrderDispatched.class));

        assertThat(exception.getMessage(), equalTo("order dispatch producer failure"));

    }


}