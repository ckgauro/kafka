# OrderCreateHandlerTest — Summary Tables Explanation

This test class checks if `OrderCreateHandler` works correctly.  
It uses **JUnit 5** for testing and **Mockito** for mocking dependencies.

The goal of this test:
- Verify that `DispatchService.process()` is called
- Verify that handler works even if service throws exception


```java
class OrderCreateHandlerTest {

    private OrderCreateHandler orderCreateHandler;
    private DispatchService dispatchServiceMock;

    @BeforeEach
    void setUp(){
        dispatchServiceMock=mock(DispatchService.class);
        orderCreateHandler=new OrderCreateHandler(dispatchServiceMock);
    }

    @Test
    void listenSuccess()throws Exception{
        OrderCreated testEvent= TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        orderCreateHandler.listen(testEvent);
        verify(dispatchServiceMock, times(1)).process(testEvent);
    }

    @Test
    public void listen_ServiceThrowsException() throws Exception{
        OrderCreated testEvent=TestEventData.buildOrderCreatedEvent(randomUUID(),randomUUID().toString());
        doThrow(new RuntimeException("Service failure")).when(dispatchServiceMock).process(testEvent);
        orderCreateHandler.listen(testEvent);
        verify(dispatchServiceMock,times(1)).process(testEvent);

    }

}
```


## 1. Imports Summary

| Import | Purpose | Why needed |
|---------|---------|------------|
| `OrderCreated` | Event class | Used as test input |
| `DispatchService` | Service dependency | Will be mocked |
| `TestEventData` | Test data builder | Creates fake events |
| `BeforeEach` | JUnit annotation | Runs before every test |
| `Test` | JUnit annotation | Marks test methods |
| `UUID` | Unique IDs | Used to create test values |
| `randomUUID` | Static method | Generate random UUID |
| `Assertions.*` | JUnit assertions | For checking results |
| `Mockito.*` | Mockito methods | mock, verify, doThrow |

Note:  
`OrderCreateHandler` not listed in imports because it is in the same package.

---

## 2. Fields Summary

```java
private OrderCreateHandler orderCreateHandler;
private DispatchService dispatchServiceMock;
```

| Field               | Meaning                   |
| ------------------- | ------------------------- |
| orderCreateHandler  | Class under test          |
| dispatchServiceMock | Mock service used in test |

**Setup Method**
```java
@BeforeEach
void setUp()
```

| Field               | Meaning                   |
| ------------------- | ------------------------- |
| orderCreateHandler  | Class under test          |
| dispatchServiceMock | Mock service used in test |



**Test 1 — listenSuccess**
| Step          | Check               |
| ------------- | ------------------- |
| create event  | TestEventData       |
| call listen() | handler runs        |
| verify        | process called once |

```java
verify(dispatchServiceMock, times(1)).process(testEvent);
```
Checks that the mocked `DispatchService` method `process(testEvent)` was called exactly `one time` during the test.
Used in `Mockito` to confirm the handler correctly called the service.


**Test 2 — listen_ServiceThrowsException**

| Step              | Check               |
| ----------------- | ------------------- |
| mock throws error | doThrow             |
| call listen()     | no crash            |
| verify            | process called once |


```java
doThrow(...).when(mock).process(...)
```
This tells Mockito to throw a `RuntimeException` when `dispatchServiceMock.process`(testEvent) is called.
It is used to simulate a service failure so the test can verify that the handler correctly handles exceptions without crashing.

**Key Mockito Methods**
| Method    | Meaning            |
| --------- | ------------------ |
| mock()    | create fake object |
| verify()  | check call         |
| times(1)  | called once        |
| doThrow() | simulate error     |



**Test Purpose**

| Test                          | Goal           |
| ----------------------------- | -------------- |
| listenSuccess                 | service called |
| listen_ServiceThrowsException | error handled  |


-------
# DispatchServiceTest — Short Summary Tables

Unit test for `DispatchService` using **JUnit5 + Mockito + KafkaTemplate mock**


```java

class DispatchServiceTest {

    private DispatchService service;
    private KafkaTemplate kafkaProducerMock;

    @BeforeEach
    void setUp(){
        kafkaProducerMock=mock(KafkaTemplate.class);
        service=new DispatchService(kafkaProducerMock);
    }

    @Test
    void process_Success() throws Exception {
        CompletableFuture<SendResult<String, Object>> future =
                CompletableFuture.completedFuture(null);

        when(kafkaProducerMock.send(anyString(), any(OrderDispatched.class)))
                .thenReturn(future);

        OrderCreated testEvent =
                TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());

        service.process(testEvent);

        verify(kafkaProducerMock, times(1))
                .send(eq("order.dispatched"), any(OrderDispatched.class));
    }
    @Test
    void process_ProducerThrowsException(){
        OrderCreated testEvent= TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        doThrow(new RuntimeException("Producer failure")).when(kafkaProducerMock)
                .send(eq("order.dispatched"), any(OrderDispatched.class));
        Exception exception=assertThrows(RuntimeException.class,()-> service.process(testEvent));
        verify(kafkaProducerMock, times(1))
                .send(eq("order.dispatched"), any(OrderDispatched.class));
        assertThat(exception.getMessage(),equalTo("Producer failure"));


    }
}
```


