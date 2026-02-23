## Set up a Kafka Cluster with 3 brokers
Run this command and this will spin up a kafka cluster with 3 brokers.
docker-compose -f docker-compose-multi-broker.yml up
```bash
$docker ps
```
---

## Create topic with the replication factor as 3

docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 \
             --create \
             --topic test-topic \
             --replication-factor 3 --partitions 3


## Produce Messages to the topic.
docker exec --interactive --tty kafka1  \
kafka-console-producer --bootstrap-server localhost:9092,kafka2:19093,kafka3:19094 \
                       --topic test-topic


## Consume Messages from the topic.
docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server localhost:9092,kafka2:19093,kafka3:19094 \
                       --topic test-topic \
                       --from-beginning             



-------
                       