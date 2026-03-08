
create KAFKA_CLUSTER_ID

Assign to server.properties
start KAFKA


-----

Sending and Receiving 

## to create topics
```bash
# Outside the docker
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic my.first.topic

# Same command inside docker
kafka-topics --bootstrap-server kafka1:9092 --create --topic my.first.topic
```

To list 

## to list topics

```bash
# Outside the docker
kafka-topics.sh --bootstrap-server localhost:9092 --list


# Same command inside docker
kafka-topics --bootstrap-server kafka1:9092 --list
```

## To create producer
```bash

# Outside the docker
bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic my.first.topic


# Same command inside docker
kafka-console-producer --bootstrap-server kafka1:9092 --topic my.first.topic
```

## To create consumer
```bash

# Outside the docker
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic my.first.topic


# Same command inside docker
 kafka-console-consumer --bootstrap-server kafka1:9092 --topic my.first.topic
```


## Describe Topic 

```bash
$ kafka-topics --bootstrap-server kafka1:9092  --describe --topic my.new.topic
Topic: my.new.topic     TopicId: C31o938sT-aDpTVXLn_IjA PartitionCount: 1       ReplicationFactor: 1    Configs: 
        Topic: my.new.topic     Partition: 0    Leader: 1       Replicas: 1     Isr: 1

```


## to Alter partitions topics 

```bash
kafka-topics --bootstrap-server kafka1:9092 --alter --topic my.new.topic --partitions 3

$ kafka-topics --bootstrap-server kafka1:9092 --describe --topic my.new.topic
Topic: my.new.topic     TopicId: C31o938sT-aDpTVXLn_IjA PartitionCount: 3       ReplicationFactor: 1    Configs: 
        Topic: my.new.topic     Partition: 0    Leader: 1       Replicas: 1     Isr: 1
        Topic: my.new.topic     Partition: 1    Leader: 1       Replicas: 1     Isr: 1
        Topic: my.new.topic     Partition: 2    Leader: 1       Replicas: 1     Isr: 1
```


## to delete topic

```bash

kafka-topics --bootstrap-server kafka1:9092 --delete --topic my.new.topic

$ kafka-topics --bootstrap-server kafka1:9092 --list
__consumer_offsets
my.first.topic
```

## Consumer Group CLI

```bash
kafka-topics --bootstrap-server kafka1:9092 --create --topic cg.demo.topic --partitions 5

```
## to View group
```bash
kafka-topics --bootstrap-server kafka1:9092 --describe --topic cg.demo.topic
```
**Output**
```bash
Topic: cg.demo.topic    TopicId: kZH7VOyqQ1WQlCy0N8vWaQ PartitionCount: 5       ReplicationFactor: 1    Configs: 
        Topic: cg.demo.topic    Partition: 0    Leader: 1       Replicas: 1     Isr: 1
        Topic: cg.demo.topic    Partition: 1    Leader: 1       Replicas: 1     Isr: 1
        Topic: cg.demo.topic    Partition: 2    Leader: 1       Replicas: 1     Isr: 1
        Topic: cg.demo.topic    Partition: 3    Leader: 1       Replicas: 1     Isr: 1
        Topic: cg.demo.topic    Partition: 4    Leader: 1       Replicas: 1     Isr: 1
```

## To Create Groups
```bash
kafka-console-consumer --bootstrap-server kafka1:9092 --topic cg.demo.topic --group my.new.group
```

## To view group
```bash
kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group my.new.group
```
***output**
```bash
GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                           HOST            CLIENT-ID
my.new.group    cg.demo.topic   0          0               0               0               console-consumer-8c6ee6d1-8e99-4e51-87fe-7fca44d50300 /127.0.0.1      console-consumer
my.new.group    cg.demo.topic   1          0               0               0               console-consumer-8c6ee6d1-8e99-4e51-87fe-7fca44d50300 /127.0.0.1      console-consumer
my.new.group    cg.demo.topic   2          0               0               0               console-consumer-8c6ee6d1-8e99-4e51-87fe-7fca44d50300 /127.0.0.1      console-consumer
my.new.group    cg.demo.topic   3          0               0               0               console-consumer-8c6ee6d1-8e99-4e51-87fe-7fca44d50300 /127.0.0.1      console-consumer
my.new.group    cg.demo.topic   4          0               0               0               console-consumer-8c6ee6d1-8e99-4e51-87fe-7fca44d50300 /127.0.0.1      console-consumer

```

## Again run following command in another window
```bash
kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group my.new.group
```

## To view group
```bash
kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group my.new.group
```
***output**
```bash
GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                           HOST            CLIENT-ID
my.new.group    cg.demo.topic   0          0               0               0               console-consumer-743791f1-ef10-4eca-891a-8f54e669609f /127.0.0.1      console-consumer
my.new.group    cg.demo.topic   1          0               0               0               console-consumer-743791f1-ef10-4eca-891a-8f54e669609f /127.0.0.1      console-consumer
my.new.group    cg.demo.topic   2          0               0               0               console-consumer-743791f1-ef10-4eca-891a-8f54e669609f /127.0.0.1      console-consumer
my.new.group    cg.demo.topic   3          0               0               0               console-consumer-8c6ee6d1-8e99-4e51-87fe-7fca44d50300 /127.0.0.1      console-consumer
my.new.group    cg.demo.topic   4          0               0               0               console-consumer-8c6ee6d1-8e99-4e51-87fe-7fca44d50300 /127.0.0.1      console-consumer

```

-------

```bash
kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group my.new.group --state
```

**output**
```bash
GROUP                     COORDINATOR (ID)          ASSIGNMENT-STRATEGY  STATE           #MEMBERS
my.new.group              127.0.0.1:9092 (1)        range                Stable          2
```

```bash
kafka-consumer-groups --bootstrap-server kafka1:9092 --describe --group my.new.group --members
```

```bash
GROUP           CONSUMER-ID                                           HOST            CLIENT-ID        #PARTITIONS     
my.new.group    console-consumer-8c6ee6d1-8e99-4e51-87fe-7fca44d50300 /127.0.0.1      console-consumer 2               
my.new.group    console-consumer-743791f1-ef10-4eca-891a-8f54e669609f /127.0.0.1      console-consumer 3               
```




---------
## Kafka command

```bash
$ls /usr/bin/kafka*
kafka-acls                 kafka-features                    kafka-run-class
kafka-broker-api-versions  kafka-get-offsets                 kafka-server-start
kafka-cluster              kafka-leader-election             kafka-server-stop
kafka-configs              kafka-log-dirs                    kafka-storage
kafka-console-consumer     kafka-metadata-quorum             kafka-streams-application-reset
kafka-console-producer     kafka-metadata-shell              kafka-topics
kafka-consumer-groups      kafka-mirror-maker                kafka-transactions
kafka-consumer-perf-test   kafka-preferred-replica-election  kafka-verifiable-consumer
kafka-delegation-tokens    kafka-producer-perf-test          kafka-verifiable-producer
kafka-delete-records       kafka-reassign-partitions
kafka-dump-log             kafka-replica-verification
```

```bash
$kafka-console-consumer --help
```

kafka-server-start.sh

kafka-server-stop.sh





