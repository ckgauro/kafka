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
        void receiveDispatchPreparing(@Payload DispatchPreparing payload) {
            log.debug("Received DispatchPreparing: " + payload);
            dispatchPreparingCounter.incrementAndGet();
        }

        @KafkaListener(groupId = "KafkaIntegrationTest", topics = ORDER_DISPATCHED_TOPIC)
        void receiveOrderDispatched(@Payload OrderDispatched payload) {
            log.debug("Received OrderDispatched: " + payload);
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
        sendMessage(ORDER_CREATED_TOPIC, orderCreated);
    }

    @Test
    public void testDispatchTrackingListenerOnly() throws Exception {
        DispatchPreparing payload = DispatchPreparing.builder()
                .orderId(randomUUID())
                .build();

        sendMessage(DISPATCH_TRACKING_TOPIC, payload);

        await().atMost(5, TimeUnit.SECONDS)
                .until(testListener.dispatchPreparingCounter::get, equalTo(1));
    }


    @Test
    public void testOrderDispatchedListenerOnly() throws  Exception{
        OrderDispatched orderDispatched= OrderDispatched.builder()
                .orderId(randomUUID())
                .build();

        sendMessage(ORDER_DISPATCHED_TOPIC,orderDispatched);
        await().atMost(5,TimeUnit.SECONDS)
                .until(testListener.orderDispatchedCounter::get,equalTo(1));
    }

    private void sendMessage(String topic, Object data) throws Exception {
        kafkaTemplate.send(MessageBuilder
                .withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build()).get();
    }
}
```
# OrderDispatchIntegrationTest Explained

## Purpose of this test class

This is an **integration test** for a Spring Boot Kafka-based flow.

Its job is to verify that:
- the app can send Kafka messages during tests
- Kafka listeners are started correctly
- messages published to topics can be consumed
- the overall dispatch flow works with an `embedded Kafka broker` instead of a real external Kafka server

So this is not a simple unit test. It checks how multiple Spring and Kafka components work together.

## Full class overview

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

```

This class uses several important annotations.

### 1. @Slf4j
```java
@Slf4j
```
This is a Lombok annotation.

**What it does**

It automatically creates a logger object named log.

So instead of writing:
```java
private static final Logger log = LoggerFactory.getLogger(OrderDispatchIntegrationTest.class);
```

Lombok generates it for you.

**Why it is used here**

Inside the Kafka listener methods, logs are printed:
```java
log.debug("Received DispatchPreparing: " + payload);
```
and
```java
log.debug("Received OrderDispatched: " + payload);
```
This helps during testing and debugging.

### 2. @SpringBootTest
```java
@SpringBootTest(classes = {
        DispatchConfiguration.class,
        OrderDispatchIntegrationTest.TestConfig.class
})
```

This tells Spring Boot to start an **application context** for the test.

**What it does**

Instead of testing only one class in isolation, Spring loads the required beans and wiring.

**Why specific classes are passed**

Here, only these configurations are loaded:
- `DispatchConfiguration.class`
- `OrderDispatchIntegrationTest.TestConfig.class`

That means the test context includes:

- your Kafka-related application configuration from `DispatchConfiguration`
- test-only beans from `TestConfig`

**Why this matters**

This keeps the test focused and avoids loading unnecessary parts of the application.

### 3. @DirtiesContext
```java
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
```

**What it means**

After all tests in this class finish, Spring will mark the application context as dirty and destroy it.

**Why it is useful**

Kafka listeners, embedded brokers, and Spring contexts can keep state between tests or test classes.

Using this annotation avoids side effects when other tests run later.

**In simple words**

It says:
> "After this test class is done, throw away this Spring context and do not reuse it."

### 4. @ActiveProfiles("test")
```java
@ActiveProfiles("test")
```

**What it does**

This activates the `test` Spring profile.

**Why it matters**

Spring will load test-specific configuration, usually from files like:

```yaml
application-test.yml
application-test.properties
```

This is useful for test Kafka configuration, test topics, serializers, or other test-only values.

