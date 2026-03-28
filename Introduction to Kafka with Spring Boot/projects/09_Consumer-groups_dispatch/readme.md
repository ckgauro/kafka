# Testing:

##  Testing Notes — Shared Consumer Kafka Consumer Group Behavior

## 1. Start Two Application Instances

Run two instances of the same Spring Boot application to observe consumer group behavior.

### Run first application (App)

```bash
mvn spring-boot:run
```
Console output:
```bash
dispatch.order.created.consumer: partitions assigned: []
```
Comment:
- No partition assigned yet.
- Consumer is waiting for partition assignment.

### Run second application (App2)
```bash
mvn spring-boot:run
```

Console output:
```bash
dispatch.order.created.consumer: partitions assigned: [order.created-0]
```
Comment:
- Topic currently has only **one partition**
- Kafka assigns the partition to only **one consumer in the group**
- Other consumer remains idle

Important note:
> In a consumer group, one partition can be consumed by only one consumer.

Check logs carefully:
- Each application instance has a different client id
- Both share the same group id
- Partition assigned to only one consumer

### 2. Run Kafka Console Consumer

Use console consumer to monitor messages from the output topic.

```bash
kafka-console-consumer \
--bootstrap-server localhost:9092 \
--topic order.dispatched
```

Comment:
- This consumer listens to dispatched orders
- Useful to verify processing result

### 3. Run Kafka Console Producer

Send test message to input topic.
```bash
kafka-console-producer \
--bootstrap-server localhost:9092 \
--topic order.created
```
Send message:
```json
{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book-5"}
```
Comment:
- Message is produced to `order.created`
- Only one consumer in the group processes it
- Result should appear in `order.dispatched`

### 4. Observation
| Scenario           | Result                                |
| ------------------ | ------------------------------------- |
| One partition      | Only one consumer active              |
| Multiple consumers | Idle consumers if no extra partitions |
| Same group id      | Shared consumption                    |
| Different group id | Duplicate consumption                 |
| Console consumer   | Used for verification                 |
| Console producer   | Used for testing                      |


###  To view Consumer groups

```bash
kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group dispatch.order.created.consumer
```
**Explanation**

This command is used to **inspect the status of a Kafka consumer group.**

- **kafka-consumer-groups**
> Tool to manage and view consumer group details.
- **--bootstrap-server kafka1:9092**
> Connects to the Kafka broker running at kafka1 on port 9092.
- **--describe**
> Displays detailed information about the consumer group.
- **--group dispatch.order.created.consumer**
> Specifies the consumer group to analyze.

**output**

```bash
GROUP                           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                                                     HOST            CLIENT-ID
dispatch.order.created.consumer order.created   0          30              30              0               consumer-dispatch.order.created.consumer-1-aa22b9a3-7150-44ea-9a6d-1573f7ca397b /192.168.65.1   consumer-dispatch.order.created.consumer-1
```


### 1. Field Explanation
| Field          | Meaning                           |
| -------------- | --------------------------------- |
| GROUP          | Consumer group name               |
| TOPIC          | Topic being consumed              |
| PARTITION      | Partition number                  |
| CURRENT-OFFSET | Last offset consumed by the group |
| LOG-END-OFFSET | Latest offset available in Kafka  |
| LAG            | Messages yet to be consumed       |
| CONSUMER-ID    | Unique consumer instance ID       |
| HOST           | Consumer machine IP               |
| CLIENT-ID      | Client identifier                 |

### 2. Given Output Interpretation
| Metric         | Value                           | Meaning                   |
| -------------- | ------------------------------- | ------------------------- |
| Group          | dispatch.order.created.consumer | Active consumer group     |
| Topic          | order.created                   | Source topic              |
| Partition      | 0                               | Only one partition        |
| Current Offset | 30                              | Consumed up to message 30 |
| Log End Offset | 30                              | Latest message also 30    |
| Lag            | 0                               | No pending messages       |
| Consumer ID    | Unique ID                       | Active consumer instance  |
| Host           | /192.168.65.1                   | Running machine           |
| Client ID      | consumer-dispatch...            | Consumer client           |

### 3. Key Insight
- **Lag = 0 → Consumer is fully caught up**
- Only **one partition → one active consumer**
- Offsets match → no backlog
- System is healthy and processing in real time


--------

