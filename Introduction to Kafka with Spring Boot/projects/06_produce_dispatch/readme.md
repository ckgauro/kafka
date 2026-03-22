- https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/tree/06-produce

- https://github.com/ckgauro/kafka/blob/main/Introduction%20to%20Kafka%20with%20Spring%20Boot/projects/06_produce_dispatch/src/main/java/dev/lydtech/dispatch/config/DispatchConfiguration.java

```java
@EnableKafka
@Configuration
public class DispatchConfiguration {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreated> kafkaListenerContainerFactory(
            ConsumerFactory<String, OrderCreated> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, OrderCreated> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderCreated> consumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class);
        config.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, OrderCreated.class.getCanonicalName());
        config.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "dev.lydtech.dispatch.message");

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
```

## Detailed Explanation of `DispatchConfiguration`

This class is a **Spring Kafka configuration class**.  
Its job is to tell Spring:

- how to **consume** Kafka messages
- how to **deserialize** incoming JSON into Java objects
- how to **produce** Kafka messages
- how to create the `KafkaTemplate` used to send messages

---

## Full Purpose of This Class

| Part | Purpose |
|------|---------|
| `@EnableKafka` | Enables Kafka listener support in Spring |
| `@Configuration` | Tells Spring this class contains bean definitions |
| `kafkaListenerContainerFactory()` | Creates the listener container factory used by `@KafkaListener` |
| `consumerFactory()` | Defines how consumers connect to Kafka and deserialize messages |
| `producerFactory()` | Defines how producers connect to Kafka and serialize messages |
| `kafkaTemplate()` | Creates a helper object used to send messages to Kafka |

---

## 1. Class-Level Annotations

### `@EnableKafka`

| Item | Explanation |
|------|-------------|
| Purpose | Activates Kafka listener processing in Spring |
| Why needed | Without it, `@KafkaListener` methods may not be detected and managed properly |
| What it enables | Spring creates the infrastructure needed to listen to Kafka topics |

### `@Configuration`

| Item | Explanation |
|------|-------------|
| Purpose | Marks this class as a Spring configuration class |
| Why needed | Spring scans this class and registers the methods annotated with `@Bean` |
| Result | Each bean becomes part of the Spring application context |

---

