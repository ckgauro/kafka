## application.yaml

```yaml
spring:
  application:
    name: dispatch
  kafka:
    bootstrap-servers: localhost:9092
```

## 📊 Summary Table
| Property            | Full Key                         | Meaning                                   | Why it is used                            | Example value    |
| ------------------- | -------------------------------- | ----------------------------------------- | ----------------------------------------- | ---------------- |
| `spring`            | root key                         | Main configuration prefix for Spring Boot | All Spring settings start from here       | —                |
| `application`       | `spring.application`             | Application-level configuration           | Used to define app identity               | —                |
| `name`              | `spring.application.name`        | Name of the Spring Boot app               | Used in logs, microservices, monitoring   | `dispatch`       |
| `kafka`             | `spring.kafka`                   | Kafka configuration section               | Used to configure Kafka producer/consumer | —                |
| `bootstrap-servers` | `spring.kafka.bootstrap-servers` | Kafka broker address                      | Tells Spring where Kafka is running       | `localhost:9092` |

-----
# DispatchConfiguration Class Explanation (Spring Boot + Kafka)

This class is a **Kafka Consumer configuration class** in Spring Boot.  
It manually configures Kafka instead of using only `application.yaml`.

The class defines:

- Kafka enablement
- Kafka consumer properties
- ConsumerFactory bean
- KafkaListenerContainerFactory bean
- JSON message conversion

---

## ✅ Full Class


```java

@EnableKafka
@Configuration
public class DispatchConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setRecordMessageConverter(new StringJacksonJsonMessageConverter());
        return factory;
    }
}
```

### 1. @EnableKafka
**Meaning**

Enables Kafka listener support in Spring.
| Annotation   | Meaning                  |
| ------------ | ------------------------ |
| @EnableKafka | Enables `@KafkaListener` |
| Required for | Kafka consumer           |
| Without it   | Listener will not work   |

**Example**

```java

@KafkaListener(topics = "order.created")
public void listen(String message) {}
```

Without `@EnableKafka` → this will not run.

### 2. @Configuration
**Meaning**

Marks this class as Spring configuration class.
| Annotation     | Meaning              |
| -------------- | -------------------- |
| @Configuration | Defines Spring beans |
| Similar to     | XML config           |
| Used for       | Manual configuration |

Spring will scan this class and create beans.

### 3. @Value("${spring.kafka.bootstrap-servers}")
**Meaning**

Reads value from ``application.yaml``

```yaml
application.yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

**Code**
```java
@Value("${spring.kafka.bootstrap-servers}")
private String bootstrapServers;
```
| Part                           | Meaning       |
| ------------------------------ | ------------- |
| ${}                            | read property |
| spring.kafka.bootstrap-servers | key           |
| bootstrapServers               | variable      |

Result:
```java
bootstrapServers = localhost:9092
```
Used to connect Kafka.

### 4. consumerFactory()
**Purpose**

Creates Kafka ConsumerFactory bean.

ConsumerFactory is used to create Kafka consumers.

**Method**
```java
@Bean
public ConsumerFactory<String, Object> consumerFactory()

```

| Part            | Meaning                |
| --------------- | ---------------------- |
| @Bean           | Spring bean            |
| ConsumerFactory | Kafka consumer creator |
| <String,Object> | key,value type         |


`ConsumerConfig` is a **Kafka class that contains configuration keys (constants)** used to configure a Kafka consumer.

It comes from: `org.apache.kafka.clients.consumer.ConsumerConfig`

### 1. What is ConsumerConfig

| Item | Meaning |
|-------|---------|
| ConsumerConfig | Kafka configuration class |
| Package | org.apache.kafka.clients.consumer |
| Contains | static constant keys |
| Used for | Consumer properties |
| Used inside | props.put(...) |


**Properties Map**
```java
Map<String, Object> props = new HashMap<>();
```

Kafka configuration stored here.

**BOOTSTRAP SERVERS**
```java
props.put(
 ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
 bootstrapServers
);
```
| Config                   | Meaning        |
| ------------------------ | -------------- |
| BOOTSTRAP_SERVERS_CONFIG | Kafka address  |
| bootstrapServers         | localhost:9092 |

Kafka will connect to broker.

**Key Deserializer**

Kafka does NOT send **String** or **Object** directly. Kafka sends **bytes**, so we must deserialize them.

```java
props.put(
 ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
 StringDeserializer.class
);

