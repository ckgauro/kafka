https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/tree/06-produce

## To create Topics from Java
- Automatic topic creation
Flag:
- Broker config: auto.create.topics
    - Default: true
- Consumer config: allow.auto.create.topics
    - Default: true

https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/blob/06-produce/src/main/java/dev/lydtech/dispatch/DispatchConfiguration.java

- https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/blob/06-produce/src/main/java/dev/lydtech/dispatch/message/OrderDispatched.java

- https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/blob/06-produce/src/main/java/dev/lydtech/dispatch/service/DispatchService.java
- https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/blob/06-produce/src/main/java/dev/lydtech/dispatch/handler/OrderCreatedHandler.java


mvn clean install 
- 5 tests


---------

Consume Using Cli

run application
producer
- kafka-console-producer --bootstrap-servers localhost 9092 --topic order.created
{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book10"} 

consumer
- kafka-console-consumer --bootstrap-servers localhost 9092 --topic order.dispatched