## 2. `kafkaListenerContainerFactory(...)`

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, OrderCreated> kafkaListenerContainerFactory(
        ConsumerFactory<String, OrderCreated> consumerFactory) {

    ConcurrentKafkaListenerContainerFactory<String, OrderCreated> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
}
```

#### What this bean does

This bean creates the **listener container factory** used by Spring Kafka to run consumers behind the scenes.

When you write a method like this:

```java
@KafkaListener(topics = "order.created", groupId = "dispatch.order.created.consumer")
public void listen(OrderCreated event) {
    ...
}
```
Spring uses the `ConcurrentKafkaListenerContainerFactory` to:

1. create consumer threads
2. connect to Kafka
3. poll records
4. convert records into Java objects
5. call your listener method

**Summary Table**

| Item       | Explanation                                                     |
| ---------- | --------------------------------------------------------------- |
| Bean type  | `ConcurrentKafkaListenerContainerFactory<String, OrderCreated>` |
| Key type   | `String`                                                        |
| Value type | `OrderCreated`                                                  |
| Main role  | Builds listener containers for `@KafkaListener` methods         |
| Uses       | `ConsumerFactory<String, OrderCreated>`                         |

Why `ConcurrentKafkaListenerContainerFactory`?

| Feature              | Meaning                                              |
| -------------------- | ---------------------------------------------------- |
| Listener support     | Required for `@KafkaListener`                        |
| Concurrency support  | Can run multiple consumer threads if configured      |
| Consumer integration | Uses the `ConsumerFactory` to create Kafka consumers |

Right now your code uses the default behavior, but later you can configure:
- concurrency
- error handlers
- retry logic
- acknowledgment mode
- record filtering

Example:
```java
factory.setConcurrency(3);
```

This would allow multiple consumer threads.

## 3. consumerFactory(...)
```java
@Bean
public ConsumerFactory<String, OrderCreated> consumerFactory(
        @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class);
    config.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, OrderCreated.class.getCanonicalName());
    config.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "dev.lydtech.dispatch.message");

    return new DefaultKafkaConsumerFactory<>(config);
}
```

This bean defines **how Kafka consumers are created**.

A `ConsumerFactory` is basically a factory object that Spring uses whenever it needs a new Kafka consumer.

### 1. Step-by-Step Breakdown of Consumer Configuration

`@Value("${spring.kafka.bootstrap-servers}")`

| Item          | Explanation                                              |
| ------------- | -------------------------------------------------------- |
| Purpose       | Reads Kafka broker addresses from application properties |
| Example value | `localhost:9092`                                         |
| Why needed    | Consumer must know where the Kafka cluster is            |

Example property:

```properties
spring.kafka.bootstrap-servers=localhost:9092
```

`Map<String, Object> config = new HashMap<>();`

This map stores all Kafka consumer configuration properties.

Kafka expects configuration as key-value pairs.

`BOOTSTRAP_SERVERS_CONFIG`
```java
config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
```
| Item       | Explanation                                    |
| ---------- | ---------------------------------------------- |
| Config key | `bootstrap.servers`                            |
| Purpose    | Tells consumer where Kafka brokers are running |
| Example    | `localhost:9092`                               |



This is the first connection point to the Kafka cluster.

`KEY_DESERIALIZER_CLASS_CONFIG`
```java
config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
```

| Item       | Explanation                                                 |
| ---------- | ----------------------------------------------------------- |
| Config key | `key.deserializer`                                          |
| Purpose    | Converts Kafka message key bytes into Java `String`         |
| Why needed | Kafka stores data as bytes, but your app wants Java objects |



So if a Kafka record key is stored as bytes, `StringDeserializer` converts it back into a Java `String`.


### 2. Why Use ErrorHandlingDeserializer?

`VALUE_DESERIALIZER_CLASS_CONFIG`
```java
config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
```

This is one of the most important lines.

Instead of directly using a JSON deserializer, you are using:
- outer deserializer: `ErrorHandlingDeserializer`
- inner real deserializer: `JacksonJsonDeserializer`


| Reason                | Explanation                                                |
| --------------------- | ---------------------------------------------------------- |
| Safer consumption     | Prevents hard crashes when deserialization fails           |
| Better error handling | Wraps deserialization errors so Spring can manage them     |
| Production-friendly   | Useful when bad JSON or invalid messages appear in a topic |


If you directly used `JacksonJsonDeserializer` and Kafka received malformed JSON, the consumer could fail badly.

With `ErrorHandlingDeserializer`, deserialization exceptions are captured and handled more safely.

**Inner value deserializer**
```java
config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class);
```

This tells `ErrorHandlingDeserializer`:
> "Use `JacksonJsonDeserializer` as the real deserializer for the message value."

So the flow is:

| Step | Action                                         |
| ---- | ---------------------------------------------- |
| 1    | Kafka receives bytes from topic                |
| 2    | `ErrorHandlingDeserializer` gets the bytes     |
| 3    | It delegates to `JacksonJsonDeserializer`      |
| 4    | JSON is converted into `OrderCreated`          |
| 5    | If conversion fails, error is wrapped properly |


### 3. VALUE_DEFAULT_TYPE
```java
config.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, OrderCreated.class.getCanonicalName());
```

| Item       | Explanation                                                       |
| ---------- | ----------------------------------------------------------------- |
| Purpose    | Tells the JSON deserializer which Java type to create             |
| Value      | Fully qualified class name of `OrderCreated`                      |
| Why needed | Kafka message payload is JSON, so deserializer needs target class |


If message JSON is:
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "item": "book1"
}
```

Then it is converted into:

```java
OrderCreated orderCreated = new OrderCreated(...);
```

Why `getCanonicalName`()?

Because the deserializer needs the full class name, for example:
```java
dev.lydtech.dispatch.message.OrderCreated
```

### 4. TRUSTED_PACKAGES

```java
config.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "dev.lydtech.dispatch.message");
```

This is a security-related setting.

| Item            | Explanation                                                    |
| --------------- | -------------------------------------------------------------- |
| Purpose         | Tells deserializer which packages are safe to deserialize from |
| Why needed      | Prevents unsafe deserialization of arbitrary classes           |
| Current package | `dev.lydtech.dispatch.message`                                 |

Kafka JSON deserialization can be risky if it is allowed to create objects from any class.
So Spring Kafka requires trusted packages.

**Good practice**

| Value            | Meaning                       |
| ---------------- | ----------------------------- |
| Specific package | Safer and recommended         |
| `*`              | Trust all packages, less safe |



Your configuration is good because it trusts only the message package.

### 5. Returning DefaultKafkaConsumerFactory
```java
return new DefaultKafkaConsumerFactory<>(config);
```

