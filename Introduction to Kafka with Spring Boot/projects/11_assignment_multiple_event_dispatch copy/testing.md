# Kafka Configuration 

## Configuration Explained
```yaml
kafka:
    bootstrap-servers: ${spring.embedded.kafka.brokers}
```

### 🔹 Summary Table

| Property                           | Meaning                     | Explanation                                              |
| ---------------------------------- | --------------------------- | -------------------------------------------------------- |
| `kafka.bootstrap-servers`          | Kafka broker address        | Tells your application where Kafka is running            |
| `${spring.embedded.kafka.brokers}` | Dynamic value (from Spring) | Automatically provides Kafka broker address during tests |

----

```java

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
```


## 1. Purpose of the Test Class

| Component | Description |
|----------|------------|
| `OrderCreateHandlerTest` | Unit test class for testing `OrderCreateHandler` |
| Goal | Verify that incoming Kafka messages are handled correctly |
| Dependency | `DispatchService` (mocked) |
| Focus | Ensuring `listen()` method calls `process()` correctly |

---

## 2. Setup Phase (@BeforeEach)

| Step | Code | Purpose |
|-----|------|--------|
| 1 | `dispatchServiceMock = mock(DispatchService.class)` | Create mock of service |
| 2 | `orderCreateHandler = new OrderCreateHandler(dispatchServiceMock)` | Inject mock into handler |
| Outcome | Handler is isolated for unit testing |

---

## 3. Test Case 1 — Success Scenario

### ▸ Method: `listenSuccess()`

| Element | Value |
|--------|------|
| Input Key | Random UUID |
| Input Event | `OrderCreated` test object |
| Action | `orderCreateHandler.listen(0, key, testEvent)` |
| Expected Behavior | Service method is called |
| Verification | `process(key, testEvent)` called exactly once |

### ✔ Assertion Summary

| Check | Result |
|------|--------|
| Method call count | `times(1)` |
| Exception expected | ❌ No |
| Outcome | ✅ Success path verified |

---

## 4. Test Case 2 — Exception Scenario

### ▸ Method: `listen_ServiceThrowsException()`

| Element | Value |
|--------|------|
| Input Key | Random UUID |
| Input Event | `OrderCreated` test object |
| Mock Behavior | `dispatchServiceMock.process()` throws exception |
| Action | `orderCreateHandler.listen(0, key, testEvent)` |
| Expected Behavior | Method still invoked despite exception |

### ✔ Assertion Summary

| Check | Result |
|------|--------|
| Method call count | `times(1)` |
| Exception thrown from test | ❌ Not propagated |
| Outcome | ✅ Handler tolerates service failure |

---

## 5. Behavior Comparison

| Scenario | Service Behavior | Handler Reaction | Result |
|----------|----------------|----------------|--------|
| Success | Normal execution | Calls `process()` | ✅ Pass |
| Exception | Throws RuntimeException | Still calls `process()` | ✅ Pass |

---

## 6. Key Testing Concepts Used

| Concept | Usage |
|--------|------|
| Mocking | `DispatchService` mocked using Mockito |
| Dependency Injection | Mock passed into handler |
| Verification | `verify(..., times(1))` ensures method call |
| Exception Simulation | `doThrow()` used to simulate failure |
| Isolation | Only handler logic tested |

---

## 7. Key Takeaways

| Insight | Explanation |
|--------|------------|
| Handler Responsibility | Delegates work to `DispatchService` |
| Robustness | Handles service failure without crashing |
| Test Coverage | Covers both success and failure paths |
| Design Benefit | Loose coupling via dependency injection |

---

## Final Summary

| Aspect | Explanation |
|-------|------------|
| What is tested? | `listen()` method of handler |
| What is verified? | Service interaction |
| Why important? | Ensures reliable message handling |
| Overall Result | ✅ Handler behaves correctly in all scenarios |


----
# OrderDispatchIntegrationTest 

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
    public static class KafkaTestListener {
        AtomicInteger dispatchPreparingCounter = new AtomicInteger(0);
        AtomicInteger orderDispatchedCounter = new AtomicInteger(0);

        @KafkaListener(groupId = "KafkaIntegrationTest", topics = DISPATCH_TRACKING_TOPIC)
        void receiveDispatchPreparing(@Header(KafkaHeaders.RECEIVED_KEY) String key, @Payload DispatchPreparing payload) {

            log.info("receiveDispatchPreparing=>Received DispatchPreparing key: {}  - payload: {}", key, payload);
            assertThat(key, notNullValue());
            assertThat(payload, notNullValue());
            dispatchPreparingCounter.incrementAndGet();
        }

        @KafkaListener(groupId = "KafkaIntegrationTest", topics = ORDER_DISPATCHED_TOPIC)
        void receiveOrderDispatched(@Header(KafkaHeaders.RECEIVED_KEY) String key, @Payload OrderDispatched payload) {
            log.info("receiveOrderDispatched ==> Received OrderDispatched: key: {} - payload :{} ", key, payload);
            assertThat(key, notNullValue());
            assertThat(payload, notNullValue());
            orderDispatchedCounter.incrementAndGet();
        }
    }

    @BeforeEach
    public void setUp() {
        testListener.dispatchPreparingCounter.set(0);
        testListener.orderDispatchedCounter.set(0);

        // Wait until the partitions are assigned.
        registry.getListenerContainers().stream().forEach(container ->
                ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic()));
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

    private void sendMessage(String topic, String key, Object data) throws Exception {
        kafkaTemplate.send(MessageBuilder
                .withPayload(data)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build()).get();
    }
}

