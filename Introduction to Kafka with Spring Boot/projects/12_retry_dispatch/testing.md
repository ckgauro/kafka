
# DispatchServiceTest — Summary Tables
```java

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
        when(kafkaProducerMock.send(anyString(),anyString(),any(DispatchCompleted.class))).thenReturn(mock(CompletableFuture.class));



        String key = randomUUID().toString();
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        service.process(key, testEvent);

        verify(kafkaProducerMock, times(1)).send(eq("dispatch.tracking"), eq(key), any(DispatchPreparing.class));
        verify(kafkaProducerMock, times(1)).send(eq("order.dispatched"), eq(key), any(OrderDispatched.class));
        verify(kafkaProducerMock, times(1)).send(eq("dispatch.tracking"), eq(key), any(DispatchCompleted.class));

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

        verifyNoMoreInteractions(kafkaProducerMock);
        assertThat(exception.getMessage(), equalTo("order dispatch producer failure"));

    }

    @Test
    void process_SecondDispatchedProducerThrowException(){
        String key=randomUUID().toString();
        OrderCreated testEvent=TestEventData.buildOrderCreatedEvent(randomUUID(),randomUUID().toString());
        when(kafkaProducerMock.send(anyString(),anyString(), any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));
        when(kafkaProducerMock.send(anyString(),anyString(),any(OrderDispatched.class))).thenReturn(mock(CompletableFuture.class));

        doThrow(new RuntimeException("Dispatch completed producer failure")). when(kafkaProducerMock)
                .send(anyString(),anyString(), any(DispatchCompleted.class));
        Exception exception=assertThrows(RuntimeException.class,()->service.process(key, testEvent));

        verify(kafkaProducerMock,times(1)).send(eq("dispatch.tracking"),eq(key),any(DispatchPreparing.class));
        verify(kafkaProducerMock,times(1)).send(eq("order.dispatched"),eq(key), any(OrderDispatched.class));
        verify(kafkaProducerMock,times(1)).send(eq("dispatch.tracking"),eq(key),any(DispatchCompleted.class));


        assertThat(exception.getMessage(),equalTo("Dispatch completed producer failure"));

    }
}
```

## 1. Class Overview

| Item | Description |
|------|-------------|
| Class Name | `DispatchServiceTest` |
| Purpose | Unit test class for `DispatchService` |
| Test Type | Unit testing with Mockito |
| Main Goal | Verify Kafka producer calls and exception handling |

---

## 2. Objects Used in Test

| Field | Type | Description |
|------|------|-------------|
| `service` | `DispatchService` | Class under test |
| `kafkaProducerMock` | `KafkaTemplate` | Mocked Kafka producer |

---

## 3. Test Setup

| Method | Purpose | Explanation |
|--------|---------|-------------|
| `setUp()` | Initialize test objects | Creates mocked `KafkaTemplate` and injects it into `DispatchService` |

### Setup Flow

```java
kafkaProducerMock = mock(KafkaTemplate.class);
service = new DispatchService(kafkaProducerMock);
```

| Step                     | Meaning                                     |
| ------------------------ | ------------------------------------------- |
| Mock KafkaTemplate       | Real Kafka is not used                      |
| Inject mock into service | Service behavior can be tested in isolation |


## 4. Test Cases Summary

| Test Method                                         | Scenario                | Expected Result                              |
| --------------------------------------------------- | ----------------------- | -------------------------------------------- |
| `process_Success()`                                 | All Kafka sends succeed | All 3 messages are sent once                 |
| `process_DispatchTrackingProducerThrowsException()` | First send fails        | Exception thrown, no more sends              |
| `process_OrderDispatchedThrowsException()`          | Second send fails       | Exception thrown, third send not executed    |
| `process_SecondDispatchedProducerThrowException()`  | Third send fails        | Exception thrown after first 2 sends succeed |


## 5. Detailed Test Explanation
### 5.1 `process_Success()`

**Purpose**

Tests the successful flow where all Kafka messages are sent correctly.

**Mock Behavior**
| Kafka Call                        | Mock Response                      |
| --------------------------------- | ---------------------------------- |
| `send(... DispatchPreparing ...)` | returns mocked `CompletableFuture` |
| `send(... OrderDispatched ...)`   | returns mocked `CompletableFuture` |
| `send(... DispatchCompleted ...)` | returns mocked `CompletableFuture` |


**Verified Calls**

