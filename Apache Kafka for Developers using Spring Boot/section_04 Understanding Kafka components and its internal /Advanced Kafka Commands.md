# Advanced Kafka Commands

## 1. List the topics in a cluster
```bash
docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 --list
```
### 1️⃣ Docker Part

| Command Part    | Purpose                                  |
| --------------- | ---------------------------------------- |
| `docker exec`   | Run a command inside a running container |
| `--interactive` | Keep STDIN open                          |
| `--tty`         | Allocate terminal session                |
| `kafka1`        | Target container name                    |


### 2️⃣ Kafka Part

| Command Part                      | Purpose                                      |
| --------------------------------- | -------------------------------------------- |
| `kafka-topics`                    | Kafka topic management CLI                   |
| `--bootstrap-server kafka1:19092` | Connect to broker (internal Docker listener) |
| `--list`                          | Display all existing topics                  |


------
## 2. Describe topic
**Command to describe all the Kafka topics.**

```bash
docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 --describe
```
### 1️⃣ Docker Part

| Command Part    | Purpose                              |
| --------------- | ------------------------------------ |
| `docker exec`   | Run command inside running container |
| `--interactive` | Keep input stream open               |
| `--tty`         | Allocate terminal session            |
| `kafka1`        | Target container                     |


### 2️⃣ Kafka Part

| Command Part                      | Purpose                               |
| --------------------------------- | ------------------------------------- |
| `kafka-topics`                    | Kafka topic management CLI            |
| `--bootstrap-server kafka1:19092` | Connect to broker (internal listener) |
| `--describe`                      | Show topic details                    |


### 📊 Output Shows

| Detail             | Meaning                |
| ------------------ | ---------------------- |
| Topic name         | Topic identifier       |
| Partitions         | Number of partitions   |
| Replication factor | Number of replicas     |
| Leader             | Broker handling writes |
| ISR                | In-sync replicas       |




- **Command to describe a specific Kafka topic.**
```bash
docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 --describe \
--topic test-topic
```
### 1️⃣ Docker Execution
| Part            | Meaning                      |
| --------------- | ---------------------------- |
| `docker exec`   | Run command inside container |
| `--interactive` | Keep input stream open       |
| `--tty`         | Allocate terminal            |
| `kafka1`        | Target Kafka container       |


### 2️⃣ Kafka Command

| Part                              | Meaning                               |
| --------------------------------- | ------------------------------------- |
| `kafka-topics`                    | Topic management CLI                  |
| `--bootstrap-server kafka1:19092` | Connect to broker (internal listener) |
| `--describe`                      | Show topic metadata                   |
| `--topic test-topic`              | Specific topic to inspect             |


### 3️⃣ What It Shows
| Output Field       | Description                   |
| ------------------ | ----------------------------- |
| Partitions         | Number of partitions          |
| Replication Factor | Number of replicas            |
| Leader             | Broker leading each partition |
| ISR                | In-sync replicas              |




## 3. Alter topic Partitions
```bash
docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 \
--alter --topic test-topic --partitions 40
```

### 1️⃣ Docker Command

| Part            | Purpose                                  |
| --------------- | ---------------------------------------- |
| `docker exec`   | Execute command inside running container |
| `--interactive` | Keep input stream open                   |
| `--tty`         | Allocate terminal                        |
| `kafka1`        | Target Kafka container                   |


### 2️⃣ Kafka Command

| Part                              | Purpose                               |
| --------------------------------- | ------------------------------------- |
| `kafka-topics`                    | Topic management CLI tool             |
| `--bootstrap-server kafka1:19092` | Connect to broker (internal listener) |
| `--alter`                         | Modify existing topic                 |
| `--topic test-topic`              | Target topic                          |
| `--partitions 40`                 | Increase partitions to 40             |

### ⚠️ Important Notes

| Rule                                     | Explanation                                 |
| ---------------------------------------- | ------------------------------------------- |
| Can only increase partitions             | Decreasing is not allowed                   |
| Existing messages stay in old partitions | New messages distributed across all 40      |
| Affects parallelism                      | More partitions = more consumer scalability |




## 4. How to view consumer groups
```bash
docker exec --interactive --tty kafka1  \
kafka-consumer-groups --bootstrap-server kafka1:19092 --list
```
### 1️⃣ Docker Part

| Command Part    | Purpose                                |
| --------------- | -------------------------------------- |
| `docker exec`   | Run command inside a running container |
| `--interactive` | Keep input stream open                 |
| `--tty`         | Allocate terminal session              |
| `kafka1`        | Target container                       |


### 2️⃣ Kafka Part

