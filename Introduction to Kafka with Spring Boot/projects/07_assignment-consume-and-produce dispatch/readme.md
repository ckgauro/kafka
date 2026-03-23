```java
@RequiredArgsConstructor
@Service
public class DispatchService {

    private static final String DISPATCH_TRACKING_TOPIC="dispatch.tracking";
    private static final String ORDER_DISPATCHED_TOPIC="order.dispatched";
    private final KafkaTemplate<String,Object> kafkaProducer;

    public void process(OrderCreated orderCreated) throws Exception{
        DispatchPreparing dispatchPreparing= DispatchPreparing.builder()
                .orderId(orderCreated.getOrderId())
                .build();
        kafkaProducer.send(DISPATCH_TRACKING_TOPIC,dispatchPreparing).get();

        OrderDispatched orderDispatched= OrderDispatched.builder()
                .orderId(orderCreated.getOrderId())
                .build();
        kafkaProducer.send(ORDER_DISPATCHED_TOPIC,orderDispatched).get();

    }
}
```

Here is the **single summary table** for your `DispatchService` class.

## Summary Table – DispatchService

| Component     | Type            | Code / Name                                     | Purpose                                    | Important Notes                       |
| ------------- | --------------- | ----------------------------------------------- | ------------------------------------------ | ------------------------------------- |
| Annotation    | Lombok          | `@RequiredArgsConstructor`                      | Generates constructor for final fields     | Injects `KafkaTemplate` automatically |
| Annotation    | Spring          | `@Service`                                      | Marks class as service layer bean          | Managed by Spring container           |
| Class         | Service Class   | `DispatchService`                               | Handles dispatch logic after order created | Sends events to Kafka                 |
| Constant      | Topic Name      | `DISPATCH_TRACKING_TOPIC = "dispatch.tracking"` | Kafka topic for tracking event             | Used for dispatch status              |
| Constant      | Topic Name      | `ORDER_DISPATCHED_TOPIC = "order.dispatched"`   | Kafka topic for dispatched event           | Used after dispatch completed         |
| Field         | Dependency      | `KafkaTemplate<String,Object> kafkaProducer`    | Kafka producer for sending messages        | Injected by Spring                    |
| Method        | Business Method | `process(OrderCreated orderCreated)`            | Called when order created event received   | Produces 2 Kafka events               |
| Object        | Event           | `DispatchPreparing`                             | Event for preparing dispatch               | Sent first                            |
| Object        | Event           | `OrderDispatched`                               | Event for dispatched order                 | Sent second                           |
| Kafka Call    | Send Message    | `kafkaProducer.send(topic, data)`               | Sends message to Kafka topic               | Returns Future                        |
| Blocking Call | `.get()`        | Wait for Kafka send result                      | Ensures message sent before next step      | Used for reliability                  |
| Flow Step 1   | Create Event    | DispatchPreparing.builder()                     | Build preparing event                      | Uses orderId                          |
| Flow Step 2   | Send Event      | dispatch.tracking topic                         | Track dispatch progress                    | First message                         |
| Flow Step 3   | Create Event    | OrderDispatched.builder()                       | Build dispatched event                     | Uses orderId                          |
| Flow Step 4   | Send Event      | order.dispatched topic                          | Notify dispatch complete                   | Second message                        |
| Architecture  | Pattern         | Event-Driven / Kafka                            | Service produces events                    | Used in microservices                 |
| Reliability   | Sync Send       | `.get()` used                                   | Makes send synchronous                     | Avoids message loss                   |


### Flow Diagram (Short)

```bash
OrderCreated → DispatchService.process()

→ create DispatchPreparing
→ send to dispatch.tracking

→ create OrderDispatched
→ send to order.dispatched
```