### 5. @EmbeddedKafka
```java
@EmbeddedKafka(controlledShutdown = true)
```

**What it does**

This starts an **in-memory embedded Kafka broker** for the test.

**Why it is important**

Without this, your test would need a real Kafka server running somewhere.

Embedded Kafka gives you:
- isolated test execution
- no dependency on external Kafka
- repeatable integration tests
`controlledShutdown = true`

This allows Kafka broker shutdown to happen more gracefully.

### 6. Topic constants
```java
private final static String ORDER_CREATED_TOPIC = "order.created";
private final static String ORDER_DISPATCHED_TOPIC = "order.dispatched";
private final static String DISPATCH_TRACKING_TOPIC = "dispatch.tracking";
```
These are topic names used in the tests.

**Meaning of each topic**
`order.created`

This is likely the input topic.
An OrderCreated event is sent here to start the dispatch workflow.

`order.dispatched`

This is likely one of the output topics.
After processing, the system publishes an OrderDispatched event here.

`dispatch.tracking`

This is another output topic.
The system probably sends a DispatchPreparing event here as part of tracking.

Using constants is better than hardcoding strings everywhere because it reduces mistakes.

**Injected beans**

### 7. KafkaTemplate
```java
@Autowired
private KafkaTemplate kafkaTemplate;
```

**What it is**

`KafkaTemplate` is Spring Kafka’s helper class for publishing messages.

**What it does here**

It sends test messages to Kafka topics.

Used in:
```java
kafkaTemplate.send(...)
```

**Why raw type is not ideal**

Here it is declared as:
```java
KafkaTemplate kafkaTemplate;
```

A better version is usually:
```java
KafkaTemplate<String, Object>
```

or some more specific type.

Because raw types remove compile-time type safety.

### 8. EmbeddedKafkaBroker

```java
@Autowired
private EmbeddedKafkaBroker embeddedKafkaBroker;
```

**What it is**

This represents the embedded Kafka broker started by `@EmbeddedKafka`.

**Why it is needed**

It is used in setup to determine partition assignment count:
```java
embeddedKafkaBroker.getPartitionsPerTopic()
```
This helps wait until listeners are fully ready.

### 9. KafkaListenerEndpointRegistry

```java
@Autowired
private KafkaListenerEndpointRegistry registry;
```

**What it is**

This registry holds all `@KafkaListener` listener containers managed by Spring.

**Why it is important**

Kafka listeners may not be ready immediately when the test starts.

So before sending a message, you should wait until all listener containers are assigned to partitions.

This line in setup uses it:
```java
registry.getListenerContainers().stream().forEach(container ->
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic()));

```

Without this, messages may be sent before listeners are ready, and tests can fail intermittently.

### 9. KafkaTestListener

```java
@Autowired
private KafkaTestListener testListener;
```

**What it is**

This is a custom test helper bean defined inside the test class.

**Why it exists**

It consumes messages from outbound topics and counts how many messages arrive.

This allows assertions like:

```java
await().atMost(5, TimeUnit.SECONDS)
        .until(testListener.dispatchPreparingCounter::get, equalTo(1));
```

So instead of checking Kafka directly, the test uses this listener to confirm message delivery.

Inner test configuration

```java
@Configuration
static class TestConfig {

    @Bean
    public KafkaTestListener testListener() {
        return new KafkaTestListener();
    }
}

```
**Purpose**

This adds a bean to the Spring test context.

**Why needed**

`KafkaTestListener` is just a normal class. Spring will not automatically manage it unless you register it as a bean.

By defining it in `TestConfig`, Spring creates and manages it.

Then:

- `@KafkaListener` methods inside it become active
- it can be autowired into the test class

**The KafkaTestListener class**
```java
public static class KafkaTestListener {
    AtomicInteger dispatchPreparingCounter = new AtomicInteger(0);
    AtomicInteger orderDispatchedCounter = new AtomicInteger(0);
```

This class is used only for testing.

**Why `AtomicInteger` is used**