| Command Part                      | Purpose                                     |
| --------------------------------- | ------------------------------------------- |
| `kafka-consumer-groups`           | Manage and inspect consumer groups          |
| `--bootstrap-server kafka1:19092` | Connect to Kafka broker (internal listener) |
| `--list`                          | Show all active consumer groups             |

### ✅ Output

Displays all consumer group IDs currently registered in the Kafka cluster.

## 5. Consumer Groups and their Offset
```bash
docker exec --interactive --tty kafka1  \
kafka-consumer-groups --bootstrap-server kafka1:19092 \
--describe --group console-consumer-41911
```

### 1️⃣ Docker Part

| Command Part    | Purpose                              |
| --------------- | ------------------------------------ |
| `docker exec`   | Run command inside running container |
| `--interactive` | Keep STDIN open                      |
| `--tty`         | Allocate terminal                    |
| `kafka1`        | Target container                     |

### 2️⃣ Kafka Part

| Command Part                      | Purpose                               |
| --------------------------------- | ------------------------------------- |
| `kafka-consumer-groups`           | Inspect/manage consumer groups        |
| `--bootstrap-server kafka1:19092` | Connect to broker (internal listener) |
| `--describe`                      | Show detailed group information       |
| `--group console-consumer-41911`  | Target specific consumer group        |

### 3️⃣ What It Shows
| Field          | Meaning                    |
| -------------- | -------------------------- |
| TOPIC          | Topic being consumed       |
| PARTITION      | Partition number           |
| CURRENT-OFFSET | Last committed offset      |
| LOG-END-OFFSET | Latest offset in partition |
| LAG            | Messages not yet consumed  |
| CONSUMER-ID    | Active consumer instance   |
| HOST           | Consumer host              |
| CLIENT-ID      | Consumer client ID         |

### ✅ What It Does

Displays detailed offset, lag, and consumer activity for the specified consumer group.
# Log file and related config

## 1. Log into the container.
```bash
docker exec -it kafka1 bash
```
### 1️⃣ Docker Command Breakdown
| Part          | Meaning                                |
| ------------- | -------------------------------------- |
| `docker exec` | Run command inside a running container |
| `-i`          | Keep STDIN open (interactive mode)     |
| `-t`          | Allocate terminal (TTY session)        |
| `kafka1`      | Target container name                  |
| `bash`        | Start Bash shell inside container      |


### ✅ What It Does

Opens an interactive Bash terminal inside the kafka1 container so you can run Kafka CLI commands directly.


## 2. The config file is present in the below path.
```bash
/etc/kafka/server.properties
```

This file contains the **main Kafka broker configuration** inside the container.

---

### 📍 Location

| Path | Purpose |
|------|----------|
| `/etc/kafka/server.properties` | Default Kafka broker config file |

---

### 🔧 What It Defines

| Configuration Area | Example Settings |
|--------------------|------------------|
| Broker Identity | `broker.id` |
| Listeners | `listeners`, `advertised.listeners` |
| Log Storage | `log.dirs` |
| Replication | `offsets.topic.replication.factor` |
| Partitions | `num.partitions` |
| Networking | `inter.broker.listener.name` |

---

### ✅ What It Controls

This file determines:
- How the broker starts
- Where it listens
- How data is stored
- How replication works
- Cluster communication behavior


## 3. The log file is present in the below path.
```bash
/var/lib/kafka/data/
```

This directory stores **Kafka’s actual data and logs** inside the container.

---

### 📍 Location

| Path | Purpose |
|------|----------|
| `/var/lib/kafka/data/` | Kafka log directory (message storage) |

---

### 📦 What It Contains

| Item | Meaning |
|------|----------|
| Topic folders | Each topic-partition has its own folder |
| Log segments (`.log`) | Actual message data |
| Index files (`.index`, `.timeindex`) | Offset lookup optimization |
| `__consumer_offsets` | Internal consumer offset topic |
| `meta.properties` | Broker metadata (cluster ID, node ID) |

---

### ✅ What It Does

Stores all topic messages, offsets, and broker metadata.  
If this folder is deleted, Kafka loses its stored data.

# How to view the commit log?
```bash
docker exec --interactive --tty kafka1  \
kafka-run-class kafka.tools.DumpLogSegments \
--deep-iteration \
--files /var/lib/kafka/data/test-topic-0/00000000000000000000.log
```

### 1️⃣ Docker Part
| Part              | Meaning                                  |
| ----------------- | ---------------------------------------- |
| `docker exec -it` | Run interactive command inside container |
| `kafka1`          | Target Kafka container                   |


### 2️⃣ Kafka Tool Part

