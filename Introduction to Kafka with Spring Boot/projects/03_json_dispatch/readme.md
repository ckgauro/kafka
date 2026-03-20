Here is a **summary table** explaining the Maven dependency `jackson-datatype-jsr310` in a clean, readable format.

```xml
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

## 📦 Dependency Summary Table

| Item               | Value                                                          | Explanation                                                  |
| ------------------ | -------------------------------------------------------------- | ------------------------------------------------------------ |
| Dependency Name    | `jackson-datatype-jsr310`                                      | Jackson module for Java 8 Date & Time API                    |
| Group ID           | `com.fasterxml.jackson.datatype`                               | Official Jackson datatype modules group                      |
| Artifact ID        | `jackson-datatype-jsr310`                                      | Module for Java Time (JSR-310) support                       |
| Used With          | `Jackson ObjectMapper`                                         | Enables serialization/deserialization of Java 8 time classes |
| Required For       | `LocalDate`, `LocalDateTime`, `Instant`, `ZonedDateTime`, etc. | Jackson cannot handle these by default                       |
| Problem Without It | JSON conversion error                                          | Jackson cannot parse Java 8 date/time types                  |
| Common Error       | `Cannot deserialize value of type java.time.LocalDateTime`     | Happens when module not added                                |
| Typical Use Case   | Spring Boot / Kafka / REST API / JSON messages                 | Needed when DTO has Java time fields                         |
| Java Spec          | JSR-310                                                        | Java 8 Date and Time API specification                       |
| Register Needed?   | Yes (if not auto-configured)                                   | `objectMapper.registerModule(new JavaTimeModule())`          |

---
Here is the **summary table** for your Spring Kafka YAML configuration.

## 📊 Spring Kafka Configuration Summary

```yaml
spring:
  application:
    name: dispatch
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      properties:
        spring:
          deserializer:
            value:
              delegate:
                class: org.springframework.kafka.support.serializer.JsonDeserializer
          json:
            trusted:
              packages: "*"
            value:
              default:
                type: dev.lydtech.dispatch.message.OrderCreated
```                

| Section                                                                   | Property                                  | Value                                                  | Purpose                                               |
| ------------------------------------------------------------------------- | ----------------------------------------- | ------------------------------------------------------ | ----------------------------------------------------- |
| spring.application                                                        | name                                      | dispatch                                               | Sets the Spring Boot application name                 |
| spring.kafka                                                              | bootstrap-servers                         | localhost:9092                                         | Kafka broker address where producer/consumer connects |
| spring.kafka.consumer                                                     | value-deserializer                        | ErrorHandlingDeserializer                              | Wrapper deserializer to handle errors safely          |
| spring.kafka.consumer.properties.spring.deserializer.value.delegate.class | JsonDeserializer                          | Actual deserializer used to convert JSON → Java object |                                                       |
| spring.kafka.consumer.properties.spring.json.trusted.packages             | "*"                                       | Allows deserialization from all packages               |                                                       |
| spring.kafka.consumer.properties.spring.json.value.default.type           | dev.lydtech.dispatch.message.OrderCreated | Default class used when converting JSON message        |                                                       |

----

## Here is the summary table explanation for your @KafkaListener code.

### 📊 KafkaListener Annotation Summary
```java
   @KafkaListener(
            id="orderConsumerClient",
            topics="order.created",
            groupId = "dispatch.order.created.consumer"
    )
    public void listen(OrderCreated payload){
        log.info("Received message: payload: {}", payload);
        dispatchService.process(payload);
    }
```

| Part                    | Value                           | Meaning               | Purpose                             |
| ----------------------- | ------------------------------- | --------------------- | ----------------------------------- |
| Annotation              | @KafkaListener                  | Spring Kafka listener | Marks method as Kafka consumer      |
| id                      | orderConsumerClient             | Listener container ID | Unique name for this consumer       |
| topics                  | order.created                   | Kafka topic name      | Topic to read messages from         |
| groupId                 | dispatch.order.created.consumer | Consumer group        | Group that this consumer belongs to |
| method                  | listen(...)                     | Listener method       | Called when message arrives         |
| parameter               | OrderCreated payload            | Message object        | JSON converted to Java object       |
| log.info                | payload logged                  | Logging message       | Shows received message              |
| dispatchService.process | method call                     | Business logic        | Processes received order            |



---
## 🧪 Testing

### 1. Run Dispatch Application

Start the Spring Boot dispatch service normally run from IDE. 
or
```bash
./mvnw spring-boot:run
```
---

### 2. Connect to Kafka container

Open terminal and connect to Kafka docker container.

```bash
docker exec -it kafka1 bash
```

---

### 3. Run Kafka Producer

Inside container run:

```bash
kafka-console-producer --bootstrap-server kafka1:19092 --topic order.created
```

---

### 4. Send Test Messages

Type messages and press ENTER.

```text
>{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book"}
>{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book1"}       
>{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book2"} 
```

---

### 5. Verify Dispatch Application Logs

In dispatch application console you should see logs:

```text
Received message: payload: OrderCreated(orderId=550e8400-e29b-41d4-a716-446655440000, item=book)
Received message: payload: OrderCreated(orderId=550e8400-e29b-41d4-a716-446655440000, item=book1)
Received message: payload: OrderCreated(orderId=550e8400-e29b-41d4-a716-446655440000, item=book2)
```

---

### 6. Expected Flow

```bash
Producer → Kafka Topic (order.created) → OrderCreateHandler → DispatchService → Log
```

----

## 7. Now lets pass wrong json


### 1. Run Kafka Producer

Inside container run:

```bash
kafka-console-producer --bootstrap-server kafka1:19092 --topic order.created
```

---

### 2. Send Test Messages

Type messages and press ENTER.

```text
>{"orderId":"1234","item":"book4"}
```

---

### 3. Verify Dispatch Application Logs

In dispatch application console you should see logs: Error occurred while deserializing message
Could not parse message

```text
It will throws error indicating it could not parse
```

### 4. Offset Behavior After Error
Even when an error occurs, Kafka may still move to the next offset.

| Concept                   | Meaning                    | Explanation                   |
| ------------------------- | -------------------------- | ----------------------------- |
| Offset                    | Message position in topic  | Each message has index        |
| Offset increase           | Next message pointer moves | Consumer skips failed message |
| ErrorHandlingDeserializer | Handles error safely       | Prevents consumer crash       |
| Consumer continues        | Reads next message         | Application keeps running     |