# Kafka Consumer Failover (Fault Tolerance) — Testing Notes

This guide explains how to test **Kafka Consumer Failover** using two Spring Boot applications and Kafka CLI tools.

---

## 🎯 Objective

- Verify that Kafka **automatically reassigns partitions**
- Ensure **fault tolerance** when a consumer goes down
- Observe **consumer group behavior**

---

## 🚀 Step 1: Run Two Applications

Start two instances of your Spring Boot application (same codebase).

### ▸ Run App1
```bash
mvn spring-boot:run
```

### ▸ Run App2
```bash
mvn spring-boot:run
```

**✔ What to Check**
- Look at the console logs
- You should see different consumer IDs under the same group
- Both apps should join the same consumer group

## 👀 Step 2: Monitor Consumers (Optional)

Use Kafka console consumer to see messages being consumed:
```bash
kafka-console-consumer \
--bootstrap-server localhost:9092 \
--topic order.dispatched
```

✔ This shows processed messages

## 📤 Step 3: Produce Messages

Send test messages using Kafka console producer:
```bash
kafka-console-producer \
--bootstrap-server localhost:9092 \
--topic order.created
```

Example Message
```json
{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book-6"}
```

✔ This triggers your consumer applications

## 🔄 Step 4: Test Failover

**▸ Stop One Application (App2)**
- Manually stop App2 (Ctrl + C)

**✔ Expected Behavior**
- Kafka detects missing heartbeat
- Consumer is marked as dead
- **Rebalance** happens automatically
- Remaining app (App1) takes over all partitions

## 🔁 Step 5: Restart Application

Restart App2:
```bash
mvn spring-boot:run
```

**✔ Expected Behavior**
- App2 rejoins the consumer group
- Kafka triggers rebalance
- Partitions redistributed between App1 and App2

## 🧠 What You Are Verifying
| Scenario          | Expected Result                 |
| ----------------- | ------------------------------- |
| Two apps running  | Load shared across consumers    |
| One app stopped   | Other app takes over processing |
| App restarted     | Partitions rebalanced           |
| Messages produced | Always consumed (no loss)       |


## ⚙️ Key Kafka Concepts Tested
- Consumer Groups
- Partition Assignment
- Heartbeat Mechanism
- Rebalancing
- Fault Tolerance

## ✅ Final Outcome
- System continues processing even if one consumer fails
- No manual intervention required
- Kafka ensures high **availability and reliability**

You can verify this behavior using the following command. During a rebalance, the **consumer ID is reassigned to a new consumer**, indicating that partition ownership has been transferred successfully.

```bash
kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group dispatch.order.created.consumer
```

# Kafka Duplicate Consumption – Testing Notes

## 🎯 Objective
To verify how Kafka handles **duplicate message consumption** when multiple consumer groups are used.

---

## ⚙️ Setup Overview
We will run **two instances of the same application**, each with a **different consumer group ID**, and observe how both consume the same message independently.

---

## 🚀 Step 1: Run Two Application Instances

### ▶️ App1 (Default Group ID)
```bash
mvn spring-boot:run
```

### ▶️ App2 (Modified Group ID)

Update the consumer group ID:

> 📁 dev.lydtech.dispatch.handler.OrderCreatedHandler.java
```java
groupId = "dispatch.order.created.consumer2"
```
Then run:
```bash
mvn spring-boot:run
```

✅ Now both applications are running with **different consumer group IDs**

## 📤 Step 2: Produce a Message

Run Kafka producer:
```bash
kafka-console-producer --bootstrap-server localhost:9092 --topic order.created
```
Send message:
```json
{"orderId":"550e8400-e29b-41d4-a716-446655440100","item":"book-100"}
```

### 📥 Step 3: Consume Messages (order.tracking)

```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic order.tracking
```
Output:
```json
{"orderId":"550e8400-e29b-41d4-a716-446655440100"}
{"orderId":"550e8400-e29b-41d4-a716-446655440100"}
```

📌 Same message appears twice because both apps processed it.

### 📥 Step 4: Consume Messages (order.dispatched)
```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic order.dispatched
```

Output:
```json
{"orderId":"550e8400-e29b-41d4-a716-446655440100","processedById":"8c71696c-7413-473f-8052-837d477e11e9","notes":"Dispatched: book-100"}
{"orderId":"550e8400-e29b-41d4-a716-446655440100","processedById":"1bc458ba-bc08-48c4-b328-1b77a6058dcd","notes":"Dispatched: book-100"}
```