| Part                          | Meaning                              |
| ----------------------------- | ------------------------------------ |
| `kafka-run-class`             | Run internal Kafka Java class        |
| `kafka.tools.DumpLogSegments` | Tool to inspect raw log segment data |
| `--deep-iteration`            | Read and decode each record fully    |
| `--files`                     | Specify log segment file path        |




### 📂 File Path

| Path                                | Meaning                         |
| ----------------------------------- | ------------------------------- |
| `/var/lib/kafka/data/test-topic-0/` | Topic `test-topic`, partition 0 |
| `00000000000000000000.log`          | First log segment file          |



### ✅ What It Does

Reads and prints detailed internal message records from a Kafka log segment file, including offsets, keys, values, timestamps, and metadata.



------
# In Details
## 1. List the topics in a cluster
```bash
docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 --list
```
------
## 2. Describe topic
**Command to describe all the Kafka topics.**

```bash
docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 --describe
```

- **Command to describe a specific Kafka topic.**
```bash
docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 --describe \
--topic test-topic
```

## 3. Alter topic Partitions
```bash
docker exec --interactive --tty kafka1  \
kafka-topics --bootstrap-server kafka1:19092 \
--alter --topic test-topic --partitions 40
```

## 4. How to view consumer groups
```bash
docker exec --interactive --tty kafka1  \
kafka-consumer-groups --bootstrap-server kafka1:19092 --list
```

## 5. Consumer Groups and their Offset
```bash
docker exec --interactive --tty kafka1  \
kafka-consumer-groups --bootstrap-server kafka1:19092 \
--describe --group console-consumer-41911
```


# Log file and related config

## 1. Log into the container.
```bash
docker exec -it kafka1 bash
```

## 2. The config file is present in the below path.
```bash
/etc/kafka/server.properties
```
#### server.properties 
```properties
broker.id=0
num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
log.dirs=/var/lib/kafka
num.partitions=1
num.recovery.threads.per.data.dir=1
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1
log.retention.hours=168
log.retention.check.interval.ms=300000
zookeeper.connect=localhost:2181
zookeeper.connection.timeout.ms=18000
group.initial.rebalance.delay.ms=0
```

# Kafka `server.properties` — Summary Tables

---

## 1️⃣ Broker Identity

| Property | Meaning |
|-----------|----------|
| `broker.id=0` | Unique ID of this Kafka broker |

---

## 2️⃣ Thread & Performance Settings

| Property | Meaning |
|-----------|----------|
| `num.network.threads=3` | Threads handling network requests |
| `num.io.threads=8` | Threads for disk I/O operations |
| `num.recovery.threads.per.data.dir=1` | Threads used during log recovery |

---

## 3️⃣ Socket & Network Buffers

| Property | Meaning |
|-----------|----------|
| `socket.send.buffer.bytes=102400` | Send buffer size |
| `socket.receive.buffer.bytes=102400` | Receive buffer size |
| `socket.request.max.bytes=104857600` | Max request size (100MB) |

---

## 4️⃣ Log & Storage Configuration

| Property | Meaning |
|-----------|----------|
| `log.dirs=/var/lib/kafka` | Directory storing Kafka logs |
| `num.partitions=1` | Default partitions per topic |
| `log.retention.hours=168` | Retain messages for 7 days |
| `log.retention.check.interval.ms=300000` | Retention check every 5 minutes |

---

## 5️⃣ Replication & Transactions (Single Broker Setup)

| Property | Meaning |
|-----------|----------|
| `offsets.topic.replication.factor=1` | Replication factor for offsets topic |
| `transaction.state.log.replication.factor=1` | Transaction log replication |
| `transaction.state.log.min.isr=1` | Minimum in-sync replicas |

---

## 6️⃣ ZooKeeper Configuration (Pre-KRaft Mode)

| Property | Meaning |
|-----------|----------|
| `zookeeper.connect=localhost:2181` | ZooKeeper connection string |
| `zookeeper.connection.timeout.ms=18000` | ZooKeeper connection timeout |

> ⚠ This indicates **ZooKeeper-based Kafka**, not KRaft mode.

---

## 7️⃣ Consumer Group Behavior

| Property | Meaning |
|-----------|----------|
| `group.initial.rebalance.delay.ms=0` | No delay before first consumer rebalance |

---

## ✅ Overall

This configuration represents a **single-node Kafka broker**, using **ZooKeeper mode**, optimized for development/testing with minimal replication and default performance settings.


-------

## 3. The log file is present in the below path.
```bash
/var/lib/kafka/data/
```

# How to view the commit log?
```bash
docker exec --interactive --tty kafka1  \
kafka-run-class kafka.tools.DumpLogSegments \
--deep-iteration \
--files /var/lib/kafka/data/test-topic-0/00000000000000000000.log
```


