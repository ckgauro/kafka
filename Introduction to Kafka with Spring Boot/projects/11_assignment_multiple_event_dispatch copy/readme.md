# OrderCreateHandler 
```java

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DispatchCompleted {
    UUID orderId;
    String dispatchedDate;
}

```
# DispatchCompleted — Summary Tables

## 1. Class Overview

| Item | Description |
|-----|-------------|
| Class Name | `DispatchCompleted` |
| Purpose | Represents an event/message indicating that an order dispatch is completed |
| Type | POJO (Plain Java Object) |
| Usage | Typically used in Kafka messaging or event-driven systems |

---

## 2. Fields

| Field Name | Type | Description |
|-----------|------|-------------|
| orderId | `UUID` | Unique identifier of the order |
| dispatchedDate | `String` | Date when the order was dispatched |

---

## 3. Lombok Annotations

| Annotation | Meaning | Benefit |
|-----------|--------|--------|
| `@Data` | Generates getters, setters, `toString()`, `equals()`, `hashCode()` | Reduces boilerplate code |
| `@Builder` | Enables builder pattern for object creation | Cleaner and flexible object creation |
| `@AllArgsConstructor` | Generates constructor with all fields | Easy initialization |
| `@NoArgsConstructor` | Generates default constructor | Required for serialization/deserialization |

---

## 4. Object Creation Methods

### ▸ Using Constructor

| Method | Example |
|-------|--------|
| All args constructor | `new DispatchCompleted(orderId, "2026-04-02")` |
| No args constructor | `new DispatchCompleted()` then set values |

---

### ▸ Using Builder Pattern

| Step | Example |
|-----|--------|
| Build object | `DispatchCompleted.builder().orderId(id).dispatchedDate("2026-04-02").build();` |

✔ Preferred in modern code  
✔ Improves readability  
✔ Avoids constructor confusion

---

## 5. Typical Usage in Kafka/Event Flow

| Step | Description |
|-----|-------------|
| Event Produced | When dispatch is completed |
| Message Sent | Sent to a Kafka topic (e.g., `order.dispatched`) |
| Event Consumed | Other services consume and process it |

---

## 6. Key Characteristics

| Feature | Explanation |
|--------|------------|
| Immutable-style usage | Often used via builder (though not strictly immutable) |
| Serializable | Can be converted to JSON for Kafka |
| Lightweight | Only contains required event data |
| Event-driven | Represents a domain event |

---

## 7. Example JSON Representation

```json
{
  "orderId": "123e4567-e89b-12d3-a456-426614174000",
  "dispatchedDate": "2026-04-02"
}
```

-----
# DispatchService

```java


@Slf4j
@RequiredArgsConstructor
@Service
public class DispatchService {

    private static final String DISPATCH_TRACKING_TOPIC="dispatch.tracking";
    private static final String ORDER_DISPATCHED_TOPIC="order.dispatched";
    private static final UUID APPLICATION_ID=randomUUID();

    private final KafkaTemplate<String,Object> kafkaProducer;

    public void process(String key,OrderCreated orderCreated) throws Exception{
        DispatchPreparing dispatchPreparing= DispatchPreparing.builder()
                .orderId(orderCreated.getOrderId())
                .build();
        kafkaProducer.send(DISPATCH_TRACKING_TOPIC, key,dispatchPreparing).get();

        OrderDispatched orderDispatched= OrderDispatched.builder()
                .orderId(orderCreated.getOrderId())
                .processedById(APPLICATION_ID)
                .notes("Dispatched: "+orderCreated.getItem())
                .build();
        kafkaProducer.send(ORDER_DISPATCHED_TOPIC,key,orderDispatched).get();

        DispatchCompleted dispatchCompleted= DispatchCompleted.builder()
                .orderId(orderCreated.getOrderId())
                .dispatchedDate(LocalDate.now().toString())
                .build();
        kafkaProducer.send(DISPATCH_TRACKING_TOPIC,key,dispatchCompleted).get();

        log.info("Sent messages: key: {}  - orderId:{} - processedById :{}", key, orderCreated.getOrderId(), APPLICATION_ID);


    }
}
```
# DispatchService — Summary Tables

