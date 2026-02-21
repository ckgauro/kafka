## Kafka Topics & Partitions

# Apache Kafka Topics – Detailed Explanation

---

# 1️⃣ What is a Kafka Topic?

A **Kafka Topic** is a logical category or channel where messages (events) are published and stored.

Think of a topic as a **named event log**.

Examples:

```java
user-signup-events
order-events
payment-transactions
video-stream-events
```


Producers write data to topics.  
Consumers read data from topics.

---

# 2️⃣ Topics are Distributed Logs

A Kafka topic is internally implemented as a **distributed, append-only log**.

Key characteristics:

- Messages are appended at the end.
- Messages are immutable (cannot be modified).
- Messages are ordered within a partition.
- Each message has a unique offset.

---

# 3️⃣ Topic Partitions

## What is a Partition?

Each topic is divided into one or more **partitions**.

Example:
```java
Topic: order-events

Partition 0
Partition 1
Partition 2

```


### Why Partitions?

- Enable parallel processing
- Improve scalability
- Distribute data across brokers
- Allow high throughput

---

## Ordering Guarantee

Kafka guarantees **ordering only within a partition**, not across partitions.

Example:

Partition 0:
```java
Offset 0 → Order A
Offset 1 → Order B
Offset 2 → Order C
```

This order is guaranteed.

---

# 4️⃣ Offsets in Topics

Each record in a partition has a unique **offset**.

Offset properties:

- Sequential number
- Assigned automatically
- Used by consumers to track progress
- Offset is unique only within a partition

Example:

```java
Partition 1:
Offset 0
Offset 1
Offset 2

```

Consumers store offsets to resume consumption.

---

# 5️⃣ Replication in Topics

For fault tolerance, partitions are replicated across brokers.

## Replication Factor

Defines how many copies of a partition exist.

Example:

```java
Replication Factor = 3
```

Means:
- 1 Leader
- 2 Followers

### Leader
- Handles reads and writes.

### Followers
- Replicate data from leader.
- Take over if leader fails.

---

# 6️⃣ Topic Retention Policy

Kafka does NOT delete messages after consumption.

Messages are retained based on:

- Time (e.g., 7 days)
- Size (e.g., 100 GB)
- Log compaction

---

## Time-Based Retention

Example:
```java
retention.ms = 604800000 (7 days)
```

Messages older than 7 days are deleted.

---

## Size-Based Retention

```java
retention.bytes = 10737418240 (10 GB)
```

If log size exceeds limit, older messages are removed.

---

## Log Compaction

Instead of deleting old messages:

- Keeps latest value per key.
- Useful for state updates.

Used in:
- Change Data Capture
- Configuration topics

---

# 7️⃣ Topic Configuration Parameters

Common configurations:

| Configuration | Purpose |
|--------------|----------|
| partitions | Number of partitions |
| replication.factor | Number of replicas |
| retention.ms | Time retention |
| retention.bytes | Size retention |
| cleanup.policy | delete / compact |
| min.insync.replicas | Minimum replicas required for write |

---

# 8️⃣ Creating a Topic

Using CLI:

```java
kafka-topics.sh --create
--topic order-events
--bootstrap-server localhost:9092
--partitions 3
--replication-factor 2
```

---

# 9️⃣ Topic Naming Best Practices

- Use lowercase
- Use hyphens
- Avoid spaces
- Use clear domain-based names

Good Examples:
```java
user-events
payment-transactions
inventory-updates
```

Bad Examples:
```java
Topic1
Test
Data
```

---

# 🔟 Topic Scalability

You can:

- Increase partitions (not decrease)
- Add brokers to cluster
- Increase replication factor (with caution)

Note:
Increasing partitions changes partition distribution and can affect ordering.

---

# 1️⃣1️⃣ Topic in Event-Driven Architecture

In microservices:

Example:

```java
User Service → user-created-topic
Order Service → order-created-topic
Payment Service → payment-completed-topic
```

Each service reacts to events via topics.

This enables:

- Loose coupling
- Asynchronous communication
- Replay capability
- Scalability

---

# 1️⃣2️⃣ Internal Kafka Topics

Kafka uses internal topics such as:

```java
__consumer_offsets
```

Used for:
- Storing consumer offsets
- Group management

---

# 1️⃣3️⃣ Summary

A Kafka Topic is:

- A distributed, partitioned, replicated log
- Append-only and immutable
- Scalable and fault tolerant
- Retention-based (not queue-based)
- Core abstraction in Kafka architecture

Topics enable high-throughput, event-driven, real-time systems.