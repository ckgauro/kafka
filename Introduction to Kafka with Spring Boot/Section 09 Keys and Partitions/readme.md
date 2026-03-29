https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/tree/10-keys

[project](../projects/10_keys_dispatch)

# Kafka Ordering & Partitions — Explained

Ordering in Kafka is a very important concept when you care about **sequence of events**  
(e.g., order creation → payment → shipment must be processed in order).

---

## 1. What is Ordering?

Ordering means:

👉 Messages are consumed **in the same order they were produced**

### Important Rule

✔ Kafka guarantees ordering **ONLY within a partition**

❌ Kafka does NOT guarantee ordering across multiple partitions

---

### Example

Partition 0:
```bash
Order1 → Order2 → Order3 ✔ ordered
```

Partition 1:
```bash
Order4 → Order5 → Order6 ✔ ordered
```

Across partitions:

Order1 (P0), Order4 (P1), Order2 (P0)
❌ Not guaranteed order


---

## 2. Partitions Revisited

Partitions are the core of Kafka’s scalability and ordering.

### Key Points

- Topic = split into multiple partitions
- Each partition is:
  - Ordered
  - Immutable log
- Each message has an **offset**

---

### Why partitions matter for ordering

| Scenario | Ordering |
|--------|---------|
| Single partition | Global ordering ✔ |
| Multiple partitions | Only per-partition ordering ✔ |
| Multiple consumers | Parallel but no global order ❌ |

---

### Trade-off

A **trade-off** means:

👉 You gain one thing, but you lose something else

In systems like Kafka, you often cannot have everything at once  
(e.g., high speed + strict ordering + high scalability)

---

### Simple Example

| You Want | What You Lose |
|----------|--------------|
| High throughput (many partitions) | Strict ordering ❌ |
| Strict ordering (single partition) | Scalability ❌ |
| Fast processing (parallel) | Ordering consistency ❌ |

### Kafka Trade-off Example

| Choice | Benefit | Drawback |
|--------|--------|----------|
| Multiple partitions | High performance | No global ordering |
| Single partition | Perfect ordering | Low performance |
| Parallel consumers | Fast processing | Possible disorder |

---

## 3. What Disrupts Ordering?

Ordering can break due to several reasons.

---

### ▸ Multiple Partitions

- Messages go to different partitions
- No guarantee of order across them

---

### ▸ Multiple Producers

- Different producers send messages at same time
- Arrival order may differ

---

### ▸ Retries (Producer)

- If a send fails and retries
- Message may arrive later → out of order

---

### ▸ Max In-Flight Requests

Config:
```java
max.in.flight.requests.per.connection
```

#### 1. Recommended Settings

If you want **strict ordering**:


max.in.flight.requests.per.connection = 1
enable.idempotence = true
acks = all
retries = high


---

#### 2. Trade-off with this config

| Setting | Benefit | Drawback |
|--------|--------|----------|
| = 1 | Guaranteed ordering ✔ | Slower performance ❌ |
| > 1 | Faster throughput ✔ | Possible reordering ❌ |



This is a **Kafka producer configuration**.

---

##### What it means

👉 Number of messages the producer can send **without waiting for acknowledgment**

- If > 1:
  - Messages can be sent in parallel
  - Retry can reorder messages

---

### ▸ Rebalancing (Consumers)

- When partitions move between consumers
- Temporary disorder in processing

---

### ▸ Asynchronous Processing

- If consumer processes messages in parallel threads
- Order can break

---

## 4. How to Guarantee Ordering?

To maintain ordering, follow these rules.

---

### ✔ Use Single Partition

- All messages go to one partition
- Guarantees strict ordering

❌ Limitation:
- No scalability

---

### ✔ Use Message Keys (Best Practice)

- Kafka uses key to decide partition
- Same key → same partition

Example:
```bash
Key = CustomerID
```

Result:
- All messages for same customer go to same partition
- Ordering preserved per customer

---

### ✔ Configure Producer Properly

Important configs:

```bash
acks=all
retries=∞ (or high)
enable.idempotence=true
max.in.flight.requests.per.connection=1
```

Why:
- Prevent duplicates
- Prevent reordering during retry

---

### ✔ Avoid Parallel Processing in Consumer

- Process messages sequentially
- Or ensure ordering logic in code