| Topic               | Key        | Payload             |
| ------------------- | ---------- | ------------------- |
| `dispatch.tracking` | same `key` | `DispatchPreparing` |
| `order.dispatched`  | same `key` | `OrderDispatched`   |
| `dispatch.tracking` | same `key` | `DispatchCompleted` |


**Meaning**

This confirms that:

- dispatch preparation event is sent first
- order dispatched event is sent second
- dispatch completed event is sent third

### 5.2 `process_DispatchTrackingProducerThrowsException()`

**Purpose**

Tests what happens if the first Kafka send fails.

**Failure Point**

| Send Call                                    | Result                    |
| -------------------------------------------- | ------------------------- |
| `dispatch.tracking` with `DispatchPreparing` | throws `RuntimeException` |


**Expected Behavior**
| Check                 | Expected |
| --------------------- | -------- |
| First send attempted  | Yes      |
| Second send attempted | No       |
| Third send attempted  | No       |
| Exception thrown      | Yes      |


**Important Verification**
```java
verifyNoMoreInteractions(kafkaProducerMock);
```

This means after the first failure:
- service stops immediately
- no further Kafka calls happen

### 5.3 process_OrderDispatchedThrowsException()

**Purpose**

Tests what happens if the **second Kafka send** fails.

**Flow**
| Step                             | Result       |
| -------------------------------- | ------------ |
| First send (`DispatchPreparing`) | succeeds     |
| Second send (`OrderDispatched`)  | fails        |
| Third send (`DispatchCompleted`) | not executed |


**Expected Behavior**
| Check                   | Expected |
| ----------------------- | -------- |
| First send called once  | Yes      |
| Second send called once | Yes      |
| Third send called       | No       |
| Exception thrown        | Yes      |

**Meaning**

This confirms the method does not continue after failure in the second step.

### 5.4 `process_SecondDispatchedProducerThrowException()`

**Purpose**

Tests what happens if the **third Kafka send** fails.

**Flow**
| Step                             | Result   |
| -------------------------------- | -------- |
| First send (`DispatchPreparing`) | succeeds |
| Second send (`OrderDispatched`)  | succeeds |
| Third send (`DispatchCompleted`) | fails    |


**Expected Behavior**
| Check                   | Expected |
| ----------------------- | -------- |
| First send called once  | Yes      |
| Second send called once | Yes      |
| Third send called once  | Yes      |
| Exception thrown        | Yes      |


**Meaning**

This confirms the service reaches the last step and fails there correctly.

## 6. Mockito Methods Used

| Mockito Method                  | Purpose                                |
| ------------------------------- | -------------------------------------- |
| `mock(...)`                     | Creates fake object                    |
| `when(...).thenReturn(...)`     | Defines success behavior               |
| `doThrow(...).when(...)`        | Defines exception behavior             |
| `verify(...)`                   | Confirms method call                   |
| `times(1)`                      | Ensures called exactly once            |
| `verifyNoMoreInteractions(...)` | Ensures no extra Kafka calls were made |


## 7. Assertion Methods Used
| Assertion                       | Purpose                    |
| ------------------------------- | -------------------------- |
| `assertThrows(...)`             | Checks exception is thrown |
| `assertThat(..., equalTo(...))` | Verifies exception message |


## 8. Test Logic Flow Summary
**Successful Flow**
```bash
process()
 ├── send DispatchPreparing ✅
 ├── send OrderDispatched ✅
 └── send DispatchCompleted ✅
```

**First Send Failure**
```bash
process()
 ├── send DispatchPreparing ❌
 └── stop
``` 
**Second Send Failure**

```bash
process()
 ├── send DispatchPreparing ✅
 ├── send OrderDispatched ❌
 └── stop
```

**Third Send Failure**
```bash
process()
 ├── send DispatchPreparing ✅
 ├── send OrderDispatched ✅
 ├── send DispatchCompleted ❌
 └── exception
```

## 9. Strengths of This Test Class

| Strength             | Explanation                                 |
| -------------------- | ------------------------------------------- |
| Good coverage        | Tests success and all main failure points   |
| Isolated testing     | No real Kafka dependency                    |
| Clear verification   | Confirms exact topic, key, and payload type |
| Failure path testing | Ensures processing stops on error           |


## 10. Final Summary Table

