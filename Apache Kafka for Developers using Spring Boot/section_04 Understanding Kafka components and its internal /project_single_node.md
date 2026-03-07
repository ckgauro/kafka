
# Set Up Kafka in Local using Docker

## Set up broker 
- Navigate to the path where the `docker-compose.yml` is located and then run the below command.
```bash
docker-compose up
```
## docker-compose up

Builds (if needed) and starts all services defined in `docker-compose.yml`.  
Creates containers, networks, and volumes, then runs them in the foreground by default.  

Use `-d` to run in detached mode (background).  
It’s the standard command to launch your full multi-container application.
---

# Producer and Consume the Messages
### 1. Let's going to the container by running the below command.
```bash
docker exec -it kafka1 bash
```

#### `docker exec -it kafka1 bash`

Opens an interactive Bash shell inside the running `kafka1` container.

- `exec` → run a command in an existing container  
- `-it` → interactive terminal  
- `kafka1` → container name  
- `bash` → shell to execute  

Used for debugging, checking logs, or running Kafka CLI tools.

---
### 2. Create a Kafka topic using the **kafka-topics** command.
- **kafka1:19092** refers to the **KAFKA_ADVERTISED_LISTENERS** in the `docker-compose.yml` file.

```bash
kafka-topics --bootstrap-server kafka1:19092 \
             --create \
             --topic test-topic \
             --replication-factor 1 --partitions 1
```
Creates a new Kafka topic named test-topic.
| Part                              | Purpose             | Explanation                                     |
| --------------------------------- | ------------------- | ----------------------------------------------- |
| `kafka-topics`                    | Kafka CLI tool      | Manages topics (create, list, describe, delete) |
| `--bootstrap-server kafka1:19092` | Broker address      | Connects to Kafka via internal Docker listener  |
| `--create`                        | Operation flag      | Creates a new topic                             |
| `--topic test-topic`              | Topic name          | Name of the topic to create                     |
| `--replication-factor 1`          | Replication setting | Single replica (used in single-broker setup)    |
| `--partitions 1`                  | Partition count     | Creates one partition for the topic             |

---
### 3. Produce Messages to the topic.
```bash
docker exec --interactive --tty kafka1  \
kafka-console-producer --bootstrap-server kafka1:19092 \
                       --topic test-topic
```

**inputData**
```bash
>hello tdoay
>Good
>thanks
```
Runs Kafka console producer inside the kafka1 container.

| Part                              | Purpose                  | Explanation                                          |
| --------------------------------- | ------------------------ | ---------------------------------------------------- |
| `docker exec`                     | Run command in container | Executes a command inside a running Docker container |
| `--interactive --tty` (`-it`)     | Interactive mode         | Opens a terminal so you can type messages manually   |
| `kafka1`                          | Container name           | Target Kafka container                               |
| `kafka-console-producer`          | Kafka CLI tool           | Sends messages to a Kafka topic                      |
| `--bootstrap-server kafka1:19092` | Broker address           | Connects to Kafka via internal Docker listener       |
| `--topic test-topic`              | Topic name               | Messages will be published to `test-topic`           |


---

### 4. Consume Messages from the topic.
```bash
docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic test-topic \
                       --from-beginning
```

**output**
```bash
hello tdoay
Good
thanks
```

| Part                              | Purpose              | Explanation                                    |
| --------------------------------- | -------------------- | ---------------------------------------------- |
| `docker exec`                     | Run inside container | Executes command in running `kafka1` container |
| `--interactive --tty` (`-it`)     | Interactive mode     | Opens terminal to view streaming messages      |
| `kafka-console-consumer`          | Kafka CLI tool       | Reads messages from a topic                    |
| `--bootstrap-server kafka1:19092` | Broker address       | Connects using internal Docker listener        |
| `--topic test-topic`              | Topic name           | Consumes messages from `test-topic`            |
| `--from-beginning`                | Offset option        | Reads all messages from the start of the topic |

# Producer and Consume the Messages With Key and Value
#### 1. Produce Messages with Key and Value to the topic.
```bash
docker exec --interactive --tty kafka1  \
kafka-console-producer --bootstrap-server kafka1:19092 \
                       --topic test-topic \
                       --property "key.separator=-" --property "parse.key=true"
```     
| Part                              | Purpose              | Explanation                            |
| --------------------------------- | -------------------- | -------------------------------------- |
| `docker exec -it kafka1`          | Run inside container | Opens interactive terminal in `kafka1` |
| `kafka-console-producer`          | CLI tool             | Produces (sends) messages to Kafka     |
| `--bootstrap-server kafka1:19092` | Broker address       | Connects via internal Docker listener  |
| `--topic test-topic`              | Target topic         | Messages are sent to `test-topic`      |
| `parse.key=true`                  | Enable key parsing   | Splits input into key and value        |
| `key.separator=-`                 | Key delimiter        | Uses `-` to separate key and value     |

