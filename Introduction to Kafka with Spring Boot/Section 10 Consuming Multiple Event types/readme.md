- https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot-tracking-service/tree/03-kafka-handler
DispatchPreparing

@KafkaListener
@KafkaHandler



Testing:

Run 2 application: Stop it
- run Application (App1)
    mvn spring-boot:run

- run Application (App2)
 groupId = "dispatch.order.created.consumer2",
    mvn spring-boot:run    
- Check in console at last there will find different partition




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