## 1. Class Overview

| Item | Code | Purpose |
|------|------|---------|
| Test class | `DispatchServiceTest` | Test DispatchService |
| Service | `DispatchService service` | Class under test |
| Mock | `KafkaTemplate kafkaProducerMock` | Fake Kafka producer |

---

## 2. Setup Method

```java
@BeforeEach
void setUp(){
    kafkaProducerMock = mock(KafkaTemplate.class);
    service = new DispatchService(kafkaProducerMock);
}
```

| Step                | Meaning           |
| ------------------- | ----------------- |
| mock()              | Create fake Kafka |
| new DispatchService | Inject mock       |
| BeforeEach          | Run before test   |

## 3. Test — process_Success()
```java
@Test
void process_Success()
```
### DispatchServiceTest → process_Success() Explanation (Single Summary Table)

| Line / Code | Explanation |
|-----------|------------|
| `@Test` | Marks this method as a JUnit test case. JUnit will run this method during testing. |
| `void process_Success() throws Exception` | Test method name is **process_Success**. It checks the success scenario of `process()` method. `throws Exception` allows the test to run without handling checked exceptions manually. |
| `CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);` | Creates a completed future object. Kafka `send()` returns a `CompletableFuture`, so we mock a successful send result using `completedFuture(null)`. |
| `when(kafkaProducerMock.send(anyString(), any(OrderDispatched.class)))` | Mockito mock setup. It tells that when `send()` is called on `kafkaProducerMock` with any topic and any `OrderDispatched` object, then return the mocked result. |
| `.thenReturn(future);` | Specifies that the mocked `send()` method should return the previously created `future`. This simulates successful Kafka publishing. |
| `OrderCreated testEvent =` | Declares a test event object that will be passed to the service. |
| `TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());` | Calls helper method to create a fake `OrderCreated` event with random values. Used for testing instead of real data. |
| `service.process(testEvent);` | Calls the method under test. This should internally send a message to Kafka topic `order.dispatched`. |
| `verify(kafkaProducerMock, times(1))` | Mockito verification. Checks that the mock was used exactly **1 time**. |
| `.send(eq("order.dispatched"), any(OrderDispatched.class));` | Confirms that `send()` was called with topic `"order.dispatched"` and an `OrderDispatched` object. `eq()` is used because Mockito requires matchers for all parameters. |


### 4. @Test - process_ProducerThrowsException

#### DispatchServiceTest → process_ProducerThrowsException() Detailed Explanation

| Line / Code | Detailed Explanation |
|-----------|----------------------|
| `@Test` | Marks this method as a JUnit test case. JUnit will execute this method during the test run. |
| `void process_ProducerThrowsException()` | Test method name. This test checks the scenario when **Kafka producer throws an exception** while sending a message. |
| `OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());` | Creates a fake `OrderCreated` event using helper class. Random values are used to simulate real input data for testing. |
| `doThrow(new RuntimeException("Producer failure"))` | Mockito instruction to throw an exception. This simulates a failure in Kafka producer. |
| `.when(kafkaProducerMock)` | Applies the mock behavior to `kafkaProducerMock`. |
| `.send(eq("order.dispatched"), any(OrderDispatched.class));` | Specifies that when `send()` is called with topic `"order.dispatched"` and any `OrderDispatched` object, then throw the exception. `eq()` is used because Mockito requires matchers for all parameters. |
| `Exception exception = assertThrows(RuntimeException.class, () -> service.process(testEvent));` | JUnit assertion that expects an exception. When `service.process()` runs, it should throw `RuntimeException`. If no exception occurs, the test fails. The thrown exception is stored in variable `exception`. |
| `verify(kafkaProducerMock, times(1))` | Mockito verification step. Confirms that the Kafka producer `send()` method was called exactly one time. |
| `.send(eq("order.dispatched"), any(OrderDispatched.class));` | Verifies that the send was attempted with correct topic and message type. |
| `assertThat(exception.getMessage(), equalTo("Producer failure"));` | Checks the exception message. Ensures that the error message is exactly `"Producer failure"`. This confirms the correct exception was thrown. |

---

### Summary

| Step | What happens |
|------|-------------|
| 1 | Create test event |
| 2 | Mock Kafka to throw exception |
| 3 | Call service.process() |
| 4 | Exception should occur |
| 5 | Verify send() called once |
| 6 | Verify exception message |
| 7 | Test passed if all correct |