```

| Config             | Meaning                |
| ------------------ | ---------------------- |
| KEY_DESERIALIZER   | how to read key        |
| StringDeserializer | convert bytes → String |

Kafka sends bytes → must convert.

**Value Deserializer**
```java
props.put(
 ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
 StringDeserializer.class
);

```
| Config             | Meaning           |
| ------------------ | ----------------- |
| VALUE_DESERIALIZER | how to read value |
| StringDeserializer | read as String    |

Message comes as JSON string.

**Return Factory**
```java
return new DefaultKafkaConsumerFactory<>(props);
```
Creates Kafka consumer factory.

Flow:
```bash
props → ConsumerFactory → Kafka Consumer
```

### 5. kafkaListenerContainerFactory()
**Purpose**

Creates listener container factory.

# ConcurrentKafkaListenerContainerFactory Explanation (Spring Kafka)

`ConcurrentKafkaListenerContainerFactory` is a **Spring Kafka class used to create listener containers** for methods annotated with `@KafkaListener`.

It connects: `@KafkaListener → ContainerFactory → ConsumerFactory → Kafka Broker`

It is required when using Kafka consumers in Spring Boot.

---

## 1. Full Class Name


org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory


| Part | Meaning |
|-------|----------|
| Concurrent | supports multiple threads |
| KafkaListener | used for @KafkaListener |
| Container | manages consumer |
| Factory | creates container |

---

## 2. Why this class is needed

When you write:

```java
@KafkaListener(topics = "order.created")
public void listen(String msg) {}
```

Spring must create internally:
- Kafka consumer
- thread
- listener container
- message converter

**What is Listener Container**

Listener container = object that:
- runs Kafka consumer
- listens to topic
- calls @KafkaListener
- manages threads
- converts message

Flow:
```bash
Kafka
 ↓
Consumer
 ↓
Listener Container
 ↓
@KafkaListener method
```

Required for:
```java
@KafkaListener
```
Spring uses this internally.

**Method**

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Object>
kafkaListenerContainerFactory()
```

| Part                          | Meaning          |
| ----------------------------- | ---------------- |
| Concurrent                    | multi-thread     |
| KafkaListenerContainerFactory | listener creator |
| <String,Object>               | key,value        |


**Create Factory**

```java
ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
```
Creates listener container.

This manages:
- threads
- consumer
- message
- conversion

**Set ConsumerFactory**
```java
factory.setConsumerFactory(consumerFactory());
```

Connects consumer to listener.

Flow:

```bash
Listener → ContainerFactory → ConsumerFactory → Kafka
```

**Message Converter**
```java
factory.setRecordMessageConverter(
    new StringJacksonJsonMessageConverter()
);
```

Very important.

Converts JSON → Object.

Without this → error
```bash
Cannot convert from String to Object
```
**Why converter needed**

Kafka message:
```json
{"orderId":"123","item":"book"}
```
Java expects:
```java
OrderCreated object
```

Converter does:
```java
JSON → Java Object
```

### 6. Bean Flow Diagram
```bash
application.yaml
      ↓
@Value
      ↓
bootstrapServers
      ↓
consumerFactory()
      ↓
Kafka Consumer
      ↓
kafkaListenerContainerFactory()
      ↓
@KafkaListener
      ↓
Your method
```


---

#### 1. Overall Flow Summary

| Step | Component | Type | Purpose | Output |
|-------|----------|--------|----------|---------|
| 1 | application.yaml | Config file | Define Kafka settings | bootstrap server |
| 2 | @Value | Annotation | Read config value | String variable |
| 3 | bootstrapServers | Variable | Store Kafka address | used in config |
| 4 | consumerFactory() | Bean | Create consumer config | ConsumerFactory |
| 5 | Kafka Consumer | Kafka object | Connect to broker | read messages |
| 6 | kafkaListenerContainerFactory() | Bean | Create listener container | ContainerFactory |
| 7 | @KafkaListener | Annotation | Listen to topic | triggers method |
| 8 | Your method | Java method | Process message | business logic |

