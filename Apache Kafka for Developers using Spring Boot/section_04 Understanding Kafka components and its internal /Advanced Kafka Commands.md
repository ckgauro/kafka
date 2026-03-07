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

## 1️⃣ What Is This Directory `/var/lib/kafka/data/`?

| Item | Meaning |
|------|----------|
| `/var/lib/kafka/data/` | Default Kafka data storage directory |
| Purpose | Stores all topic messages and broker metadata |
| Used by | Kafka broker at runtime |

---

## 2️⃣ What It Contains

| Type | Example | Description |
|------|----------|-------------|
| Topic-Partition Folders | `test-topic-0/` | One folder per topic partition |
| Log Segment Files | `00000000000000000000.log` | Actual message data |
| Offset Index Files | `.index` | Offset lookup index |
| Time Index Files | `.timeindex` | Timestamp lookup index |
| Snapshot Files | `.snapshot` | Leader epoch metadata |
| Metadata File | `meta.properties` | Cluster ID & broker ID info |
| Internal Topics | `__consumer_offsets-*` | Consumer group offsets storage |

---

## 3️⃣ Topic-Partition Folder Structure

Example: `test-topic-0/`

| File | Purpose |
|------|----------|
| `00000000000000000008.log` | Message segment file |
| `00000000000000000008.index` | Offset index |
| `00000000000000000008.timeindex` | Time-based index |
| `leader-epoch-checkpoint` | Leader epoch tracking |
| `partition.metadata` | Partition metadata |

---

## 4️⃣ How Kafka Uses This Directory

| Function | Explanation |
|-----------|------------|
| Message Storage | All produced messages are written to `.log` files |
| Segment Rolling | Kafka creates new segment files after size/time limits |
| Retention Cleanup | Old segments deleted based on retention rules |
| Recovery | Broker rebuilds state from these logs after restart |

---

## 5️⃣ Important Notes

| Scenario | Impact |
|----------|--------|
| Delete this folder | All topic data is lost |
| No Docker volume mounted | Data lost when container is recreated |
| Multiple brokers | Each broker has its own data directory |

---

## ✅ Summary

`/var/lib/kafka/data/` is the **physical storage layer of Kafka**.  
It contains all topic messages, partition logs, indexes, and broker metadata.

# How to view the commit log?
```bash
docker exec --interactive --tty kafka1  \
kafka-run-class kafka.tools.DumpLogSegments \
--deep-iteration \
--files /var/lib/kafka/data/test-topic-0/00000000000000000000.log
```

## 1️⃣ Docker Part

| Part            | Meaning                                  |
| --------------- | ---------------------------------------- |
| `docker exec`   | Run a command inside a running container |
| `--interactive` | Keep STDIN open (interactive input)      |
| `--tty`         | Allocate a terminal session              |
| `kafka1`        | Container name to run the command in     |


## 2️⃣ Kafka Runtime Part

| Part                          | Meaning                                     |
| ----------------------------- | ------------------------------------------- |
| `kafka-run-class`             | Runs a Kafka internal Java class (tooling)  |
| `kafka.tools.DumpLogSegments` | Reads and prints Kafka log segment contents |



## 3️⃣ Options
| Option             | Meaning                                                     |
| ------------------ | ----------------------------------------------------------- |
| `--deep-iteration` | Iterates through records and decodes details (more verbose) |
| `--files <path>`   | Path to the **segment `.log` file** you want to inspect     |


## 4️⃣ File Being Read

| Path Part                  | Meaning                                                |
| -------------------------- | ------------------------------------------------------ |
| `/var/lib/kafka/data/`     | Kafka data directory                                   |
| `test-topic-0/`            | Topic `test-topic`, partition `0`                      |
| `00000000000000000000.log` | Segment file starting at offset **0** (oldest segment) |


## ✅ What It Does (Output)

| Output Content   | You’ll See                             |
| ---------------- | -------------------------------------- |
| Segment metadata | base offset, size, timestamps          |
| Record details   | offset, key, value, headers, timestamp |
| Batch info       | batch size, compression, magic version |


## ⚠ Common Note

If the file doesn’t exist (or old segments were deleted by retention), this command won’t show older messages because there’s nothing left on disk.


