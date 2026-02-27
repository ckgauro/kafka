# Kafka install

https://kafka.apache.org/community/downloads/

```
4.2.0
Released February 17, 2026
Release Notes
Docker image: apache/kafka:4.2.0.
Docker Native image: apache/kafka-native:4.2.0.
Source download: kafka-4.2.0-src.tgz (asc, sha512)
Binary download: kafka_2.13-4.2.0.tgz (asc, sha512)
```

download 
> Binary download: kafka_2.13-4.2.0.tgz (asc, sha512) version

$ mkdir -p ~/tools/kafka
$cd  ~/tools/ka
$tar -xvf ~/Downloads/kafka_2.13-4.2.0.tgz
$cd  kafka_2.13-4.2.0
$ls

>setup cluster id
$KAFKA_CLUSTER_ID="$(bin/kafka-storage.sh random-uuid)"

$echo $KAFKA_CLUSTER_ID

>Log directory location :This is a directory on the disk that will be used for the persistent store of the message topics and

all the metadata for Kafka.
$bin/kafka-storage.sh format -t $KAFKA_CLUSTER_ID -c config/Kraft/server.properties

```bash
/tmp/kraft-combined-logs
```
> Start kafka

$bin/kafka-server-start.sh config/kraft/server.properties

> Stop kafka

$bin/kafka-server-stop.sh 
$



$
$
$
$
$
$
$
$
$