**Example Input**
```bash
order1-hello
```
- Key → `order1`
- Value → `hello`

#### 2. Consuming messages with Key and Value from a topic.

```bash
docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic test-topic \
                       --from-beginning \
                       --property "key.separator= - " --property "print.key=true"

```

| Part                              | Purpose              | Explanation                               |
| --------------------------------- | -------------------- | ----------------------------------------- |
| `docker exec -it kafka1`          | Run inside container | Opens interactive terminal in `kafka1`    |
| `kafka-console-consumer`          | CLI tool             | Consumes (reads) messages from Kafka      |
| `--bootstrap-server kafka1:19092` | Broker address       | Connects using internal Docker listener   |
| `--topic test-topic`              | Topic name           | Reads from `test-topic`                   |
| `--from-beginning`                | Offset option        | Reads all messages starting from offset 0 |
| `print.key=true`                  | Show key             | Displays message key along with value     |
| `key.separator= - `               | Key delimiter        | Prints key and value separated by `-`     |


**Output Format Example**

If produced as:
```bash
order1-hello
```
Consumer output:
```code
order1 - hello
```

# Consume Messages using Consumer Groups
```bash
docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic test-topic --group console-consumer-41911\
                       --property "key.separator= - " --property "print.key=true"
```
| Part                              | Purpose              | Explanation                                    |
| --------------------------------- | -------------------- | ---------------------------------------------- |
| `docker exec -it kafka1`          | Run inside container | Opens interactive terminal in `kafka1`         |
| `kafka-console-consumer`          | CLI tool             | Consumes messages from Kafka                   |
| `--bootstrap-server kafka1:19092` | Broker address       | Connects via internal Docker listener          |
| `--topic test-topic`              | Topic name           | Reads from `test-topic`                        |
| `--group console-consumer-41911`  | Consumer group       | Enables offset tracking and group coordination |
| `print.key=true`                  | Show key             | Displays message key with value                |
| `key.separator= - `               | Output format        | Separates key and value using `-`              |


**Offset Behavior (With Group)**
| Scenario               | Start Position                                                 |
| ---------------------- | -------------------------------------------------------------- |
| First run of new group | Reads from latest offset (default)                             |
| Subsequent runs        | Resumes from last committed offset                             |
| Add `--from-beginning` | Forces read from offset 0 (only if no committed offset exists) |



Example Messages:
```bash
a-abc
b-bus 
```
------

# Consume Messages With Headers
```bash
docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic library-events.DLT \
                       --property "print.headers=true" --property "print.timestamp=true" 
```
| Part                              | Purpose                                             |
| --------------------------------- | --------------------------------------------------- |
| `docker exec -it kafka1`          | Run command inside `kafka1` container interactively |
| `kafka-console-consumer`          | Start Kafka CLI consumer                            |
| `--bootstrap-server kafka1:19092` | Connect to broker via internal listener             |
| `--topic library-events.DLT`      | Consume messages from Dead Letter Topic             |
| `print.headers=true`              | Display message headers                             |
| `print.timestamp=true`            | Display message timestamps                          |

🔹 What This Does
- Consumes messages from library-events.DLT and prints:
- Message value
- Headers
- 
Timestamp

**Example Messages:**
```bash
a-abc
b-bus
```



-----
kafka-topics --bootstrap-server kafka1:19092 \
             --create \
             --topic test-topic \
             --replication-factor 1 --partitions 1

----
Created topic test-topic

====


Producer and Consume the Messages With Key and Value

docker exec --interactive --tty kafka1  \
kafka-console-producer --bootstrap-server kafka1:19092 \
                       --topic test-topic \
                       --property "key.separator=-" --property "parse.key=true"

---
```message
>A-Apple
>B-Ball
>
```

docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic test-topic \
                       --from-beginning \
                       --property "key.separator= - " --property "print.key=true"


```
A - Apple
B - Ball
```



--------
## Advanced Kafka Commands

docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 --list
```java
__consumer_offsets
test-topic
```
### How to view consumer groups

docker exec --interactive --tty kafka1  \
kafka-consumer-groups --bootstrap-server kafka1:19092 --list

```
console-consumer-27883
```


## Consume Messages using Consumer Groups

docker exec --interactive --tty kafka1  \
kafka-console-consumer --bootstrap-server kafka1:19092 \
                       --topic test-topic --group console-consumer-27883\
                       --property "key.separator= - " --property "print.key=true"

---


## Describe topic

docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 --describe

docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 --describe \
--topic test-topic

## Alter topic Partitions

docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 \
--alter --topic test-topic --partitions 2


## Log file and related config

cd /etc/kafka/
[appuser@kafka1 kafka]$ ls
connect-console-sink.properties    kafka.properties
connect-console-source.properties  kraft
connect-distributed.properties     log4j.properties
connect-file-sink.properties       producer.properties
connect-file-source.properties     secrets
connect-log4j.properties           server.properties
connect-mirror-maker.properties    tools-log4j.properties
connect-standalone.properties      trogdor.conf
consumer.properties                zookeeper.properties


```txt
$ cat server.properties 
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# This configuration file is intended for use in ZK-based mode, where Apache ZooKeeper is required.
# See kafka.server.KafkaConfig for additional details and defaults
#

############################# Server Basics #############################

# The id of the broker. This must be set to a unique integer for each broker.
broker.id=0

############################# Socket Server Settings #############################

# The address the socket server listens on. If not configured, the host name will be equal to the value of
# java.net.InetAddress.getCanonicalHostName(), with PLAINTEXT listener name, and port 9092.
#   FORMAT:
#     listeners = listener_name://host_name:port
#   EXAMPLE:
#     listeners = PLAINTEXT://your.host.name:9092
#listeners=PLAINTEXT://:9092

# Listener name, hostname and port the broker will advertise to clients.
# If not set, it uses the value for "listeners".
#advertised.listeners=PLAINTEXT://your.host.name:9092

# Maps listener names to security protocols, the default is for them to be the same. See the config documentation for more details
#listener.security.protocol.map=PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL

# The number of threads that the server uses for receiving requests from the network and sending responses to the network
num.network.threads=3

# The number of threads that the server uses for processing requests, which may include disk I/O
num.io.threads=8

# The send buffer (SO_SNDBUF) used by the socket server
socket.send.buffer.bytes=102400

# The receive buffer (SO_RCVBUF) used by the socket server
socket.receive.buffer.bytes=102400

# The maximum size of a request that the socket server will accept (protection against OOM)
socket.request.max.bytes=104857600


############################# Log Basics #############################

# A comma separated list of directories under which to store log files
log.dirs=/var/lib/kafka

# The default number of log partitions per topic. More partitions allow greater
# parallelism for consumption, but this will also result in more files across
# the brokers.
num.partitions=1

# The number of threads per data directory to be used for log recovery at startup and flushing at shutdown.
# This value is recommended to be increased for installations with data dirs located in RAID array.
num.recovery.threads.per.data.dir=1

############################# Internal Topic Settings  #############################
# The replication factor for the group metadata internal topics "__consumer_offsets" and "__transaction_state"
# For anything other than development testing, a value greater than 1 is recommended to ensure availability such as 3.
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1

############################# Log Flush Policy #############################

# Messages are immediately written to the filesystem but by default we only fsync() to sync
# the OS cache lazily. The following configurations control the flush of data to disk.
# There are a few important trade-offs here:
#    1. Durability: Unflushed data may be lost if you are not using replication.
#    2. Latency: Very large flush intervals may lead to latency spikes when the flush does occur as there will be a lot of data to flush.
#    3. Throughput: The flush is generally the most expensive operation, and a small flush interval may lead to excessive seeks.
# The settings below allow one to configure the flush policy to flush data after a period of time or
# every N messages (or both). This can be done globally and overridden on a per-topic basis.

# The number of messages to accept before forcing a flush of data to disk
#log.flush.interval.messages=10000

# The maximum amount of time a message can sit in a log before we force a flush
#log.flush.interval.ms=1000

############################# Log Retention Policy #############################

# The following configurations control the disposal of log segments. The policy can
# be set to delete segments after a period of time, or after a given size has accumulated.
# A segment will be deleted whenever *either* of these criteria are met. Deletion always happens
# from the end of the log.

# The minimum age of a log file to be eligible for deletion due to age
log.retention.hours=168

# A size-based retention policy for logs. Segments are pruned from the log unless the remaining
# segments drop below log.retention.bytes. Functions independently of log.retention.hours.
#log.retention.bytes=1073741824

# The maximum size of a log segment file. When this size is reached a new log segment will be created.
#log.segment.bytes=1073741824

# The interval at which log segments are checked to see if they can be deleted according
# to the retention policies
log.retention.check.interval.ms=300000

############################# Zookeeper #############################

# Zookeeper connection string (see zookeeper docs for details).
# This is a comma separated host:port pairs, each corresponding to a zk
# server. e.g. "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002".
# You can also append an optional chroot string to the urls to specify the
# root directory for all kafka znodes.
zookeeper.connect=localhost:2181

# Timeout in ms for connecting to zookeeper
zookeeper.connection.timeout.ms=18000

##################### Confluent Metrics Reporter #######################
# Confluent Control Center and Confluent Auto Data Balancer integration
#
# Uncomment the following lines to publish monitoring data for
# Confluent Control Center and Confluent Auto Data Balancer
# If you are using a dedicated metrics cluster, also adjust the settings
# to point to your metrics kakfa cluster.
#metric.reporters=io.confluent.metrics.reporter.ConfluentMetricsReporter
#confluent.metrics.reporter.bootstrap.servers=localhost:9092
#
# Uncomment the following line if the metrics cluster has a single broker
#confluent.metrics.reporter.topic.replicas=1

############################# Group Coordinator Settings #############################

# The following configuration specifies the time, in milliseconds, that the GroupCoordinator will delay the initial consumer rebalance.
# The rebalance will be further delayed by the value of group.initial.rebalance.delay.ms as new members join the group, up to a maximum of max.poll.interval.ms.
# The default value for this is 3 seconds.
# We override this to 0 here as it makes for a better out-of-the-box experience for development and testing.
# However, in production environments the default value of 3 seconds is more suitable as this will help to avoid unnecessary, and potentially expensive, rebalances during application startup.
group.initial.rebalance.delay.ms=0
```