This creates the actual Spring Kafka consumer factory using the configuration map.

| Item         | Explanation                        |
| ------------ | ---------------------------------- |
| Factory type | `DefaultKafkaConsumerFactory`      |
| Role         | Creates Kafka `Consumer` instances |
| Used by      | Listener container factory         |


So the chain is:

| Component          | Uses                    |
| ------------------ | ----------------------- |
| `@KafkaListener`   | uses listener container |
| listener container | uses `ConsumerFactory`  |
| `ConsumerFactory`  | creates Kafka consumers |


## 4. producerFactory(...)

```java
@Bean
public ProducerFactory<String, Object> producerFactory(
        @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {

    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

    return new DefaultKafkaProducerFactory<>(config);
}
```

This bean defines how Kafka producers are created.

A `ProducerFactory` is used whenever your application sends messages to Kafka.
### 1. Step-by-Step Breakdown of Producer Configuration

`BOOTSTRAP_SERVERS_CONFIG`
```java
config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
```

| Item       | Explanation                                    |
| ---------- | ---------------------------------------------- |
| Config key | `bootstrap.servers`                            |
| Purpose    | Tells producer where Kafka brokers are located |


Producer also needs the Kafka broker address to send records.

`KEY_SERIALIZER_CLASS_CONFIG`
```java
config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
```

| Item       | Explanation                                                   |
| ---------- | ------------------------------------------------------------- |
| Config key | `key.serializer`                                              |
| Purpose    | Converts Java `String` key into bytes before sending to Kafka |


Kafka sends data as bytes, so producer must serialize Java objects first.

`VALUE_SERIALIZER_CLASS_CONFIG`
```java
config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
```

| Item       | Explanation                          |
| ---------- | ------------------------------------ |
| Config key | `value.serializer`                   |
| Purpose    | Converts Java object into JSON bytes |
| Used for   | Message payload                      |

So if you send:
```java
OrderCreated event = OrderCreated.builder()
        .orderId(UUID.randomUUID())
        .item("book1")
        .build();
```

`JacksonJsonSerializer` JacksonJsonSerializer uses Jackson ObjectMapper to:
1. Convert Java object → JSON
2. Convert JSON → byte[]
3. Kafka sends byte[]