```bash
kafka-run-class kafka.tools.DumpLogSegments --deep-iteration --files /var/lib/kafka/data/test-topic-0/00000000000000000008.log 
```

**output**
```text
Dumping /var/lib/kafka/data/test-topic-0/00000000000000000008.log
Log starting offset: 8
baseOffset: 8 lastOffset: 8 count: 1 baseSequence: 0 lastSequence: 0 producerId: 1000 producerEpoch: 0 partitionLeaderEpoch: 5 isTransactional: false isControl: false deleteHorizonMs: OptionalLong.empty position: 0 CreateTime: 1772383672586 size: 79 magic: 2 compresscodec: none crc: 306771344 isvalid: true
| offset: 8 CreateTime: 1772383672586 keySize: -1 valueSize: 11 sequence: 0 headerKeys: []
baseOffset: 9 lastOffset: 9 count: 1 baseSequence: 1 lastSequence: 1 producerId: 1000 producerEpoch: 0 partitionLeaderEpoch: 5 isTransactional: false isControl: false deleteHorizonMs: OptionalLong.empty position: 79 CreateTime: 1772383740229 size: 72 magic: 2 compresscodec: none crc: 1323801164 isvalid: true
| offset: 9 CreateTime: 1772383740229 keySize: -1 valueSize: 4 sequence: 1 headerKeys: []
baseOffset: 10 lastOffset: 10 count: 1 baseSequence: 2 lastSequence: 2 producerId: 1000 producerEpoch: 0 partitionLeaderEpoch: 5 isTransactional: false isControl: false deleteHorizonMs: OptionalLong.empty position: 151 CreateTime: 1772383742950 size: 74 magic: 2 compresscodec: none crc: 2359339998 isvalid: true
| offset: 10 CreateTime: 1772383742950 keySize: -1 valueSize: 6 sequence: 2 headerKeys: []
```

# DumpLogSegments Output — Summary Tables

---

## 1️⃣ Segment Information

| Field | Value | Meaning |
|-------|-------|----------|
| File | `00000000000000000008.log` | Segment file name |
| Log starting offset | `8` | First offset stored in this segment |
| Partition | `test-topic-0` | Topic `test-topic`, partition 0 |

---

## 2️⃣ Record Batch #1 (Offset 8)

| Field | Value | Meaning |
|-------|-------|----------|
| baseOffset / lastOffset | 8 / 8 | This batch contains offset 8 only |
| count | 1 | One record in batch |
| producerId | 1000 | Producer ID |
| producerEpoch | 0 | Producer epoch |
| baseSequence / lastSequence | 0 / 0 | First sequence number |
| partitionLeaderEpoch | 5 | Leader epoch at write time |
| isTransactional | false | Not transactional |
| compression | none | No compression |
| size | 79 bytes | Batch size |
| isValid | true | Record integrity valid |
| valueSize | 11 bytes | Message payload size |

---

## 3️⃣ Record Batch #2 (Offset 9)

| Field | Value | Meaning |
|-------|-------|----------|
| baseOffset / lastOffset | 9 / 9 | Single record batch |
| count | 1 | One record |
| sequence | 1 | Producer sequence incremented |
| size | 72 bytes | Batch size |
| valueSize | 4 bytes | Message payload size |
| isValid | true | CRC valid |

---

## 4️⃣ Record Batch #3 (Offset 10)

| Field | Value | Meaning |
|-------|-------|----------|
| baseOffset / lastOffset | 10 / 10 | Single record batch |
| count | 1 | One record |
| sequence | 2 | Producer sequence incremented |
| size | 74 bytes | Batch size |
| valueSize | 6 bytes | Message payload size |
| isValid | true | CRC valid |

---

## 5️⃣ General Observations

| Observation | Meaning |
|-------------|----------|
| Offsets 8, 9, 10 | Three messages exist in this segment |
| Sequence numbers 0 → 2 | Same producer sending ordered messages |
| No compression | Messages stored uncompressed |
| Non-transactional | Normal producer (not transactional) |
| Valid CRC | Data integrity confirmed |

---

## ✅ Summary

This segment file contains **three valid messages** at offsets **8, 9, and 10**, produced by the same producer (ID 1000), written sequentially, uncompressed, and non-transactional.
