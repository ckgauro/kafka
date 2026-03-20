```xml
<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-kafka</artifactId>
		</dependency>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-kafka-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
```

| # | Dependency                | Group ID                 | Artifact ID                    | Scope             | Purpose                               | When Used                                               |
| - | ------------------------- | ------------------------ | ------------------------------ | ----------------- | ------------------------------------- | ------------------------------------------------------- |
| 1 | Spring Boot Kafka Starter | org.springframework.boot | spring-boot-starter-kafka      | default (compile) | Provides Kafka support in Spring Boot | Used for Producer / Consumer / KafkaTemplate / Listener |
| 2 | Lombok                    | org.projectlombok        | lombok                         | optional          | Reduces boilerplate code              | Used for getters, setters, constructors, logs           |
| 3 | Kafka Test Starter        | org.springframework.boot | spring-boot-starter-kafka-test | test              | Provides testing utilities for Kafka  | Used only during unit / integration testing             |

### 📊 Detailed Explanation Table
| Dependency                     | What it adds                              | Why needed                                 | Example Usage                 |
| ------------------------------ | ----------------------------------------- | ------------------------------------------ | ----------------------------- |
| spring-boot-starter-kafka      | Kafka client + Spring Kafka + auto config | To connect Spring Boot with Kafka broker   | @KafkaListener, KafkaTemplate |
| lombok                         | Annotation based code generation          | Avoid writing getters/setters manually     | @Data, @Builder, @Slf4j       |
| spring-boot-starter-kafka-test | Embedded Kafka + test utils               | For testing Kafka apps without real broker | @EmbeddedKafka                |
------
Here is the **short summary table explanation** of your `application.yml` config.

```yaml
spring:
  application:
    name: dispatch
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```
### ⚙️ Configuration Summary Table
| # | Property                                 | Value              | Meaning                 | Purpose                         |
| - | ---------------------------------------- | ------------------ | ----------------------- | ------------------------------- |
| 1 | spring.application.name                  | dispatch           | Name of Spring Boot app | Used for logs / microservice id |
| 2 | spring.kafka.bootstrap-servers           | localhost:9092     | Kafka broker address    | Connects app to Kafka           |
| 3 | spring.kafka.consumer.value-deserializer | StringDeserializer | Converts bytes → String | Used when reading messages      |

----

Here is the **short summary table explanation** of your Spring class.

```java
@Service
public class DispatchService {
    public void process(String payload){
        //no-op
    }
}
```

### 📊 Class Summary Table

| # | Part            | Type       | Meaning             | Purpose                        |
| - | --------------- | ---------- | ------------------- | ------------------------------ |
| 1 | @Service        | Annotation | Spring Service Bean | Marks class as service layer   |
| 2 | DispatchService | Class      | Service class       | Contains business logic        |
| 3 | process()       | Method     | Public method       | Handles message / payload      |
| 4 | String payload  | Parameter  | Input data          | Data received from Kafka / API |
| 5 | //no-op         | Comment    | No operation        | Method does nothing            |

-----

Here is the **short summary table explanation** of your Kafka listener class.

```java
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderCreateHandler {
    private final DispatchService dispatchService;

    @KafkaListener(
            id="orderConsumerClient",
            topics="order.created",
            groupId = "dispatch.order.created.consumer"
    )
    public void listen(String payload){
        log.info("Received message: payload: {}", payload);
        dispatchService.process(payload);
    }
}
```

### 📊 Class Summary Table

| # | Part                     | Type       | Meaning                  | Purpose                            |
| - | ------------------------ | ---------- | ------------------------ | ---------------------------------- |
| 1 | @Component               | Annotation | Spring Bean              | Register class in Spring container |
| 2 | @Slf4j                   | Lombok     | Logger created           | For logging                        |
| 3 | @RequiredArgsConstructor | Lombok     | Constructor auto created | Inject dependencies                |
| 4 | OrderCreateHandler       | Class      | Kafka handler            | Receives messages                  |
| 5 | dispatchService          | Field      | Service bean             | Process message                    |
| 6 | @KafkaListener           | Annotation | Kafka consumer           | Listen to topic                    |
| 7 | listen()                 | Method     | Consumer method          | Called when message arrives        |

### 📊 KafkaListener Config Table

| Property | Value                           | Meaning        |
| -------- | ------------------------------- | -------------- |
| id       | orderConsumerClient             | Consumer id    |
| topics   | order.created                   | Topic name     |
| groupId  | dispatch.order.created.consumer | Consumer group |

### 📊 What happens when message comes

| Step | Action                           |
| ---- | -------------------------------- |
| 1    | Kafka sends message              |
| 2    | Topic = order.created            |
| 3    | Listener triggered               |
| 4    | listen() called                  |
| 5    | log printed                      |
| 6    | dispatchService.process() called |

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
Hello
This is test from CKG
Order123 created
```

---

### 5. Verify Dispatch Application Logs

In dispatch application console you should see logs:

```text
Received message: payload: Hello
Received message: payload: This is test from CKG
Received message: payload: Order123 created
```

---

### 6. Expected Flow

```bash
Producer → Kafka Topic (order.created) → OrderCreateHandler → DispatchService → Log
```