Example JSON produced:
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "item": "book1"
}
```

Returning `DefaultKafkaProducerFactory`
```java
return new DefaultKafkaProducerFactory<>(config);
```

This builds the producer factory that Spring will use to create Kafka producers.


## 5.  kafkaTemplate(...)
```java
@Bean
public KafkaTemplate<String, Object> kafkaTemplate(
        ProducerFactory<String, Object> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
}
```

This bean creates the `KafkaTemplate`.

`KafkaTemplate` is the main helper class used in Spring Kafka to send records.

**Summary Table**

| Item          | Explanation                       |
| ------------- | --------------------------------- |
| Bean type     | `KafkaTemplate<String, Object>`   |
| Role          | Sends messages to Kafka           |
| Uses          | `ProducerFactory<String, Object>` |
| Common method | `send(topic, value)`              |

Example use:
```java
kafkaTemplate.send("order.created", orderCreated);
```

What happens internally:
| Step | Action                                      |
| ---- | ------------------------------------------- |
| 1    | Application calls `kafkaTemplate.send(...)` |
| 2    | `KafkaTemplate` uses producer factory       |
| 3    | Producer serializes key and value           |
| 4    | Record is sent to Kafka broker              |



| Class                     | Direction                | Meaning          |
| ------------------------- | ------------------------ | ---------------- |
| JacksonJsonSerializer     | Object → JSON → bytes    | Used by Producer |
| JacksonJsonDeserializer   | bytes → JSON → Object    | Used by Consumer |
| ErrorHandlingDeserializer | wrapper for deserializer | handles errors   |

## End-to-End Flow in Your Configuration

**Producer Side**
| Step | Action                                          |
| ---- | ----------------------------------------------- |
| 1    | App creates `OrderCreated` object               |
| 2    | `KafkaTemplate` sends it                        |
| 3    | `JacksonJsonSerializer` converts object to JSON |
| 4    | Kafka broker stores JSON message in topic       |


**Consumer Side**

| Step | Action                                               |
| ---- | ---------------------------------------------------- |
| 1    | Consumer polls message from topic                    |
| 2    | Key bytes become `String` using `StringDeserializer` |
| 3    | Value bytes go through `ErrorHandlingDeserializer`   |
| 4    | It delegates to `JacksonJsonDeserializer`            |
| 5    | JSON becomes `OrderCreated` object                   |
| 6    | `@KafkaListener` receives `OrderCreated`             |


## Why This Configuration Is Good
| Good point                         | Why it matters                 |
| ---------------------------------- | ------------------------------ |
| Explicit consumer config           | Easy to understand and control |
| Explicit producer config           | Avoids hidden defaults         |
| JSON serialization/deserialization | Good for Java object messaging |
| ErrorHandlingDeserializer          | Safer for bad data             |
| Trusted packages specified         | Better security                |
| Typed listener container           | Clear expected payload type    |

----------
## OrderCreateHandler – Detailed Explanation (Kafka + Spring Boot)

This class is a **Kafka Consumer Handler** that listens to messages from Kafka topic  
`order.created` and sends them to `DispatchService` for processing.


```java

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderCreateHandler {

    private final DispatchService dispatchService;

//    public OrderCreateHandler(DispatchService dispatchService) {
//        this.dispatchService = dispatchService;
//    }

    @KafkaListener(
            id="orderConsumerClient",
            topics="order.created",
            groupId = "dispatch.order.created.consumer",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(OrderCreated payload) throws Exception{
        log.info("Received message: payload: {}", payload);
        try{
            dispatchService.process(payload);
        }catch (Exception e){
            log.error("Processing failure : {}",e);
        }
       // dispatchService.process(payload);
    }
}
```

### 1. Class Level Annotations

| Annotation                 | Meaning            | Why used                                                 |
| -------------------------- | ------------------ | -------------------------------------------------------- |
| `@Component`               | Spring Bean        | So Spring can detect and create this class automatically |
| `@RequiredArgsConstructor` | Lombok constructor | Creates constructor for final fields                     |
| `@Slf4j`                   | Lombok logger      | Creates `log` object for logging                         |


### 2. @Component

| Feature                      | Explanation                             |
| ---------------------------- | --------------------------------------- |
| Marks class as Spring Bean   | Spring will create object automatically |
| Enables dependency injection | Other beans can be injected             |
| Needed for Kafka listener    | Listener only works inside Spring bean  |


Example
```java
@Component
public class OrderCreateHandler {}
```

Without this → Kafka listener will not run

### 3. @RequiredArgsConstructor (Lombok)

This generates constructor automatically.

Your field
```java
private final DispatchService dispatchService;
```
Lombok generates

```java
public OrderCreateHandler(DispatchService dispatchService) {
    this.dispatchService = dispatchService;
}
```

| Benefit        | Explanation                  |
| -------------- | ---------------------------- |
| Less code      | No need to write constructor |
| Safe injection | final fields required        |
| Clean code     | Recommended in Spring        |


### 4. @Slf4j

Creates logger automatically.

Instead of
```java
Logger log = LoggerFactory.getLogger(OrderCreateHandler.class);
```

You can use

```java
log.info("message");
log.error("error");
```

| Method    | Meaning    |
| --------- | ---------- |
| log.info  | normal log |
| log.error | error log  |
| log.warn  | warning    |
| log.debug | debug      |

### 5. DispatchService Injection

```java
private final DispatchService dispatchService;
```

| Part               | Meaning                    |
| ------------------ | -------------------------- |
| final              | required dependency        |
| DispatchService    | service to process message |
| injected by Spring | via constructor            |


Flow:
```bash
Spring → creates DispatchService
Spring → creates OrderCreateHandler
Spring → injects DispatchService
```

### 6. @KafkaListener (MOST IMPORTANT)

This makes the method a Kafka Consumer
```java
@KafkaListener(...)
```
This tells Spring

Listen to Kafka topic and call this method when message arrives

#### 1. KafkaListener Parameters
**id**
```java
id="orderConsumerClient"
```

| Part               | Meaning                    |
| ------------------ | -------------------------- |
| Meaning | Consumer container id |
| Used for | managing listener |
| Optional | yes but recommended |

**topics**
```java
topics="order.created"
```

| Part               | Meaning                    |
| ------------------ | -------------------------- |
| Meaning | Topic to listen |
| Topic name | order.created |
| Must exist | yes |

Flow
```bash
Producer → order.created → Consumer → listen()
```
groupId
```bash

groupId = "dispatch.order.created.consumer"
```
| Part               | Meaning                    |
| ------------------ | -------------------------- |
| Meaning | Consumer group |
| Important | YES |
| Why | load balancing |

Example

| Consumer | Group  | Result            |
| -------- | ------ | ----------------- |
| A        | group1 | gets message      |
| B        | group1 | shares messages   |
| C        | group2 | gets all messages |


So here
```java
dispatch.order.created.consumer
```
is the consumer group name

**containerFactory**
```java
containerFactory = "kafkaListenerContainerFactory"
```

This tells Spring which factory to use.

Defined in config:
```java
@Bean
public ConcurrentKafkaListenerContainerFactory kafkaListenerContainerFactory(...)
```

Why needed?

| Reason              | Explanation   |
| ------------------- | ------------- |
| custom deserializer | JSON → Object |
| error handling      | retry         |
| concurrency         | threads       |
| manual ack          | control       |


Without correct factory
```java
Cannot convert String to OrderCreated
```
error happens.

#### 2. listen() Method

```java
public void listen(OrderCreated payload)
```

This method runs when message arrives.

Message type:
```bash
OrderCreated
```
So Kafka must deserialize JSON → OrderCreated

Example message
```json
{
  "orderId": "...",
  "item": "book1"
}
```
Converted to
> OrderCreated object

#### 3. Logging
```java
log.info("Received message: payload: {}", payload);
```

Output
```bash
Received message: payload: OrderCreated(orderId=..., item=book1)
```
Good for debugging.

#### 4. Processing

```java
dispatchService.process(payload);
```
Flow
```bash
Kafka → Handler → Service → Business logic
```

Best practice:

| Layer      | Responsibility  |
| ---------- | --------------- |
| Handler    | receive message |
| Service    | process         |
| Repository | DB              |
| Producer   | send event      |


#### 5. try / catch block

```java
try{
    dispatchService.process(payload);
}catch (Exception e){
    log.error("Processing failure : {}",e);
}
```

Why needed?

| Reason         | Explanation            |
| -------------- | ---------------------- |
| Prevent crash  | consumer keeps running |
| Log error      | easier debug           |
| retry possible | later                  |
| DLQ possible   | later                  |

Without try/catch

Consumer may stop.

#### 6. Full Flow Diagram

```bash
Producer
   ↓
Topic: order.created
   ↓
Kafka Broker
   ↓
KafkaListener
   ↓
OrderCreateHandler.listen()
   ↓
DispatchService.process()
   ↓
DB / API / Event / Log
```
-------

## DispatchService in Kafka + Spring Boot

This class is a **service layer component**. Its main job is to take an `OrderCreated` event, create a new `OrderDispatched` event from it, and publish that new event to another Kafka topic.

So in simple words:

> `DispatchService` receives an order-created event and sends an order-dispatched event to Kafka.

---

```java
@RequiredArgsConstructor
@Service
public class DispatchService {
    private static final String ORDER_DISPATCHED_TOPIC="order.dispatched";
    private final KafkaTemplate<String,Object> kafkaProducer;

//    public DispatchService(KafkaTemplate<String, Object> kafkaProducer) {
//        this.kafkaProducer = kafkaProducer;
//    }

    public void process(OrderCreated orderCreated) throws Exception{
        OrderDispatched orderDispatched= OrderDispatched.builder()
                .orderId(orderCreated.getOrderId())
                .build();
        kafkaProducer.send(ORDER_DISPATCHED_TOPIC,orderDispatched).get();

    }
}
```

### Summary Table
| Part                                 | Purpose                  | Explanation                                   |
| ------------------------------------ | ------------------------ | --------------------------------------------- |
| `@Service`                           | Marks service layer bean | Spring creates and manages this class         |
| `@RequiredArgsConstructor`           | Constructor injection    | Lombok generates constructor for final fields |
| `ORDER_DISPATCHED_TOPIC`             | Target Kafka topic       | Message will be sent to `order.dispatched`    |
| `KafkaTemplate<String, Object>`      | Kafka producer helper    | Used to publish messages to Kafka             |
| `process(OrderCreated orderCreated)` | Business logic method    | Converts input event into output event        |
| `OrderDispatched.builder()`          | Creates new event object | Uses Lombok builder pattern                   |
| `send(...).get()`                    | Sends synchronously      | Waits until Kafka acknowledges the send       |


















============


-----


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


mvn clean install 
- 5 tests


---------

Consume Using Cli

run application
producer
- kafka-console-producer --bootstrap-server localhost 9092 --topic order.created
{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book10"} 

consumer
- kafka-console-consumer --bootstrap-server localhost 9092 --topic order.dispatched








