package com.idempotent.idemotent.service;

import com.idempotent.idemotent.entity.ProcessedEventEntity;
import com.idempotent.idemotent.message.OrderCreated;
import com.idempotent.idemotent.message.OrderDispatched;
import com.idempotent.idemotent.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.util.UUID.randomUUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class DispatchService {

    private final ProcessedEventRepository processedEventRepository;

    private static final String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    private static final UUID APPLICATION_ID = randomUUID();

    private final KafkaTemplate<String, Object> kafkaProducer;

    public void process(String key, OrderCreated orderCreated) throws Exception {

        ProcessedEventEntity processedEventEntity = processedEventRepository.findByMessageId(key);
        if (processedEventEntity != null) {
            log.info("Found a duplicate message id: {}", processedEventEntity.getMessageId());
            return;
        }
        try {
            processedEventRepository.save(ProcessedEventEntity.builder()
                    .messageId(key)
                    .orderId(orderCreated.getOrderId().toString())
                    .build());
        } catch (DataIntegrityViolationException ex) {
            // throw new NotRetryableException(ex);
            throw new RuntimeException("DataIntegrityViolationException error ");
        }

        OrderDispatched orderDispatched = OrderDispatched.builder()
                .orderId(orderCreated.getOrderId())
                .processedById(APPLICATION_ID)
                .notes("Dispatched: " + orderCreated.getItem())
                .build();
        kafkaProducer.send(ORDER_DISPATCHED_TOPIC, key, orderDispatched).get();
        log.info("Sent messages: key: {}  - orderId:{} - processedById :{}", key, orderCreated.getOrderId(), APPLICATION_ID);
        // Save a unique message id in a database table


    }
}