## 1. Class Overview

| Item | Description |
|-----|-------------|
| Class Name | `DispatchService` |
| Annotation | `@Service` (Spring service layer component) |
| Purpose | Processes an order and sends multiple Kafka events |
| Responsibility | Orchestrates dispatch workflow using Kafka |

---

## 2. Annotations Used

| Annotation | Meaning | Benefit |
|-----------|--------|--------|
| `@Slf4j` | Adds logger (`log`) | Easy logging |
| `@RequiredArgsConstructor` | Generates constructor for final fields | Dependency injection |
| `@Service` | Marks as Spring service | Managed by Spring container |

---

## 3. Constants

| Constant | Value | Description |
|----------|------|-------------|
| `DISPATCH_TRACKING_TOPIC` | `"dispatch.tracking"` | Topic for tracking dispatch stages |
| `ORDER_DISPATCHED_TOPIC` | `"order.dispatched"` | Topic for final dispatch event |
| `APPLICATION_ID` | `randomUUID()` | Unique ID for this service instance |

---

## 4. Dependency

| Field | Type | Description |
|------|------|-------------|
| kafkaProducer | `KafkaTemplate<String, Object>` | Sends messages to Kafka |

---

## 5. Method Overview

| Method | Description |
|-------|-------------|
| `process(String key, OrderCreated orderCreated)` | Handles order processing and sends 3 Kafka events |

---

## 6. Event Flow (Step-by-Step)

| Step | Event | Topic | Description |
|-----|------|-------|-------------|
| 1 | `DispatchPreparing` | `dispatch.tracking` | Indicates dispatch started |
| 2 | `OrderDispatched` | `order.dispatched` | Confirms order dispatched |
| 3 | `DispatchCompleted` | `dispatch.tracking` | Marks dispatch completed |

---

## 7. Event Details

### ▸ DispatchPreparing

| Field | Value |
|------|------|
| orderId | From `orderCreated` |

---

### ▸ OrderDispatched

| Field | Value |
|------|------|
| orderId | From `orderCreated` |
| processedById | `APPLICATION_ID` |
| notes | `"Dispatched: " + item` |

---

### ▸ DispatchCompleted

| Field | Value |
|------|------|
| orderId | From `orderCreated` |
| dispatchedDate | Current date (`LocalDate.now()`) |

---

## 8. Kafka Send Behavior

| Feature | Explanation |
|--------|------------|
| `.send()` | Sends message to Kafka |
| `.get()` | Waits for acknowledgment (synchronous) |
| Key Usage | Ensures same partition ordering |

✔ Guarantees message delivery before next step  
✔ Maintains order of events

---

## 9. Logging

| Log Statement | Purpose |
|--------------|--------|
| `log.info(...)` | Tracks processed order details |

Logged Data:
- key
- orderId
- processedById

---

## 10. Processing Flow Diagram (Conceptual)

```bash
OrderCreated
↓
DispatchPreparing → dispatch.tracking
↓
OrderDispatched → order.dispatched
↓
DispatchCompleted → dispatch.tracking
```

---

## 11. Key Characteristics

| Feature | Explanation |
|--------|------------|
| Sequential Processing | Events sent in order |
| Synchronous Calls | Uses `.get()` to block until send completes |
| Event-Driven | Produces events for other services |
| Traceability | Multiple events track lifecycle |

---

## 12. Key Idea

| Concept | Explanation |
|--------|------------|
| Orchestration Service | Controls full dispatch workflow |
| Event Chain | Multiple events represent stages |
| Reliable Messaging | Ensures delivery using synchronous Kafka sends |



------

# Kafka Testing Guide — Consumer Group Behavior

---

## 📌 Overview

