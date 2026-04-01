




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

**output**
```bash
dispatch.order.created.consumer
console-consumer-41373
console-consumer-95726
dispatch.order.created.consumer2
console-consumer-13198
```


## TO describe consumer groups of given group

```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group dispatch.order.created.consumer
```

**output**
```bash
GROUP                           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                                                     HOST            CLIENT-ID
dispatch.order.created.consumer order.created   0          46              46              0               consumer-dispatch.order.created.consumer-1-aa326c0a-d351-42ab-a40c-49a43610bf1b /192.168.65.1   consumer-dispatch.order.created.consumer-1

```



-----

# Now lets increase partition from 1 to 5
## To describe partition
```bash
bin/kafka-topics.sh --bootstarp-server localhost:9092 --describe --topic order.created
```
 **output**
 ```bash
 Topic: order.created    TopicId: RfCmltvjThagIZ-TsqlTjw PartitionCount: 1       ReplicationFactor: 1    Configs: 
        Topic: order.created    Partition: 0    Leader: 1       Replicas: 1     Isr: 1
```        

## To Alter partition
```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --alter --topic order.created --partitions 5
```


## To describe partition
```bash
bin/kafka-topics.sh --bootstarp-server localhost:9092 --describe --topic order.created
```

**outbput**
```bash
Topic: order.created    TopicId: RfCmltvjThagIZ-TsqlTjw PartitionCount: 5       ReplicationFactor: 1    Configs: 
        Topic: order.created    Partition: 0    Leader: 1       Replicas: 1     Isr: 1
        Topic: order.created    Partition: 1    Leader: 1       Replicas: 1     Isr: 1
        Topic: order.created    Partition: 2    Leader: 1       Replicas: 1     Isr: 1
        Topic: order.created    Partition: 3    Leader: 1       Replicas: 1     Isr: 1
        Topic: order.created    Partition: 4    Leader: 1       Replicas: 1     Isr: 1
```


Run two Application:

App1 
```
mvn spring-boot:run
```

In console 
```bash
 dispatch.order.created.consumer: partitions assigned: [order.created-0, order.created-1, order.created-2, order.created-3, order.created-4]
```

Now run another App2
```bash
 dispatch.order.created.consumer: partitions assigned: [order.created-3, order.created-4]
```

 and check back to the App1 console. It automatically re balance

 ```bash
 dispatch.order.created.consumer: partitions assigned: [order.created-0, order.created-1, order.created-2]
 ```




## to list consumer groups
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --list
```
output
```bash
dispatch.order.created.consumer
console-consumer-41373
console-consumer-95726
dispatch.order.created.consumer2
console-consumer-13198
```

### TO describe consumer groups of given group
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group dispatch.order.created.consumer
```

```bash

GROUP                           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                                                     HOST            CLIENT-ID
dispatch.order.created.consumer order.created   0          46              46              0               consumer-dispatch.order.created.consumer-1-1560b722-d0d5-4e00-b501-75fe86b3353d /192.168.65.1   consumer-dispatch.order.created.consumer-1
dispatch.order.created.consumer order.created   1          0               0               0               consumer-dispatch.order.created.consumer-1-1560b722-d0d5-4e00-b501-75fe86b3353d /192.168.65.1   consumer-dispatch.order.created.consumer-1
dispatch.order.created.consumer order.created   2          0               0               0               consumer-dispatch.order.created.consumer-1-1560b722-d0d5-4e00-b501-75fe86b3353d /192.168.65.1   consumer-dispatch.order.created.consumer-1
dispatch.order.created.consumer order.created   3          0               0               0               consumer-dispatch.order.created.consumer-1-33b1284e-2a1e-449a-92ff-c9dd9ac5e343 /192.168.65.1   consumer-dispatch.order.created.consumer-1
dispatch.order.created.consumer order.created   4          0               0               0               consumer-dispatch.order.created.consumer-1-33b1284e-2a1e-449a-92ff-c9dd9ac5e343 /192.168.65.1   consumer-dispatch.order.created.consumer-1
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

"1":{"orderId":"550e8400-e29b-41d4-a716-446655440001","item":"book-456"} 
"2":{"orderId":"550e8400-e29b-41d4-a716-446655440001","item":"book-456"} 
"3":{"orderId":"550e8400-e29b-41d4-a716-446655440001","item":"book-456"} 
"4":{"orderId":"550e8400-e29b-41d4-a716-446655440001","item":"book-456"} 
"5":{"orderId":"550e8400-e29b-41d4-a716-446655440001","item":"book-456"} 
"6":{"orderId":"550e8400-e29b-41d4-a716-446655440001","item":"book-456"} 