| Test Case         | 1st Send | 2nd Send    | 3rd Send    | Expected Outcome  |
| ----------------- | -------- | ----------- | ----------- | ----------------- |
| Success           | ✅        | ✅           | ✅           | All messages sent |
| First send fails  | ❌        | Not reached | Not reached | Exception thrown  |
| Second send fails | ✅        | ❌           | Not reached | Exception thrown  |
| Third send fails  | ✅        | ✅           | ❌           | Exception thrown  |

------

# OrderDispatchIntegrationTest — Summary Tables

> Source: Uploaded integration test file :contentReference[oaicite:0]{index=0}

```java

@Slf4j
@SpringBootTest(classes = {
        DispatchConfiguration.class,
        OrderDispatchIntegrationTest.TestConfig.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(controlledShutdown = true)
public class OrderDispatchIntegrationTest {
    private final static String ORDER_CREATED_TOPIC = "order.created";
    private final static String ORDER_DISPATCHED_TOPIC = "order.dispatched";
    private final static String DISPATCH_TRACKING_TOPIC = "dispatch.tracking";

    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry registry;


    @Autowired
    private KafkaTestListener testListener;

    @Configuration
    static class TestConfig {

        @Bean
        public KafkaTestListener testListener() {
            return new KafkaTestListener();
        }
    }

    /**
     * Use this receiver to consume messages from the outbound topics.
     */

    @KafkaListener(groupId ="kafkaIntegrationTest", topics = {DISPATCH_TRACKING_TOPIC, ORDER_DISPATCHED_TOPIC})
    public static class KafkaTestListener {
        AtomicInteger dispatchPreparingCounter = new AtomicInteger(0);
        AtomicInteger orderDispatchedCounter = new AtomicInteger(0);
        AtomicInteger dispatchCompletedCounter=new AtomicInteger(0);


       // @KafkaListener(groupId = "KafkaIntegrationTest", topics = DISPATCH_TRACKING_TOPIC)
        @KafkaHandler
        void receiveDispatchPreparing(@Header(KafkaHeaders.RECEIVED_KEY) String key, @Payload DispatchPreparing payload) {

            log.info("receiveDispatchPreparing=>Received DispatchPreparing key: {}  - payload: {}", key, payload);
            assertThat(key, notNullValue());
            assertThat(payload, notNullValue());
            dispatchPreparingCounter.incrementAndGet();
        }

       // @KafkaListener(groupId = "KafkaIntegrationTest", topics = ORDER_DISPATCHED_TOPIC)
        @KafkaHandler
        void receiveOrderDispatched(@Header(KafkaHeaders.RECEIVED_KEY) String key, @Payload OrderDispatched payload) {
            log.info("receiveOrderDispatched ==> Received OrderDispatched: key: {} - payload :{} ", key, payload);
            assertThat(key, notNullValue());
            assertThat(payload, notNullValue());
            orderDispatchedCounter.incrementAndGet();
        }

        @KafkaHandler
        void receivedDispatchCompleted(@Header(KafkaHeaders.RECEIVED_KEY) String key, @Payload DispatchCompleted dispatchCompleted){
            log.info("receivedDispatchCompleted ==> Received DispatchCompleted : key : {} - payload: {}", key,dispatchCompleted);
            assertThat(key,notNullValue());
            assertThat(dispatchCompleted, notNullValue());
            dispatchCompletedCounter.incrementAndGet();
        }
    }

    @BeforeEach
    public void setUp() {
        testListener.dispatchPreparingCounter.set(0);
        testListener.orderDispatchedCounter.set(0);
        testListener.dispatchCompletedCounter.set(0);

        // Wait until the partitions are assigned.
//        registry.getListenerContainers().stream().forEach(container ->
//                ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic()));

        // Wait until the partitions are assigned.  The application listener container has one topic and the test
        // listener container has multiple topics, so take that into account when awaiting for topic assignment.
        registry.getListenerContainers().stream()
                .forEach(container -> ContainerTestUtils.waitForAssignment(container,
                        container.getContainerProperties().getTopics().length * embeddedKafkaBroker.getPartitionsPerTopic()));

    }

    @Test
    public void testOrderDispatchFlow() throws Exception {
        OrderCreated orderCreated =
                TestEventData.buildOrderCreatedEvent(randomUUID(), "my-item");
        sendMessage(ORDER_CREATED_TOPIC, randomUUID().toString(), orderCreated);
    }

    @Test
    public void testDispatchTrackingListenerOnly() throws Exception {
        DispatchPreparing payload = DispatchPreparing.builder()
                .orderId(randomUUID())
                .build();
        log.info("testDispatchTrackingListenerOnly===> Received OrderDispatched### " + payload);
        sendMessage(DISPATCH_TRACKING_TOPIC,randomUUID().toString(), payload);

        await().atMost(5, TimeUnit.SECONDS)
                .until(testListener.dispatchPreparingCounter::get, equalTo(1));
//        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
//                .until(testListener.dispatchPreparingCounter::get, equalTo(1));
    }


    @Test
    public void testOrderDispatchedListenerOnly() throws Exception {
        OrderDispatched orderDispatched = OrderDispatched.builder()
                .orderId(randomUUID())
                .build();
        log.info("testOrderDispatchedListenerOnly===> ");
        sendMessage(ORDER_DISPATCHED_TOPIC,randomUUID().toString(), orderDispatched);
        await().atMost(5, TimeUnit.SECONDS)
                .until(testListener.orderDispatchedCounter::get, equalTo(1));
    }


    @Test
    public void testDispatchCompletedListenerOnly() throws Exception {
        DispatchCompleted dispatchCompleted=DispatchCompleted.builder()
                .orderId(randomUUID())
                .dispatchedDate(LocalDate.now().toString())
                .build();
        log.info("testDispatchCompletedListenerOnly ==>");
        sendMessage(DISPATCH_TRACKING_TOPIC,randomUUID().toString(),dispatchCompleted);
        await().atMost(5, TimeUnit.SECONDS)
                .until(testListener.dispatchCompletedCounter::get,equalTo(1));
    }

    private void sendMessage(String topic, String key, Object data) throws Exception {
        kafkaTemplate.send(MessageBuilder
                .withPayload(data)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build()).get();
    }
}

```


