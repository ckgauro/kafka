https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/tree/02-consume

https://cotiviti.udemy.com/course/introduction-to-kafka-with-spring-boot/learn/lecture/38164904?start=135#overview

# Dealing with Kafka Conundrums in the Real World

This section collates a number of the Kafka articles related to this course put together by 
the Lydtech team as downloadable PDFs, to help you deal with real world problems in the Kafka ecosystem.

## - Kafka JSON Serialization

Demonstrating serialization and deserialization of JSON formatted Kafka messages using Spring Boot with Spring Kafka.



## -  Kafka Poison Pill

What is a poison pill in the context of Apache Kafka? What are the effects and how can they be prevented?


## - Kafka Consume & Produce - Spring Boot Demo

Demonstrating Kafka message consume and produce with a Spring Boot application.



## - Kafka Consume & Produce - Testing

Comprehensive guide to unit, integration, and component testing a Kafka Spring Boot application.


## - Kafka Consume & Produce - At-Least-Once Delivery

Understanding the meaning and ramifications of the at-least-once delivery guarantees provided by Kafka.


## - Kafka Consumer Group Rebalance - Part 1 of 2

The role and impact of consumer groups rebalances, triggers for a rebalance, and the configuration options that impact rebalances.



## -  Kafka Consumer Group Rebalance - Part 2 of 2

The rebalance strategies available, the ability to reduce the number of unnecessary rebalances with Static Group Membership, and the risks to consider with rebalancing.


## - Kafka Keys, Partitions and Message Ordering

Uncovering how Kafka guarantees message ordering based on the relationship between message keys and topic partitions.


## - Kafka Topics: Fat Pipe or Thin Pipe?

Investigating whether a topic should be used for more than one event type.


## - Kafka Consumer Retry

Recovering from transient errors using stateless and stateful retry.



## - Kafka Non-Blocking Retry - Spring Retry Topics

Demonstrating a generic non-blocking consumer retry pattern.

## - Kafka Message Batch Consumer Retry

Uncovering the consumer retry behaviour when failures occur in the middle of processing a batch of messages.


## - Resources for this lecture


## - To Start Kafka service
```bash

bin/kafka-server-start config/kraft/server.properties
```

## Automatic Topic Creation in Kafka

| Topic | Description |
|--------|------------|
| Automatic Topic Creation | Kafka can create a topic automatically when a producer or consumer tries to use a topic that does not exist. |
| When it happens | When a message is sent to a non-existing topic OR a consumer subscribes to a non-existing topic. |
| Controlled by | Broker configuration and Consumer configuration flags. |
| Recommended in production | Usually disabled to avoid accidental topic creation. |

---

## 1. Broker Configuration

| Property | Value |
|----------|--------|
| Config Name | `auto.create.topics.enable` |
| Location | Kafka Broker (`server.properties`) |
| Default Value | `true` |
| Meaning | Broker will automatically create topic if it does not exist |
| Example | If producer sends to `order.created`, broker creates topic automatically |

### Example (server.properties)

```properties
auto.create.topics.enable=true
```
| Property | Value |
|----------|--------|
| If true | Topics created automatically |
| If false | Topic must be created manually |

## 2. Consumer Configuration
| Property      | Value                                                                 |
| ------------- | --------------------------------------------------------------------- |
| Config Name   | `allow.auto.create.topics`                                            |
| Location      | Consumer config                                                       |
| Default Value | `true`                                                                |
| Meaning       | Consumer allows broker to create topic automatically when subscribing |

### Example (Spring / Consumer config)
```properties
spring.kafka.consumer.allow-auto-create-topics=true
```

| Property | Value |
|----------|--------|
| If true | Consumer can trigger topic creation |
| If false | Consumer fails if topic does not exist |


## 3. How Automatic Topic Creation Works
| Step | Action                                        |
| ---- | --------------------------------------------- |
| 1    | Producer sends message to topic               |
| 2    | Topic does not exist                          |
| 3    | Broker checks `auto.create.topics.enable`     |
| 4    | If true → topic created                       |
| 5    | Consumer can also trigger creation if allowed |

## 4. Example Scenario
| Situation                                  | Result                      |
| ------------------------------------------ | --------------------------- |
| Producer sends to `order.created`          | Topic created automatically |
| Consumer subscribes to `payment.completed` | Topic created automatically |
| Broker flag false                          | Error if topic missing      |
| Consumer flag false                        | Error if topic missing      |

## 5. Why disable in Production
| Reason              | Explanation                    |
| ------------------- | ------------------------------ |
| Avoid mistakes      | Typo creates wrong topic       |
| Control partitions  | Manual creation allows config  |
| Control replication | Needed for production clusters |
| Security            | Prevent unwanted topics        |

## 6. Production Recommendation
| Environment | Setting |
| ----------- | ------- |
| Development | true    |
| Testing     | true    |
| Production  | false   |

Example production setup:

```properties
auto.create.topics.enable=false
spring.kafka.consumer.allow-auto-create-topics=false
```

## 7. Manual Topic Creation (Recommended)
```bash
kafka-topics.sh \
--create \
--topic order.created \
--bootstrap-server localhost:9092 \
--partitions 3 \
--replication-factor 1
```

| Advantage            | Reason                  |
| -------------------- | ----------------------- |
| Full control         | partitions, replication |
| No accidental topics | safer                   |
| Better performance   | optimized config        |

