mvn clean install

mvn spring-boot:run

https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/blob/02-consume/src/main/resources/application.properties

@KafkaListener

kafka-console-producer --bootstrap-server kafka1:9092 --topic order.created
>Hello


--------


https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/tree/03-json


kafka-console-producer --bootstrap-server kafka1:9092 --topic order.created
>{"orderID":"UUID","item":"item-1"}



----
https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/blob/04-json-errorhandling/src/main/resources/application.properties


kafka-console-producer --bootstrap-server kafka1:9092 --topic order.created
>{"orderID":"12345","item":"item-1"}

------

Lect 33 -Revise

https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/tree/05-beans

https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/blob/05-beans/src/main/java/dev/lydtech/dispatch/DispatchConfiguration.java


---

https://github.com/lydtechconsulting/introduction-to-kafka-with-spring-boot/tree/06-produce