This guide demonstrates how a **single Kafka consumer group behaves** when:
- Producing messages
- Consuming messages
- Scaling consumers
- Inspecting consumer group state

---

## 🚀 1. Start Application Instances

Run your Spring Boot application.

### ▶️ Start First Instance
```bash
mvn spring-boot:run
```

**🖥️ Console Output**
```bash
dispatch.order.created.consumer: partitions assigned: []
```

✔ Indicates consumer started but no partitions assigned yet (or waiting for rebalance)

## 📥 2. Start Kafka Consumer (Output Topic)

### Run a console consumer to listen to processed events:
```bash
kafka-console-consumer \
--bootstrap-server kafka1:9092 \
--topic order.dispatched \
--property print.key=true \
--property key.separator=:
```


**✔ This will display messages produced by your application**

## 📤 3. Start Kafka Producer (Input Topic)

**Send test messages to trigger processing:**
```bash
kafka-console-producer \
--bootstrap-server kafka1:9092 \
--topic order.created \
--property parse.key=true \
--property key.separator=:
```

**▶️ Sample Message**

```bash
"1":{"orderId":"550e8400-e29b-41d4-a716-446655440001","item":"book-001"} 
```

## 🔄 4. Verify Application Processing


Check your Spring Boot console logs:

```bash
Sent messages: key: "1"  - orderId:550e8400-e29b-41d4-a716-446655440001 - processedById :09ffecfe-d792-499d-8060-ed3198a5663d
```

**✔ Confirms:**
- Message consumed from `order.created`
- Processed by `DispatchService`
- Produced to downstream topics

## 📊 5. List Consumer Groups

View all active consumer groups:
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

**🖥️ Output Example**
```bash
dispatch.order.created.consumer
console-consumer-41373
console-consumer-95726
dispatch.order.created.consumer2
console-consumer-13198
```

**✔ Shows:**

- Your application consumer group
- Console consumers (auto-generated groups)

## 🔍 6. Describe Consumer Group

Inspect partition assignment and offsets:

```bash
kafka-consumer-groups \
--bootstrap-server localhost:9092 \
--describe \
--group dispatch.order.created.consumer
```

**🖥️ Output Example**

```bash
GROUP                           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                                                     HOST            CLIENT-ID
dispatch.order.created.consumer order.created   0          46              46              0               consumer-dispatch.order.created.consumer-1-aa326c0a-d351-42ab-a40c-49a43610bf1b /192.168.65.1   consumer-dispatch.order.created.consumer-1

```

## 📌 7. Understanding Output (Summary Table)
| Field          | Meaning                     |
| -------------- | --------------------------- |
| GROUP          | Consumer group name         |
| TOPIC          | Topic being consumed        |
| PARTITION      | Partition number            |
| CURRENT-OFFSET | Last consumed message       |
| LOG-END-OFFSET | Latest message in partition |
| LAG            | Messages not yet consumed   |
| CONSUMER-ID    | Unique consumer instance    |
| HOST           | Consumer machine            |
| CLIENT-ID      | Kafka client identifier     |



## ✅ Key Observations
| Scenario           | Behavior                        |
| ------------------ | ------------------------------- |
| Single Consumer    | Consumes all partitions         |
| Multiple Consumers | Partitions distributed          |
| Same Key           | Goes to same partition          |
| Lag = 0            | Consumer is up-to-date          |
| Rebalance          | Happens when new consumer joins |


**🧠 Final Insight**

This setup demonstrates:
- ✔ End-to-end Kafka flow (produce → process → consume)
- ✔ Consumer group coordination
- ✔ Partition assignment and load balancing
- ✔ Real-time monitoring using Kafka CLI tools


-----

# Kafka Testing Guide — Multiple Partition and Rebalancing

---

## 📌 Objective

This test shows how Kafka behaves when:

- the number of partitions is increased
- multiple application instances run under the same consumer group
- Kafka automatically rebalances partitions when a new consumer joins or an existing consumer stops

---
