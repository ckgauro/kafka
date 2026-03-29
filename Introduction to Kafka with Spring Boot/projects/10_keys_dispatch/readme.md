# Testing:

##  Testing Notes — Single Kafka Consumer Group Behavior

## 1. Start  Application Instances

Run two instances of the same Spring Boot application to observe consumer group behavior.

### Run first application (App)

```bash
mvn spring-boot:run
```
Console output:
```bash
dispatch.order.created.consumer: partitions assigned: []
```


# Consumer running
- kafka-console-consumer --bootstrap-server kafka1:9092 --topic order.dispatched --property print.key=true --property key.separator=:

# Producer running
kafka-console-producer --bootstrap-server kafka1:9092 --topic order.created --property parse.key=true --property key.separator=:
>"456":{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book-456"} 

Check application console

>Sent messages: key: 456  - orderId:550e8400-e29b-41d4-a716-446655440000 - processedById :f5d0d044-8ea3-405f-b25e-b59392fa764a




## to list consumer groups
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

## TO describe consumer groups of given group

```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group dispatch.order.created.consumer
```


-----

# Now lets increase partition from 1 to 5
## To describe partition
```bash
bin/kafka-topics.sh --bootstarp-server localhost:9092 --describe --topic order.created
```

## To Alter partition
```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --alter --topic order.created --partitions 5
```

## To describe partition
```bash
bin/kafka-topics.sh --bootstarp-server localhost:9092 --describe --topic order.created
```
Run two Application:

App1, and App2 and check console




## to list consumer groups
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

### TO describe consumer groups of given group
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group dispatch.order.created.consumer
```

Consumer running
kafka-console-consumer --bootstrap-server localhost:9092 --topic order.dispatched --property print.key=true --property key.separator=:
Producer running
kafka-console-producer --bootstrap-server localhost:9092 --topic order.created --property parse.key=true -- property key.separator=:
"456":{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book-456"}

Check application console

"456":{"orderId":"550e8400-e29b-41d4-a716-446655440001","item":"book-456"} "456":{"orderId":"550e8400-e29b-41d4-a716-446655440002","item":"book-456"}

"789":{"orderId":"550e8400-e29b-41d4-a716-446655440009","item":"book-789"} "789":{"orderId":"550e8400-e29b-41d4-a716-446655440007","item":"book-789"}


---

Now Stop App1