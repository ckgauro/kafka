```java
 @Test
    void process_Success() throws Exception {
        when(kafkaProducerMock.send(anyString(), any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));
        when(kafkaProducerMock.send(anyString(), any(OrderDispatched.class))).thenReturn(mock(CompletableFuture.class));

        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        service.process(testEvent);

        verify(kafkaProducerMock, times(1)).send(eq("dispatch.tracking"), any(DispatchPreparing.class));
        verify(kafkaProducerMock, times(1)).send(eq("order.dispatched"), any(OrderDispatched.class));
    }
```

### Summary Table – DispatchServiceTest.process_Success()

| Component       | Type           | Code                                              | Purpose                        | Important Notes                                |
| --------------- | -------------- | ------------------------------------------------- | ------------------------------ | ---------------------------------------------- |
| Annotation      | JUnit          | `@Test`                                           | Marks method as test case      | Executed by test runner                        |
| Method          | Test Method    | `process_Success()`                               | Tests successful dispatch flow | Should send 2 Kafka messages                   |
| Mock Setup      | Mockito        | `kafkaProducerMock`                               | Mock KafkaTemplate             | Avoid real Kafka call                          |
| Stub 1          | Mockito when   | `send(anyString(), any(DispatchPreparing.class))` | Mock first send call           | For dispatch.tracking                          |
| Return Value    | Mock Future    | `mock(CompletableFuture.class)`                   | Simulates Kafka result         | Needed because `.get()` is used                |
| Stub 2          | Mockito when   | `send(anyString(), any(OrderDispatched.class))`   | Mock second send call          | For order.dispatched                           |
| Test Data       | Helper         | `TestEventData.buildOrderCreatedEvent()`          | Creates OrderCreated event     | Input for service                              |
| Call Service    | Method Call    | `service.process(testEvent)`                      | Executes method under test     | Main logic runs                                |
| Verify 1        | Mockito verify | `dispatch.tracking`                               | Check first message sent       | Must be called once                            |
| Verify 2        | Mockito verify | `order.dispatched`                                | Check second message sent      | Must be called once                            |
| Matcher         | Mockito        | `anyString()`                                     | Accept any topic string        | Used in stubbing                               |
| Matcher         | Mockito        | `any(Class)`                                      | Accept any object of type      | Used in stubbing                               |
| Matcher         | Mockito        | `eq("topic")`                                     | Exact topic check              | Used in verify                                 |
| Verification    | Mockito        | `times(1)`                                        | Ensure called once             | Prevent duplicate calls                        |
| Reason for Mock | KafkaTemplate  | send().get() used                                 | Must return Future             | Otherwise test fails                           |
| Pattern         | Unit Test      | Arrange – Act – Assert                            | Standard testing pattern       | Arrange = when, Act = process, Assert = verify |


**Flow of Test**
```bash
Arrange:
mock kafkaProducer.send()

Act:
service.process()

Assert:
verify send(dispatch.tracking)
verify send(order.dispatched)
Important (Why CompletableFuture mock needed)
```
Your service uses:
```java
kafkaProducer.send(...).get();
```
So test must return `Future → otherwise NullPointerException.`

Correct:
```java
when(...).thenReturn(mock(CompletableFuture.class))
```

------

```java
 @Test
    void process_DispatchTrackingProducerThrowsException() {
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        doThrow(new RuntimeException("dispatch tracking producer failure")).when(kafkaProducerMock)
                .send(eq("dispatch.tracking"), any(DispatchPreparing.class));
        Exception exception = assertThrows(RuntimeException.class, () -> service.process(testEvent));
        verify(kafkaProducerMock, times(1))
                .send(eq("dispatch.tracking"), any(DispatchPreparing.class));
        assertThat(exception.getMessage(), equalTo("dispatch tracking producer failure"));
    }
```

**process_DispatchTrackingProducerThrowsException()**


| Component           | Type          | Code                                                | Purpose                    | Important Notes                  |
| ------------------- | ------------- | --------------------------------------------------- | -------------------------- | -------------------------------- |
| Annotation          | JUnit         | `@Test`                                             | Marks method as test case  | Executed during unit test        |
| Method              | Test Method   | `process_DispatchTrackingProducerThrowsException()` | Tests failure scenario     | Producer throws exception        |
| Test Data           | Helper        | `buildOrderCreatedEvent()`                          | Creates OrderCreated event | Input for service                |
| Mockito Stub        | doThrow       | `doThrow(RuntimeException)`                         | Simulate Kafka failure     | Used instead of `when()`         |
| Mock Target         | KafkaTemplate | `kafkaProducerMock.send()`                          | Mock send method           | First topic only                 |
| Topic Check         | Matcher       | `eq("dispatch.tracking")`                           | Exact topic match          | Important for verify             |
| Payload Match       | Matcher       | `any(DispatchPreparing.class)`                      | Accept any payload         | Used in mock                     |
| Exception Test      | JUnit         | `assertThrows()`                                    | Expect exception           | Test should fail if no exception |
| Lambda Call         | Service Call  | `service.process(testEvent)`                        | Executes method            | Inside assertThrows              |
| Verify Call         | Mockito       | `verify(...times(1))`                               | Ensure send called once    | Only first send should run       |
| Message Check       | Assertion     | `assertThat(message)`                               | Verify error message       | Confirms correct exception       |
| Assertion Tool      | Hamcrest      | `equalTo()`                                         | Compare expected text      | Checks exception message         |
| Behavior Tested     | Failure Flow  | dispatch.tracking fails                             | Second send should NOT run | Important logic                  |
| Test Pattern        | Arrange       | doThrow setup                                       | Prepare mock               |                                  |
| Test Pattern        | Act           | service.process()                                   | Run method                 |                                  |
| Test Pattern        | Assert        | verify + assertThrows                               | Validate result            |                                  |
| Reason doThrow used | Mockito rule  | Needed for void / special cases                     | Safe for exceptions        |                                  |
| Expected Flow       | Execution     | send(dispatch.tracking) → exception                 | Stops execution            |                                  |
| Not Executed        | Second send   | order.dispatched                                    | Should not run             |                                  |