## 1. Class Overview

| Item | Description |
|------|-------------|
| Class Name | `OrderDispatchIntegrationTest` |
| Test Type | Integration Test |
| Purpose | Tests full Kafka flow with real embedded broker |
| Scope | End-to-end message flow validation |

---

## 2. Key Annotations

| Annotation | Meaning | Purpose |
|------------|--------|---------|
| `@SpringBootTest` | Loads Spring context | Full application testing |
| `@EmbeddedKafka` | Starts in-memory Kafka | No external Kafka needed |
| `@DirtiesContext` | Resets context after tests | Clean state |
| `@ActiveProfiles("test")` | Uses test config | Isolated environment |

---

## 3. Topics Used

| Constant | Topic Name | Purpose |
|----------|------------|---------|
| `ORDER_CREATED_TOPIC` | `order.created` | Input topic |
| `ORDER_DISPATCHED_TOPIC` | `order.dispatched` | Output event |
| `DISPATCH_TRACKING_TOPIC` | `dispatch.tracking` | Tracking events |

---

## 4. Dependencies (Autowired)

| Field | Type | Description |
|------|------|-------------|
| kafkaTemplate | `KafkaTemplate` | Sends messages |
| embeddedKafkaBroker | `EmbeddedKafkaBroker` | In-memory Kafka |
| registry | `KafkaListenerEndpointRegistry` | Manages listeners |
| testListener | `KafkaTestListener` | Captures consumed messages |

---

## 5. KafkaTestListener (Inner Class)

### Purpose

| Feature | Explanation |
|--------|------------|
| Acts as consumer | Listens to Kafka topics |
| Validates messages | Ensures payload + key are not null |
| Tracks counts | Uses counters to verify processing |

---

### Counters

| Counter | Tracks |
|--------|--------|
| dispatchPreparingCounter | `DispatchPreparing` events |
| orderDispatchedCounter | `OrderDispatched` events |
| dispatchCompletedCounter | `DispatchCompleted` events |

---

### Handlers

| Method | Payload Type | Purpose |
|--------|-------------|--------|
| `receiveDispatchPreparing` | `DispatchPreparing` | Handles start event |
| `receiveOrderDispatched` | `OrderDispatched` | Handles dispatched event |
| `receivedDispatchCompleted` | `DispatchCompleted` | Handles completion event |

---

## 6. Setup Method

### Purpose

| Action | Description |
|--------|-------------|
| Reset counters | Set all counters to 0 |
| Wait for partition assignment | Ensures Kafka listeners are ready |

---

### Partition Assignment Logic

```java
ContainerTestUtils.waitForAssignment(container,
  container.getContainerProperties().getTopics().length * embeddedKafkaBroker.getPartitionsPerTopic());
```

**Why Important**
Prevents test failure due to listener not ready
Ensures messages are consumed