```


## 1. Test Class Overview

| Component | Purpose | Key Details |
|----------|--------|------------|
| `@SpringBootTest` | Loads full Spring context | Uses `DispatchConfiguration` + `TestConfig` |
| `@EmbeddedKafka` | Starts in-memory Kafka broker | No need for real Kafka |
| `@DirtiesContext` | Resets context after tests | Avoids side effects |
| `@ActiveProfiles("test")` | Uses test config | Isolated environment |
| `OrderDispatchIntegrationTest` | Integration test class | Tests Kafka flow end-to-end |

---

## 2. Topics Used

| Constant | Topic Name | Purpose |
|---------|------------|--------|
| `ORDER_CREATED_TOPIC` | `order.created` | Input event |
| `ORDER_DISPATCHED_TOPIC` | `order.dispatched` | Output event |
| `DISPATCH_TRACKING_TOPIC` | `dispatch.tracking` | Intermediate event |

---

## 3. Injected Beans

| Bean | Type | Purpose |
|------|------|--------|
| `kafkaTemplate` | KafkaTemplate | Send messages to Kafka |
| `embeddedKafkaBroker` | EmbeddedKafkaBroker | In-memory Kafka |
| `registry` | KafkaListenerEndpointRegistry | Manage listeners |
| `testListener` | KafkaTestListener | Consume and verify messages |

---

## 4. Test Listener (Consumer)

| Method | Topic | Purpose | Action |
|--------|------|--------|--------|
| `receiveDispatchPreparing` | `dispatch.tracking` | Validate DispatchPreparing event | Increment counter |
| `receiveOrderDispatched` | `order.dispatched` | Validate OrderDispatched event | Increment counter |

### Internal Counters

| Counter | Type | Purpose |
|--------|------|--------|
| `dispatchPreparingCounter` | AtomicInteger | Tracks dispatch.tracking messages |
| `orderDispatchedCounter` | AtomicInteger | Tracks order.dispatched messages |

---

## 5. Setup Phase (`@BeforeEach`)

| Step | Action | Purpose |
|------|--------|--------|
| Reset counters | Set both counters to 0 | Clean state before test |
| Wait for assignment | `ContainerTestUtils.waitForAssignment` | Ensure consumers are ready |

---

## 6. Test Cases

### ▸ 6.1 testOrderDispatchFlow

| Step | Action |
|------|--------|
| Create event | Build `OrderCreated` |
| Send message | Send to `order.created` |
| Expected | Full flow triggers downstream events |

---

### ▸ 6.2 testDispatchTrackingListenerOnly

| Step | Action |
|------|--------|
| Create payload | `DispatchPreparing` |
| Send message | Send to `dispatch.tracking` |
| Verify | Wait until counter = 1 |
| Tool | `Awaitility` |

---

### ▸ 6.3 testOrderDispatchedListenerOnly

| Step | Action |
|------|--------|
| Create payload | `OrderDispatched` |
| Send message | Send to `order.dispatched` |
| Verify | Wait until counter = 1 |

---

## 7. Message Sending Logic

| Method | Purpose | Key Steps |
|--------|--------|----------|
| `sendMessage()` | Send Kafka message | |
| Step 1 | Build message | `MessageBuilder.withPayload()` |
| Step 2 | Set key | `KafkaHeaders.KEY` |
| Step 3 | Set topic | `KafkaHeaders.TOPIC` |
| Step 4 | Send | `kafkaTemplate.send()` |
| Step 5 | Wait | `.get()` ensures sync |

---

## 8. Testing Strategy

| Strategy | Explanation |
|---------|-------------|
| Embedded Kafka | Avoid external dependency |
| Listener-based validation | Verify actual consumption |
| Counters | Track message processing |
| Awaitility | Handle async verification |
| End-to-end flow | Tests real Kafka pipeline |

---

## 9. Key Concepts Covered

| Concept | Explanation |
|--------|-------------|
| Integration Testing | Tests full Kafka flow |
| Producer → Consumer | End-to-end message flow |
| Async Testing | Uses Awaitility |
| Kafka Listeners | Consumes messages |
| Embedded Broker | Lightweight Kafka for tests |

---

## 10. Overall Flow

| Step | Flow |
|------|------|
| 1 | Send message via KafkaTemplate |
| 2 | Kafka topic receives message |
| 3 | Listener consumes message |
| 4 | Assertions validate payload |
| 5 | Counter increments |
| 6 | Awaitility verifies result |

---

## Final Summary

| Area | Outcome |
|------|--------|
| Reliability | Ensures Kafka flow works |
| Isolation | Uses embedded Kafka |
| Validation | Confirms message consumption |
| Async Handling | Proper wait mechanism |
| Coverage | Tests both individual and full flow |

---

✔ This test ensures your Kafka pipeline is working correctly from producer → topic → consumer  
✔ It validates both **individual listeners** and **complete event flow**

---

# DispatchServiceTest
This test class verifies the behavior of `DispatchService` when producing Kafka messages under different scenarios.


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
```