---

### ✔ Handle Rebalance Carefully

- Use proper offset commits
- Avoid long processing delays

---

## 5. Keys (Very Important)

Keys are the **main mechanism** to control ordering.

---

### How keys work

Kafka uses:


partition = hash(key) % number_of_partitions


So:

✔ Same key → Same partition  
✔ Same partition → Ordered messages  

---

### Example

Key = `order-123`


order-123 → Partition 2
order-123 → Partition 2
order-123 → Partition 2


✔ Order maintained

---

### Without key

If no key:

- Kafka distributes messages randomly
- Ordering is lost

---

### Best Practice

| Use Case | Key |
|----------|-----|
| Orders | orderId |
| Payments | paymentId |
| Users | userId |

---

## Summary

| Concept | Meaning |
|--------|---------|
| Ordering | Sequence of messages |
| Guarantee | Only within partition |
| Partitions | Enable scaling but affect ordering |
| Disruptors | retries, multiple producers, rebalancing |
| Solution | Use keys + proper configs |
| Keys | Ensure same partition → ordered |

---

## Key Idea

👉 Kafka = **Partition-level ordering system**

✔ Use partitions for scale  
✔ Use keys for ordering  
✔ Balance between performance & correctness  


# @Header and @Payload in Kafka (Spring Boot) — Where to Use

These annotations are used inside **Kafka consumers** (listeners)  
to extract message metadata and actual data.

---

## 1. Where to Use Them?

👉 You use these in:

```java
@KafkaListener
```

inside your **consumer method**

---

## 2. Basic Example

```java
@KafkaListener(topics = "order.created", groupId = "order-group")
public void listen(
        @Payload OrderCreated event,
        @Header(KafkaHeaders.RECEIVED_KEY) String key,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
) {
    System.out.println("Event: " + event);
    System.out.println("Key: " + key);
    System.out.println("Partition: " + partition);
}
```

## 3. What Each Annotation Means
**▸ @Payload**

👉 Extracts the actual message (value)

Example:
```json
{
  "orderId": "123",
  "item": "laptop"
}
```

### Mapped to:
```
OrderCreated event
```
- ✔ Main business data
- ✔ Used for processing logic

### ▸ @Header(KafkaHeaders.RECEIVED_KEY)

👉 Extracts the **message key**

Example:
```bash
Key = "order-123"
```

Use cases:
- Maintain ordering
- Group related messages
- Logging/debugging

### ▸ @Header(KafkaHeaders.RECEIVED_PARTITION)

👉 Extracts the partition number

Example:
```java
Partition = 2
```

Use cases:

- Debugging
- Monitoring distribution
- Checking load balancing

## 4. When to Use Them (Real Use Cases)
**✔ 1. Ordering Logic**

If you use keys for ordering:
```java
@Header(KafkaHeaders.RECEIVED_KEY) String key
```

- ✔ Ensure same key → same partition
- ✔ Track sequence of events

**✔ 2. Debugging & Logging**

```java
System.out.println("Key: " + key + ", Partition: " + partition);
```
Helps you:

See where message came from
Debug partition imbalance

**✔ 3. Partition-Based Processing**

Sometimes logic depends on partition:
```java
if (partition == 0) {
    // special handling
}
```

(Not very common, but useful in advanced cases)

**✔ 4. Multi-Tenant / Routing Logic**

Use key to route logic:
```java
if (key.startsWith("premium")) {
    // premium customer logic
}
```

**✔ 5. Monitoring Consumer Behavior**

Track:
- Which partitions are active
- How messages are distributed

## 5. When NOT Needed

You don’t always need headers.

If you only care about data:
```java
@KafkaListener(topics = "order.created")
public void listen(OrderCreated event) {
    // only payload needed
}
```

- ✔ Simple use case
- ✔ Cleaner code

## 6. Summary @Header Parameters in Spring Kafka — Summary Table

`@Header` is used in a `@KafkaListener` to extract **Kafka message metadata**  
(like key, partition, offset, topic, timestamp, etc.)

## Common @Header Parameters (KafkaHeaders)