Kafka listeners run asynchronously on different threads.

If normal `int` variables were used, thread safety problems could happen.

`AtomicInteger` gives thread-safe increment and read operations.

**Two counters**
- `dispatchPreparingCounter` counts messages received from `dispatch.tracking`
- `orderDispatchedCounter` counts messages received from `order.dispatched`

**Listener 1: DispatchPreparing consumer**

```java
@KafkaListener(groupId = "KafkaIntegrationTest", topics = DISPATCH_TRACKING_TOPIC)
void receiveDispatchPreparing(@Payload DispatchPreparing payload) {
    log.debug("Received DispatchPreparing: " + payload);
    dispatchPreparingCounter.incrementAndGet();
}
```


**What it does**

This method listens to the `dispatch.tracking` topic.

Whenever a `DispatchPreparing` message arrives:

1. it logs the payload
2. increments `dispatchPreparingCounter`


**Why this is useful**

This is the proof that the message was successfully published and consumed.

**Listener 2: OrderDispatched consumer**

```java
@KafkaListener(groupId = "KafkaIntegrationTest", topics = ORDER_DISPATCHED_TOPIC)
void receiveOrderDispatched(@Payload OrderDispatched payload) {
    log.debug("Received OrderDispatched: " + payload);
    orderDispatchedCounter.incrementAndGet();
}
```

**What it does**

This method listens to the `order.dispatched` topic.

Whenever an `OrderDispatched` message is received:
- it logs the payload
- increments `orderDispatchedCounter`


**`@BeforeEach `setup**

```java
@BeforeEach
public void setUp() {
    testListener.dispatchPreparingCounter.set(0);
    testListener.orderDispatchedCounter.set(0);

    // Wait until the partitions are assigned.
    registry.getListenerContainers().stream().forEach(container ->
            ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic()));
}

```


This method runs before each test case.
----

**Step 1: Reset counters**
```java
testListener.dispatchPreparingCounter.set(0);
testListener.orderDispatchedCounter.set(0);
```


**Why needed**

Each test should start fresh.

If one test increments a counter and the next test starts without resetting it, results become unreliable.

**Step 2: Wait for listener assignment**

```java
registry.getListenerContainers().stream().forEach(container ->
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic()));
```

**Why this is very important**

Kafka listeners need time to join the consumer group and get partition assignments.

If you send a message before that process finishes:
- the message may be missed by the listener
- the test may fail even though the code is correct

**What this code does**

For every Kafka listener container registered in Spring:
- wait until it gets the expected number of partition assignments

This makes tests stable and avoids race conditions.

**Test 1: `testOrderDispatchFlow`**

```java
@Test
public void testOrderDispatchFlow() throws Exception {
    OrderCreated orderCreated =
            TestEventData.buildOrderCreatedEvent(randomUUID(), "my-item");
    sendMessage(ORDER_CREATED_TOPIC, orderCreated);
}
```

**What this test is trying to do**

This sends an `OrderCreated` event to the input topic:
```java
order.created
```

That should trigger the actual application flow.

Most likely your real application listener consumes OrderCreated, processes it, and then publishes messages to:
- `dispatch.tracking`
- `order.dispatched`

**Current problem in this test**

Right now, this test only sends the message.

It does **not verify anything**.

So the test passes as long as sending to Kafka succeeds, even if the downstream processing fails.

**Better version**

To truly test the flow, this method should wait for the output listeners to receive expected messages.

Example:

```java
@Test
public void testOrderDispatchFlow() throws Exception {
    OrderCreated orderCreated =
            TestEventData.buildOrderCreatedEvent(randomUUID(), "my-item");

    sendMessage(ORDER_CREATED_TOPIC, orderCreated);

    await().atMost(5, TimeUnit.SECONDS)
            .until(testListener.dispatchPreparingCounter::get, equalTo(1));

    await().atMost(5, TimeUnit.SECONDS)
            .until(testListener.orderDispatchedCounter::get, equalTo(1));
}
```

**Why this is better**