---

#### 2. application.yaml → @Value

| Item | Code | Meaning | Result |
|-------|--------|----------|---------|
| YAML config | spring.kafka.bootstrap-servers | Kafka address | localhost:9092 |
| Annotation | @Value("${spring.kafka.bootstrap-servers}") | read property | variable filled |
| Variable | bootstrapServers | store value | used later |

Result: `bootstrapServers = localhost:9092`


---

#### 3. bootstrapServers → consumerFactory()

| Step | Code | Purpose | Result |
|-------|--------|----------|---------|
| create map | new HashMap<>() | store config | props |
| set server | BOOTSTRAP_SERVERS_CONFIG | Kafka address | connect broker |
| set key deserializer | KEY_DESERIALIZER | bytes → String | key readable |
| set value deserializer | VALUE_DESERIALIZER | bytes → String | value readable |
| create factory | DefaultKafkaConsumerFactory | create consumer | ConsumerFactory |

Result: `ConsumerFactory created`


---

#### 4. consumerFactory → Kafka Consumer

| Item | Meaning |
|--------|----------|
| ConsumerFactory | creates consumer |
| KafkaConsumer | reads messages |
| Broker | Kafka server |
| Topic | message source |

Flow: `ConsumerFactory → KafkaConsumer → Kafka Broker`

Result: `Consumer ready to read messages`


---

#### 5. consumerFactory → kafkaListenerContainerFactory()

| Step | Code | Purpose | Result |
|-------|--------|----------|---------|
| create factory | ConcurrentKafkaListenerContainerFactory | listener manager | container |
| set consumer | setConsumerFactory | attach consumer | container can read |
| set converter | setRecordMessageConverter | JSON → Object | no error |

Result: `Listener container ready`


---

#### 6. ContainerFactory → @KafkaListener

| Item | Meaning |
|--------|----------|
| ContainerFactory | manages listener |
| ListenerContainer | runs consumer |
| @KafkaListener | method trigger |
| Topic | source |

Flow: `Container → Listener → Method`


Result: `Listener active`


---

#### 7. @KafkaListener → Your method

| Step | What happens |
|-------|-------------|
| Kafka sends message | JSON |
| Consumer reads | String |
| Converter converts | Object |
| Listener calls | method |
| Method runs | logic |

Example: `Kafka → String → Object → method`


---

#### 8. Full Pipeline Table

| Order | Stage | Responsible | What happens |
|--------|---------|-------------|-------------|
| 1 | Config file | application.yaml | define server |
| 2 | Read config | @Value | load value |
| 3 | Store value | variable | bootstrapServers |
| 4 | Create consumer | consumerFactory | config consumer |
| 5 | Connect Kafka | KafkaConsumer | ready |
| 6 | Create container | ContainerFactory | manage listener |
| 7 | Listen topic | @KafkaListener | wait message |
| 8 | Run method | your code | process message |

---

#### 9. One-look Summary

| Config | Creates | Used by |
|---------|---------|-----------|
| application.yaml | value | @Value |
| @Value | variable | consumerFactory |
| consumerFactory | consumer | containerFactory |
| containerFactory | listener | @KafkaListener |
| @KafkaListener | call method | your code |

---

#### 10. One-line summary

Spring reads Kafka config from application.yaml, creates a consumer, bu



### 7. How @KafkaListener uses this
```java
@KafkaListener(topics = "order.created")
public void listen(OrderCreated event) {
}
```

Flow:
```bash
Kafka → ConsumerFactory
      → ContainerFactory
      → Converter
      → Listener
```

### 8. Why manual config instead of yaml

Used when:
| Reason              | Why    |
| ------------------- | ------ |
| Custom deserializer | JSON   |
| Custom converter    | needed |
| Multiple consumers  | needed |
| Advanced config     | needed |


### 9. Summary Table      

| Part             | Role             |
| ---------------- | ---------------- |
| @EnableKafka     | enable Kafka     |
| @Configuration   | config class     |
| @Value           | read yaml        |
| consumerFactory  | create consumer  |
| props            | kafka settings   |
| deserializer     | convert bytes    |
| containerFactory | listener factory |
| messageConverter | JSON → Object    |



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