| Header | Type | Meaning | When to Use |
|--------|------|--------|-------------|
| `KafkaHeaders.RECEIVED_KEY` | `String / byte[]` | Message key | Ordering, grouping, routing logic |
| `KafkaHeaders.RECEIVED_PARTITION` | `int` | Partition number | Debugging, load distribution |
| `KafkaHeaders.RECEIVED_TOPIC` | `String` | Topic name | Multi-topic consumers |
| `KafkaHeaders.OFFSET` | `long` | Offset of message | Tracking progress, debugging |
| `KafkaHeaders.TIMESTAMP` | `long` | Message timestamp | Event timing, analytics |
| `KafkaHeaders.TIMESTAMP_TYPE` | `String` | Type of timestamp (CREATE_TIME / LOG_APPEND_TIME) | Time-based logic |
| `KafkaHeaders.RECEIVED_TIMESTAMP` | `long` | Time when consumer received message | Latency measurement |
| `KafkaHeaders.GROUP_ID` | `String` | Consumer group ID | Monitoring, logging |
| `KafkaHeaders.ACKNOWLEDGMENT` | `Acknowledgment` | Manual offset commit control | Advanced processing |
| `KafkaHeaders.CONSUMER` | `Consumer<?, ?>` | Kafka consumer instance | Advanced control (pause/resume) |
| `KafkaHeaders.RECEIVED_MESSAGE_KEY` *(alt name)* | `Object` | Same as key (older usage) | Backward compatibility |

---

## Example Usage

```java
@KafkaListener(topics = "order.created", groupId = "order-group")
public void listen(
        @Payload OrderCreated event,
        @Header(KafkaHeaders.RECEIVED_KEY) String key,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
) {
    System.out.println("Event: " + event);
    System.out.println("Key: " + key);
    System.out.println("Partition: " + partition);
    System.out.println("Offset: " + offset);
    System.out.println("Topic: " + topic);
}

👉 Use:
- @Payload → for business logic
- @Header → for metadata (key, partition, etc.)

Most real systems:
✔ Always use payload
✔ Use headers when you need control, debugging, or ordering insights



=====

Testing:


# Consuming Keyed Messages


Testing:

Run 2 application: Stop it
- run Application (App1)
    mvn spring-boot:run

- run Application (App2)
 groupId = "dispatch.order.created.consumer2",
    mvn spring-boot:run    
- Check in console at last there will find different group id


# Consumer running
- kafka-console-consumer --bootstrap-server localhost:9092 --topic order.dispatched --property print.key=true --property key.separator=:

# Producer running
- kafka-console-producer --bootstrap-server localhost:9092 --topic order.created --property parse.key=true -- property key.separator=:
>"123":{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book-7"} 

Check application console

Describe partition
```bash
bin/kafka-topics.sh --bootstarp-server localhost:9092 --describe --topic order.created
```

---------

# Now lets increase partition from 1 to 5

## To describe partition
```bash
bin/kafka-topics.sh --bootstarp-server localhost:9092 --describe --topic order.created
```



## To Alter partition
```bash

bin/kafka-topics.sh --bootstrap-server localhost:9092 --alter --topic order.created -- partitions 5
```

## To describe partition
```bash
bin/kafka-topics.sh --bootstarp-server localhost:9092 --describe --topic order.created
```


Testing:

Run 2 application: Stop it
- run Application (App1)
    mvn spring-boot:run

- run Application (App2)
 groupId = "dispatch.order.created.consumer2",
    mvn spring-boot:run    
- Check in console at last there will find different partition and group id




# Consumer running
- kafka-console-consumer --bootstrap-server localhost:9092 --topic order.dispatched --property print.key=true --property key.separator=:

# Producer running
- kafka-console-producer --bootstrap-server localhost:9092 --topic order.created --property parse.key=true -- property key.separator=:
>"456":{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book-456"} 

Check application console

>"456":{"orderId":"550e8400-e29b-41d4-a716-446655440001","item":"book-456"} 
>"456":{"orderId":"550e8400-e29b-41d4-a716-446655440002","item":"book-456"} 



>"789":{"orderId":"550e8400-e29b-41d4-a716-446655440009","item":"book-789"} 
>"789":{"orderId":"550e8400-e29b-41d4-a716-446655440007","item":"book-789"} 


## to list consumer groups
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

## TO describe consumer groups of given group

```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group dispatch.order.created.consumer
```

Does that same key always fix same partition?
How can we send message to given partition?
How can we send key to different partition in round ribbon?



------


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