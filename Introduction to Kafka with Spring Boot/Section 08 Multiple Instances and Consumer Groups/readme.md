https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/tree/09-consumer-groups

Consumer Groups
- Throughput
    - multiple consumers
    - Parallel Processing
- Fault Tolerance
    - heartbeat
    - Pool Interval

    - New consumer
    - Dead consumer
    - Lost consumer

- Scaling
- Heartbeating
- Rebalancing
    - Heartbeat
    - Polling Interval
    - Rebalancing Strategy
    - Pause in Processing

Monitor tool for Kafka

--- 

PlantUML diagram

Why Consumer Groups? 
- Isolation or Parallelism
- Distinct Processing Logic
- Throughput
- Fault Tolerance

Consumer Group Exercise

- Shared consumer Group
- Consumer Failover
- Duplicate Consumption


- https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/blob/09-consumer-groups/src/main/java/dev/lydtech/dispatch/message/OrderDispatched.java

- https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/blob/09-consumer-groups/src/main/java/dev/lydtech/dispatch/service/DispatchService.java


Testing:

Run 2 application:
- run Application (App)
    mvn spring-boot:run

- run Application (App2)
    mvn spring-boot:run    
- Check in console at last there will find different group id


# Consumer running
- kafka-console-consumer --bootstrap-server localhost:9092 --topic order.dispatched

# Producer running
- kafka-console-producer --bootstrap-server localhost:9092 --topic order.created
>{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book-5"} 


# Demo Consumer Failover

Testing:

Run 2 application: Stop it
- run Application (App1)
    mvn spring-boot:run

- run Application (App2)
    mvn spring-boot:run    
- Check in console at last there will find different group id


# Consumer running
- kafka-console-consumer --bootstrap-server localhost:9092 --topic order.dispatched

# Producer running
- kafka-console-producer --bootstrap-server localhost:9092 --topic order.created
>{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book-6"} 


# Duplicate Consumption


Testing:

Run 2 application: Stop it
- run Application (App1)
    mvn spring-boot:run

- https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/blob/09-consumer-groups/src/main/java/dev/lydtech/dispatch/handler/OrderCreatedHandler.java
- run Application (App2)
 groupId = "dispatch.order.created.consumer2",
    mvn spring-boot:run    
- Check in console at last there will find different group id


# Consumer running
- kafka-console-consumer --bootstrap-server localhost:9092 --topic order.dispatched

# Producer running
- kafka-console-producer --bootstrap-server localhost:9092 --topic order.created
>{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book-7"} 

proceessByID