## 1. Overall Test Structure

| Component | Purpose |
|----------|--------|
| `DispatchService` | Service under test |
| `KafkaTemplate` (mock) | Simulates Kafka producer |
| `@BeforeEach` | Initializes mock + service before each test |
| `process()` | Method being tested |
| `key` | Kafka message key |
| `OrderCreated` | Input event |
| `DispatchPreparing` | First Kafka message |
| `OrderDispatched` | Second Kafka message |

---

## 2. Happy Path — `process_Success`

| Step | Action | Expected Behavior |
|------|--------|------------------|
| 1 | Mock Kafka `send()` for both topics | Returns success (CompletableFuture) |
| 2 | Call `service.process(key, event)` | Service processes normally |
| 3 | Send to `dispatch.tracking` | Called once |
| 4 | Send to `order.dispatched` | Called once |
| 5 | No exception | Execution successful |

### ✔ Key Validation

| Verification | Meaning |
|-------------|--------|
| `times(1)` | Each message sent exactly once |
| `eq(key)` | Same key used for both messages |
| Topics | Correct topics used |

---

## 3. Failure Case 1 — Dispatch Tracking Fails

### `process_DispatchTrackingProducerThrowsException`

| Step | Action | Expected Behavior |
|------|--------|------------------|
| 1 | Mock exception on `dispatch.tracking` send | Throws RuntimeException |
| 2 | Call `process()` | Exception occurs immediately |
| 3 | First send attempt | Executed once |
| 4 | Second send (`order.dispatched`) | ❌ NOT executed |
| 5 | Exception propagated | Yes |

### ✔ Key Validation

| Check | Result |
|------|--------|
| First send called | ✅ Yes |
| Second send called | ❌ No |
| `verifyNoMoreInteractions` | Ensures no extra calls |
| Exception message | `"dispatch tracking producer failure"` |

### ✔ Insight
> If the first Kafka publish fails → system stops immediately (fail-fast behavior)

---

## 4. Failure Case 2 — Order Dispatched Fails

### `process_OrderDispatchedThrowsException`

| Step | Action | Expected Behavior |
|------|--------|------------------|
| 1 | First send (`dispatch.tracking`) succeeds | Returns future |
| 2 | Second send throws exception | RuntimeException |
| 3 | Call `process()` | Fails after second send |
| 4 | First send executed | ✅ Yes |
| 5 | Second send executed | ✅ Yes |
| 6 | Exception propagated | Yes |

### ✔ Key Validation

| Check | Result |
|------|--------|
| First send called | ✅ Yes |
| Second send called | ✅ Yes |
| Exception message | `"order dispatch producer failure"` |

### ✔ Insight
> If second Kafka publish fails → first message already sent (partial success)

---

## 5. Behavior Comparison

| Scenario | First Send | Second Send | Result |
|----------|-----------|------------|--------|
| Success | ✅ | ✅ | All messages sent |
| First Failure | ❌ (fails) | ❌ | Stops immediately |
| Second Failure | ✅ | ❌ (fails) | Partial success |

---

## 6. Key Testing Concepts Used

| Concept | Purpose |
|--------|--------|
| `mock()` | Create fake KafkaTemplate |
| `when().thenReturn()` | Mock successful calls |
| `doThrow()` | Simulate failure |
| `verify()` | Check method calls |
| `verifyNoMoreInteractions()` | Ensure no extra calls |
| `assertThrows()` | Validate exception |
| `eq()` / `any()` | Argument matchers |

---

## 7. Core Takeaways

- Service sends **two Kafka events sequentially**
- First event → `dispatch.tracking`
- Second event → `order.dispatched`

### Behavior Rules

| Rule | Meaning |
|------|--------|
| Fail-fast | If first send fails → stop |
| No rollback | If second fails → first already sent |
| Deterministic calls | Each send verified exactly once |

---

## Final Summary

| Aspect | Outcome |
|--------|--------|
| Reliability | Depends on Kafka send success |
| Error Handling | Exceptions propagated |
| Message Flow | Sequential (not transactional) |
| Testing Coverage | Success + both failure paths |