Now the test confirms the full flow:
- input event published
- application processed it
- both output events were produced and consumed

**Test 2: `testDispatchTrackingListenerOnly`**
```java
@Test
public void testDispatchTrackingListenerOnly() throws Exception {
    DispatchPreparing payload = DispatchPreparing.builder()
            .orderId(randomUUID())
            .build();

    sendMessage(DISPATCH_TRACKING_TOPIC, payload);

    await().atMost(5, TimeUnit.SECONDS)
            .until(testListener.dispatchPreparingCounter::get, equalTo(1));
}
```

**What this test does**

This is a direct test of the test listener on the `dispatch.tracking` topic.

**Flow**
1. build a `DispatchPreparing` payload
2. send it directly to `dispatch.tracking`
3. wait until listener receives it
4. verify counter becomes `1`

**Why `await()` is used**

Kafka processing is asynchronous.

If you write:

```java
assertEquals(1, testListener.dispatchPreparingCounter.get());
```

immediately after sending, the listener may not have consumed the message yet.

So Awaitility waits up to 5 seconds until the condition becomes true.

**Meaning of this condition**

```java
.until(testListener.dispatchPreparingCounter::get, equalTo(1));
```
It keeps checking:

```java
testListener.dispatchPreparingCounter.get()
```

until it equals `1`.

If that does not happen within 5 seconds, the test fails.

**Test 3: `testOrderDispatchedListenerOnly`**

```java
@Test
public void testOrderDispatchedListenerOnly() throws  Exception{
    OrderDispatched orderDispatched= OrderDispatched.builder()
            .orderId(randomUUID())
            .build();

    sendMessage(ORDER_DISPATCHED_TOPIC,orderDispatched);
    await().atMost(5,TimeUnit.SECONDS)
            .until(testListener.orderDispatchedCounter::get,equalTo(1));
}

```

**What this test does**

This directly verifies the listener for the order.dispatched topic.

**Flow**
1. create an OrderDispatched object
2. publish it to order.dispatched
3. wait for listener to consume it
4. assert the counter becomes 1

This confirms that:
- sending works
- deserialization works
- listener wiring works

**Helper method: `sendMessage`**

```java
private void sendMessage(String topic, Object data) throws Exception {
    kafkaTemplate.send(MessageBuilder
            .withPayload(data)
            .setHeader(KafkaHeaders.TOPIC, topic)
            .build()).get();
}
```

This is a reusable helper method used by all tests.

**Step-by-step explanation**

**1. Build a Spring message**
```java
MessageBuilder.withPayload(data)
```
Creates a message whose body is the object you want to send.

**2. Set topic header**
```java
.setHeader(KafkaHeaders.TOPIC, topic)
```

Tells Kafka which topic to publish the message to.

**3. Build message**
```java
.build()
```

Creates the final Spring `Message<?>`.

**4. Send it with KafkaTemplate**
```
kafkaTemplate.send(...)
```

Publishes the message to Kafka.

**5. Wait for completion**

```java
.get();
```

This is important.

Without `.get()`, sending is asynchronous and the method may return immediately.

With `.get()`, the test waits until Kafka confirms the send completed.

That makes the test more reliable.

#### What this integration test proves
**It verifies these things successfully**

**1. Spring test context loads properly**

Because `@SpringBootTest` creates the context.

**2. Embedded Kafka is working**

Because messages are published and consumed inside the test.

**3. Kafka listeners are registered and active**

Because the listener methods receive messages.

**4. Payload conversion/deserialization is working**

Because listener methods receive typed payloads:
- DispatchPreparing
- OrderDispatched

**5. Asynchronous Kafka communication works end-to-end**

Because the test waits for message delivery through actual Kafka infrastructure.

**Important testing pattern used here**
**Pattern summary**

This class uses a very common Kafka integration testing approach:

1. start Spring context
2. start embedded Kafka broker
3. register test listeners as beans
4. send messages with `KafkaTemplate`
5. wait asynchronously with Awaitility
6. verify counters

