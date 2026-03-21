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