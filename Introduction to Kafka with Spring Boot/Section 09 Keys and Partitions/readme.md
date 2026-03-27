https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/tree/10-keys

what is Ordering?
partitions revisited?
what Disrupts Ordering?
How to Guarantee Ordering?
Keys

where to use this example of 

@Header
- KafkaHeaders.RECEIVED_KEY
- KafkaHeaders.RECEIVED_PARTITION

@Payload


Testing:


# Consuming Keyed Messages


Testing:

Run 2 application: Stop it
- run Application (App1)
    mvn spring-boot:run

- run Application (App2)
 groupId = "dispatch.order.created.consumer2",
    mvn spring-boot:run    
- Check in console at last there will find different group id


# Consumer running
- kafka-console-consumer --bootstrap-server localhost:9092 --topic order.dispatched --property print.key=true --property key.separator=:

# Producer running
- kafka-console-producer --bootstrap-server localhost:9092 --topic order.created --property parse.key=true -- property key.separator=:
>"123":{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book-7"} 

Check application console

Describe partition
```bash
bin/kafka-topics.sh --bootstarp-server localhost:9092 --describe --topic order.created
```

---------

# Now lets increase partition from 1 to 5

## To describe partition
```bash
bin/kafka-topics.sh --bootstarp-server localhost:9092 --describe --topic order.created
```



## To Alter partition
```bash

bin/kafka-topics.sh --bootstrap-server localhost:9092 --alter --topic order.created -- partitions 5
```

## To describe partition
```bash
bin/kafka-topics.sh --bootstarp-server localhost:9092 --describe --topic order.created
```


Testing:

Run 2 application: Stop it
- run Application (App1)
    mvn spring-boot:run

- run Application (App2)
 groupId = "dispatch.order.created.consumer2",
    mvn spring-boot:run    
- Check in console at last there will find different partition and group id




# Consumer running
- kafka-console-consumer --bootstrap-server localhost:9092 --topic order.dispatched --property print.key=true --property key.separator=:

# Producer running
- kafka-console-producer --bootstrap-server localhost:9092 --topic order.created --property parse.key=true -- property key.separator=:
>"456":{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book-456"} 

Check application console

>"456":{"orderId":"550e8400-e29b-41d4-a716-446655440001","item":"book-456"} 
>"456":{"orderId":"550e8400-e29b-41d4-a716-446655440002","item":"book-456"} 



>"789":{"orderId":"550e8400-e29b-41d4-a716-446655440009","item":"book-789"} 
>"789":{"orderId":"550e8400-e29b-41d4-a716-446655440007","item":"book-789"} 


## to list consumer groups
```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

## TO describe consumer groups of given group

```bash
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group dispatch.order.created.consumer
```

Does that same key always fix same partition?
How can we send message to given partition?
How can we send key to different partition in round ribbon?