This pattern is simple and effective for Kafka integration tests.

### What is good in this code

Strong points
**1. Uses embedded Kafka**

This is the correct way to do integration testing without external Kafka dependency.

**2. Waits for partition assignment**

This avoids flaky tests.

**3. Uses thread-safe counters**

AtomicInteger is correct because listener execution is asynchronous.

**4. Uses Awaitility**

This is much better than Thread.sleep(...).

**5. Separates helper send method**

This keeps the test methods cleaner.

### What can be improved

**1. testOrderDispatchFlow() should assert expected output**

Right now:
```java
@Test
public void testOrderDispatchFlow() throws Exception {
    OrderCreated orderCreated =
            TestEventData.buildOrderCreatedEvent(randomUUID(), "my-item");
    sendMessage(ORDER_CREATED_TOPIC, orderCreated);
}
```
This is incomplete because there is no verification.

**Better version**

```java
@Test
public void testOrderDispatchFlow() throws Exception {
    OrderCreated orderCreated =
            TestEventData.buildOrderCreatedEvent(randomUUID(), "my-item");

    sendMessage(ORDER_CREATED_TOPIC, orderCreated);

    await().atMost(5, TimeUnit.SECONDS)
            .until(testListener.dispatchPreparingCounter::get, equalTo(1));

    await().atMost(5, TimeUnit.SECONDS)
            .until(testListener.orderDispatchedCounter::get, equalTo(1));
}

```
**2. `KafkaTemplate` should use generics**

Current:
```java
@Autowired
private KafkaTemplate kafkaTemplate;
```

Better:
```
@Autowired
private KafkaTemplate<String, Object> kafkaTemplate;
```

This improves type safety.

**3. Listener methods can be `public` for clarity**

They work even as package-private, but many teams prefer explicit visibility.

Example:
```java
public void receiveDispatchPreparing(...)
```

4. Could validate payload content, not only count

Right now the test only checks that one message arrived.

It does not verify:

correct orderId
expected field values
business correctness

A stronger version could store the received payload and assert its content.

How the full flow likely works
Expected business flow

The intention seems to be:

1. Test sends OrderCreated to order.created
2. Application consumes OrderCreated
3. Application produces DispatchPreparing to dispatch.tracking
4. Application produces OrderDispatched to order.dispatched
5. Test listener consumes both outbound messages
6. Counters confirm both messages were emitted

So testOrderDispatchFlow() should ideally validate that complete chain.

Why your setUp() method is critical

This line is one of the most important parts of the class:

ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic())

Without it, tests may randomly fail because the listener was not ready when the message was sent.

That kind of issue causes flaky Kafka tests.

So this setup is a very good practice.

In simple words

This class is testing Kafka messaging in a real Spring test environment.

It:

starts Spring Boot
starts an embedded Kafka broker
sends Kafka events
listens to Kafka output topics
counts received messages
verifies asynchronous message delivery

The listener-only tests are correct and useful.

The main flow test is currently incomplete because it sends the input event but does not check the resulting output events.

Recommended final version of the main flow test
@Test
public void testOrderDispatchFlow() throws Exception {
    OrderCreated orderCreated =
            TestEventData.buildOrderCreatedEvent(randomUUID(), "my-item");

    sendMessage(ORDER_CREATED_TOPIC, orderCreated);

    await().atMost(5, TimeUnit.SECONDS)
            .until(testListener.dispatchPreparingCounter::get, equalTo(1));

    await().atMost(5, TimeUnit.SECONDS)
            .until(testListener.orderDispatchedCounter::get, equalTo(1));
}
Final takeaway
This class mainly demonstrates
Spring Boot integration testing
Kafka integration testing with embedded Kafka
listener readiness handling
asynchronous verification with Awaitility
end-to-end event flow validation
Most important improvement

Make testOrderDispatchFlow() actually assert the expected downstream Kafka outputs.

That is what turns it into a true integration test of the dispatch flow.

If you want, I can also convert this into a line-by-line explanation table with columns like Code | Meaning | Why used | Important note.