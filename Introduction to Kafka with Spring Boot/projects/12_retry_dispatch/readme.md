

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-contract-wiremock</artifactId>
    <version>4.0.1</version>
    <scope>test</scope>
</dependency>
```

### 🧪 Spring Cloud Contract WireMock — Summary Table
| Item              | Description                          |
| ----------------- | ------------------------------------ |
| Library           | spring-cloud-contract-wiremock       |
| Purpose           | Mock external HTTP services in tests |
| Scope             | test (used only during testing)      |
| Based on          | WireMock server                      |
| Use case          | Integration testing of REST clients  |
| Starts server     | Automatically in Spring Boot tests   |
| Port              | Random or configurable               |
| No real API call  | Yes — replaces real service          |
| Common annotation | `@AutoConfigureWireMock`             |


---

```java

@Slf4j
@Component
public class StockServiceClient {

    private final RestTemplate restTemplate;

    private final String stockServiceEndpoint;

    public StockServiceClient(@Autowired RestTemplate restTemplate, @Value("${dispatch.stockServiceEndpoint}") String stockServiceEndpoint) {
        this.restTemplate = restTemplate;
        this.stockServiceEndpoint = stockServiceEndpoint;
    }

    /**
     * The stock service returns true if item is available, false otherwise.
     */
    public String checkAvailability(String item) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(stockServiceEndpoint+"?item="+item, String.class);
            if (response.getStatusCodeValue() != 200) {
                throw new RuntimeException("error " + response.getStatusCodeValue());
            }
            return response.getBody();
        } catch (HttpServerErrorException | ResourceAccessException e) {
            log.warn("Failure calling external service", e);
            throw new RetryableException(e);
        } catch (Exception e) {
            log.error("Exception thrown: " + e.getClass().getName(), e);
            throw e;
        }
    }
}
```

## 📦 StockServiceClient — Summary
### 🎯 Purpose

Calls **external Stock Service** using `RestTemplate` to check item availability.

#### 🧱 Class Overview

| Component              | Purpose                 |
| ---------------------- | ----------------------- |
| `@Component`           | Spring managed bean     |
| `@Slf4j`               | Enables logging         |
| `RestTemplate`         | Calls external REST API |
| `stockServiceEndpoint` | External service URL    |
| `checkAvailability()`  | Calls stock service     |


## 🔧 Dependency Injection
```java
public StockServiceClient(
    RestTemplate restTemplate,
    @Value("${dispatch.stockServiceEndpoint}") String stockServiceEndpoint)
```    

| Dependency           | From            | Purpose      |
| -------------------- | --------------- | ------------ |
| RestTemplate         | Spring Bean     | HTTP calls   |
| stockServiceEndpoint | application.yml | External URL |

## 🌐 External Call

```java
restTemplate.getForEntity(
    stockServiceEndpoint + "?item=" + item,
    String.class
);
```

Example:
```bash
http://stock-service/stock?item=iphone
```

Response:
```bash
true
```
## 🔄 Method Flow

```bash
checkAvailability()
        ↓
Call external service
        ↓
Check HTTP status
        ↓
Return response
        ↓
Handle exceptions
```

## 📋 Method Summary

| Step | Code            | Description              |
| ---- | --------------- | ------------------------ |
| 1    | call REST       | Calls stock service      |
| 2    | check status    | Must be 200              |
| 3    | return body     | "true" or "false"        |
| 4    | retryable error | throw RetryableException |
| 5    | other error     | rethrow                  |


## ⚠️ Exception Handling

**Retryable Exceptions**

```java
catch (HttpServerErrorException | ResourceAccessException e)
```
| Exception                | Meaning                   | Action      |
| ------------------------ | ------------------------- | ----------- |
| HttpServerErrorException | 5xx error                 | retry       |
| ResourceAccessException  | timeout / connection fail | retry       |
| Result                   | RetryableException        | Kafka retry |


**Non-Retryable Exception**
```java
catch (Exception e)
```

| Exception       | Action           |
| --------------- | ---------------- |
| Any other error | fail immediately |


## 🧪 Example Response Handling
**Case 1 — Success**

```java
HTTP 200
Body = true
```

Return:
```
true
```
**Case 2 — Server Error**

```java
HTTP 500
```
Throws:
```bash
RetryableException
```
**Case 3 — Timeout**
```java
Connection timeout
```
Throws:
```bash
RetryableException
```

## 🔁 Why RetryableException?

Used by:
- Kafka Retry
- Spring Retry
- Error Handler
- DLT logic

```bash
RetryableException
     ↓
Retry
     ↓
Retry
     ↓
Retry
     ↓
DLT
```

## 📊 Return Value

| Response  | Meaning            |
| --------- | ------------------ |
| `"true"`  | Item available     |
| `"false"` | Item not available |


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