**Flow of this Test**
```bash
Arrange:
mock send(dispatch.tracking) → throw exception

Act:
service.process()

Assert:
exception thrown
send called once
second send not executed
message correct
```
**Why doThrow instead of when**

Because we want to throw exception on method call.

Correct:
```java
doThrow(new RuntimeException())
.when(mock).send(...)
```
Used for:
- throwing exception
- void methods
- safer mocking

----

```java
@Test
    void process_OrderDispatchedThrowsException() {
        OrderCreated testEvent = TestEventData.buildOrderCreatedEvent(randomUUID(), randomUUID().toString());
        when(kafkaProducerMock.send(anyString(),any(DispatchPreparing.class))).thenReturn(mock(CompletableFuture.class));
        doThrow(new RuntimeException("order dispatch producer failure")).when(kafkaProducerMock)
                .send(eq("order.dispatched"), any(OrderDispatched.class));
        Exception exception=assertThrows(RuntimeException.class,()->service.process(testEvent));
        verify(kafkaProducerMock,times(1)).send(eq("dispatch.tracking"), any(DispatchPreparing.class));
        verify(kafkaProducerMock,times(1)).send(eq("order.dispatched"), any(OrderDispatched.class));
        assertThat(exception.getMessage(), equalTo("order dispatch producer failure"));

    }
```


**Summary Table – process_OrderDispatchedThrowsException()**

| Component       | Type                  | Code                                       | Purpose                            | Important Notes            |
| --------------- | --------------------- | ------------------------------------------ | ---------------------------------- | -------------------------- |
| Annotation      | JUnit                 | `@Test`                                    | Marks method as test case          | Runs during unit testing   |
| Method          | Test Method           | `process_OrderDispatchedThrowsException()` | Tests failure in second Kafka send | First send succeeds        |
| Test Data       | Helper                | `buildOrderCreatedEvent()`                 | Creates OrderCreated input         | Used for service call      |
| Stub 1          | Mockito when          | `send(anyString(), DispatchPreparing)`     | Mock first send                    | dispatch.tracking success  |
| Return Value    | Mock Future           | `mock(CompletableFuture.class)`            | Needed because `.get()` used       | Prevents NullPointer       |
| Stub 2          | Mockito doThrow       | `send(order.dispatched)`                   | Throw exception on second send     | Simulates producer failure |
| Matcher         | Mockito               | `eq("order.dispatched")`                   | Exact topic match                  | Important for correct mock |
| Matcher         | Mockito               | `any(OrderDispatched.class)`               | Accept any payload                 | Used in mock               |
| Exception Test  | JUnit                 | `assertThrows()`                           | Expect RuntimeException            | Test fails if not thrown   |
| Service Call    | Method                | `service.process(testEvent)`               | Executes logic                     | Inside assertThrows        |
| Verify 1        | Mockito verify        | dispatch.tracking                          | First send must run                | Should be called once      |
| Verify 2        | Mockito verify        | order.dispatched                           | Second send must run               | Should be called once      |
| Assertion       | Hamcrest              | `assertThat(message)`                      | Check exception message            | Ensure correct error       |
| Assertion Tool  | Hamcrest              | `equalTo()`                                | Compare text                       | Exact match required       |
| Flow Step 1     | Execution             | send(dispatch.tracking)                    | Success                            | Future returned            |
| Flow Step 2     | Execution             | send(order.dispatched)                     | Throws exception                   | Test scenario              |
| Flow Step 3     | Stop Flow             | Exception occurs                           | Method stops                       | No more execution          |
| Pattern         | Arrange               | when + doThrow                             | Prepare mocks                      |                            |
| Pattern         | Act                   | service.process()                          | Run method                         |                            |
| Pattern         | Assert                | verify + assertThrows                      | Validate behavior                  |                            |
| Key Rule        | Because `.get()` used | Must return Future                         | Otherwise test fails               |                            |
| Behavior Tested | Partial success       | First ok, second fails                     | Important scenario                 |                            |


**Flow of Test**

```bash
Arrange:
first send → success
second send → throw exception

Act:
service.process()

Assert:
first send called
second send called
exception thrown
message correct
```

**What this test proves**

| Scenario                | Result |
| ----------------------- | ------ |
| dispatch.tracking works | ✅      |
| order.dispatched fails  | ✅      |
| exception propagated    | ✅      |
| both sends attempted    | ✅      |
| message verified        | ✅      |
