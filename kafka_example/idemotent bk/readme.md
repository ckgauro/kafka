# producer

```bash
kafka-console-producer --bootstrap-server kafka1:9092 --topic order.created --property parse.key=true --property key.separator=:
```
input json
```json

"123":{"orderId":"550e8400-e29b-41d4-a716-446655440001","item":"book-123"}

"456":{"orderId":"550e8400-e29b-41d4-a716-446655440000","item":"book-456"}

```

# Consumer
```bash 
kafka-console-consumer --bootstrap-server kafka1:9092 --topic order.dispatched --property parse.key=true --property key.separator=:
```