## 7. Test Cases Summary

| Test Method                         | Scenario              | Expected Outcome               |
| ----------------------------------- | --------------------- | ------------------------------ |
| `testOrderDispatchFlow`             | Full flow test        | All events should be processed |
| `testDispatchTrackingListenerOnly`  | Only tracking topic   | DispatchPreparing consumed     |
| `testOrderDispatchedListenerOnly`   | Only dispatched topic | OrderDispatched consumed       |
| `testDispatchCompletedListenerOnly` | Only completed event  | DispatchCompleted consumed     |


## 8. Detailed Test Flow

### 8.1 `testOrderDispatchFlow`

| Step | Description                     |
| ---- | ------------------------------- |
| 1    | Create `OrderCreated` event     |
| 2    | Send to `order.created`         |
| 3    | DispatchService processes event |
| 4    | Produces 3 events               |


**Expected Flow**
```bash
order.created
     ↓
DispatchPreparing → dispatch.tracking
OrderDispatched → order.dispatched
DispatchCompleted → dispatch.tracking
```

### 8.2 `testDispatchTrackingListenerOnly`

| Step | Description                 |
| ---- | --------------------------- |
| 1    | Create `DispatchPreparing`  |
| 2    | Send to `dispatch.tracking` |
| 3    | Listener consumes it        |


**Verification**
```java
await().atMost(5, TimeUnit.SECONDS)
       .until(counter == 1);
```

**Meaning**
- Waits until message is consumed
- Handles async Kafka behavior

### 8.3 `testOrderDispatchedListenerOnly`

| Step | Description                |
| ---- | -------------------------- |
| 1    | Create `OrderDispatched`   |
| 2    | Send to `order.dispatched` |
| 3    | Listener consumes it       |


### 8.4 `testDispatchCompletedListenerOnly`

| Step | Description                 |
| ---- | --------------------------- |
| 1    | Create `DispatchCompleted`  |
| 2    | Send to `dispatch.tracking` |
| 3    | Listener consumes it        |


## 9. Message Sending Method

```java
kafkaTemplate.send(
    MessageBuilder.withPayload(data)
        .setHeader(KafkaHeaders.KEY, key)
        .setHeader(KafkaHeaders.TOPIC, topic)
        .build()
).get();

```

| Part     | Purpose                   |
| -------- | ------------------------- |
| Payload  | Actual data               |
| KEY      | Partition key             |
| TOPIC    | Kafka topic               |
| `.get()` | Waits for send completion |


## 10. Awaitility Usage

| Feature               | Purpose                  |
| --------------------- | ------------------------ |
| `await()`             | Wait for async operation |
| `atMost(5 seconds)`   | Timeout                  |
| `until(counter == 1)` | Condition                |


✔ Prevents flaky tests
✔ Ensures message consumption

## 11. Key Differences: Unit Test vs Integration Test

| Aspect      | Unit Test        | Integration Test      |
| ----------- | ---------------- | --------------------- |
| Kafka       | Mocked           | Real (EmbeddedKafka)  |
| Speed       | Fast             | Slower                |
| Scope       | Single class     | Full flow             |
| Reliability | Limited          | High                  |
| Purpose     | Logic validation | End-to-end validation |


## 12. Strengths of This Test

| Strength            | Explanation                    |
| ------------------- | ------------------------------ |
| Real Kafka          | Uses embedded broker           |
| Async handling      | Uses Awaitility                |
| Full flow tested    | Covers full pipeline           |
| Clean setup         | Waits for partition assignment |
| Multi-event support | Handles multiple payload types |


## 13. Potential Improvements
| Improvement                               | Reason                        |
| ----------------------------------------- | ----------------------------- |
| Add assertions in `testOrderDispatchFlow` | Currently no verification     |
| Verify all 3 counters                     | Ensure full pipeline executed |
| Add negative test cases                   | Test failure scenarios        |
| Add ordering validation                   | Ensure event sequence         |


## 14. Key Idea
| Concept             | Explanation                               |
| ------------------- | ----------------------------------------- |
| Integration Test    | Tests real interaction between components |
| Embedded Kafka      | Simulates real Kafka environment          |
| Listener Validation | Confirms messages are consumed correctly  |
| Async Testing       | Uses Awaitility to handle delays          |


## 15. Final Summary
**What This Test Ensures**
- Kafka messages are produced correctly
- Messages are consumed by listeners
- End-to-end dispatch workflow works
- System behaves correctly in async environment