## 🔍 Key Observation

Each application processes the same message independently and generates a `unique processedById`.

## 🧠 Why This Happens

Each application uses a **different consumer group**, so Kafka treats them as **independent consumers**.

## 👉 Kafka behavior:

- Same **group ID** → messages shared (load balancing)
- Different **group IDs** → messages duplicated (each group gets full copy)

## 🆔 Application Identifier

Each app generates its own unique ID:

```java
private static final UUID APPLICATION_ID = randomUUID();
```

## 📊 Output Comparison
**App1**
```bash
Sent messages:
orderId: 550e8400-e29b-41d4-a716-446655440100
processedById: 1bc458ba-bc08-48c4-b328-1b77a6058dcd
```

**App2**

```bash
Sent messages:
orderId: 550e8400-e29b-41d4-a716-446655440100
processedById: 8c71696c-7413-473f-8052-837d477e11e9
```

### ✅ Conclusion
- Both applications consume the same message because they belong to **different consumer groups**
- This leads to **duplicate processing**
- Each instance generates its own **processedById**, confirming independent processing

##
💡 Key Takeaway
> Kafka ensures **at-least-once delivery per consumer group**, not globally across all consumers.

# Kafka Consumer Group Status – Short Notes

## 📊 Command Used
```bash
kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group <group-id>
``

```bash
[appuser@kafka1 ~]$ kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group dispatch.order.created.consumer

GROUP                           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                                                     HOST            CLIENT-ID
dispatch.order.created.consumer order.created   0          30              30              0               consumer-dispatch.order.created.consumer-1-f51ad09c-5c09-474a-8935-59657595e141 /192.168.65.1   consumer-dispatch.order.created.consumer-1
[appuser@kafka1 ~]$ kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group dispatch.order.created.consumer2

GROUP                            TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                                                      HOST            CLIENT-ID
dispatch.order.created.consumer2 order.created   0          30              30              0               consumer-dispatch.order.created.consumer2-1-866a809b-0a62-4674-85f6-fcb9fb642972 /192.168.65.1   consumer-dispatch.order.created.consumer2-1
```

# Kafka Consumer Group Status – Summary Table

## 📊 Consumer Group Comparison

| Field                | dispatch.order.created.consumer | dispatch.order.created.consumer2 | Explanation |
|---------------------|--------------------------------|----------------------------------|------------|
| **Group ID**        | dispatch.order.created.consumer | dispatch.order.created.consumer2 | Two separate consumer groups (App1 & App2) |
| **Topic**           | order.created                  | order.created                    | Both consuming the same topic |
| **Partition**       | 0                              | 0                                | Only one partition in the topic |
| **Current Offset**  | 30                             | 30                               | Both have consumed up to message offset 30 |
| **Log End Offset**  | 30                             | 30                               | Total messages available = 30 |
| **Lag**             | 0                              | 0                                | No pending messages (fully consumed) |
| **Consumer ID**     | Unique (consumer-1-...)        | Unique (consumer2-1-...)         | Each app instance has its own consumer instance |
| **Host**            | /192.168.65.1                  | /192.168.65.1                    | Both running on same machine |
| **Client ID**       | consumer-dispatch...-1         | consumer-dispatch...-1           | Kafka client identifier per app |

---

## 🔍 Key Insights

| Concept | Explanation |
|--------|------------|
| **Duplicate Consumption** | Both groups consumed the same 30 messages independently |
| **Offset Tracking** | Kafka maintains offsets **separately per consumer group** |
| **No Lag** | Both consumers are fully caught up (no backlog) |
| **Independent Processing** | Each app processes messages without affecting the other |
| **Same Topic, Different Groups** | This is why duplication occurs |

---

## 🧠 Important Concept

> Kafka does **not share offsets across consumer groups**

- Group 1 → Offset = 30  
- Group 2 → Offset = 30  
✔ Both read all messages separately  

---

## ✅ Final Conclusion

- Even though both apps read the **same topic and partition**,  
  they behave as **independent consumers** because of different group IDs  

- This confirms:
  - ✔ Duplicate processing is expected  
  - ✔ Kafka ensures delivery **per consumer group**  
  - ✔ System is healthy (Lag = 0)

---