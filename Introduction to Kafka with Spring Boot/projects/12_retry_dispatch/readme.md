



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