log.retention.hours=168


---

```txt
[appuser@kafka1 kafka]$ cd /var/lib/kafka/data/
[appuser@kafka1 data]$ ls
__cluster_metadata-0   __consumer_offsets-35
__consumer_offsets-0   __consumer_offsets-36
__consumer_offsets-1   __consumer_offsets-37
__consumer_offsets-10  __consumer_offsets-38
__consumer_offsets-11  __consumer_offsets-39
__consumer_offsets-12  __consumer_offsets-4
__consumer_offsets-13  __consumer_offsets-40
__consumer_offsets-14  __consumer_offsets-41
__consumer_offsets-15  __consumer_offsets-42
__consumer_offsets-16  __consumer_offsets-43
__consumer_offsets-17  __consumer_offsets-44
__consumer_offsets-18  __consumer_offsets-45
__consumer_offsets-19  __consumer_offsets-46
__consumer_offsets-2   __consumer_offsets-47
__consumer_offsets-20  __consumer_offsets-48
__consumer_offsets-21  __consumer_offsets-49
__consumer_offsets-22  __consumer_offsets-5
__consumer_offsets-23  __consumer_offsets-6
__consumer_offsets-24  __consumer_offsets-7
__consumer_offsets-25  __consumer_offsets-8
__consumer_offsets-26  __consumer_offsets-9
__consumer_offsets-27  bootstrap.checkpoint
__consumer_offsets-28  cleaner-offset-checkpoint
__consumer_offsets-29  log-start-offset-checkpoint
__consumer_offsets-3   meta.properties
__consumer_offsets-30  recovery-point-offset-checkpoint
__consumer_offsets-31  replication-offset-checkpoint
__consumer_offsets-32  test-topic-0
__consumer_offsets-33  test-topic-1
__consumer_offsets-34

```

$ cd test-topic-0
[appuser@kafka1 test-topic-0]$ ls
00000000000000000000.index      leader-epoch-checkpoint
00000000000000000000.log        partition.metadata
00000000000000000000.timeindex
[appuser@kafka1 test-topic-0]$ cat 00000000000000000000.log
8D��C����N����N
               =>�N����4����4
hello;Md�����O����OheyC������ʗ���ʗ"how are you>�E'�����,����,A
Apple=ߙ�f���2����2�Ball=±�����C����CTest=������
����



============

