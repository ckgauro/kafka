## Setting up Kafka Broker

# Docker Compose Notes: Single Kafka Broker (KRaft mode) — Line-by-line Explanation

This `docker-compose.yml` snippet runs **one Kafka broker** using Confluent’s Kafka image (`cp-kafka:7.4.0`) and configures it in **KRaft mode** (Kafka without ZooKeeper).

## Full snippet (for reference)

```bash
services:
  kafka1:
    image: confluentinc/cp-kafka:7.4.0
    hostname: kafka1
    container_name: kafka1
    ports:
      - "9092:9092"
      - "29092:29092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT,DOCKER:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka1:19092,EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:9092,DOCKER://host.docker.internal:29092
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka1:9093
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: CONTROLLER://kafka1:9093,INTERNAL://kafka1:19092,EXTERNAL://0.0.0.0:9092,DOCKER://0.0.0.0:29092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk

```

## 1) `services.kafka1` — what you’re running
`image: confluentinc/cp-kafka:7.4.0`

Runs Kafka from Confluent Platform **7.4.0** image.

`hostname`: **kafka1** and `container_name`: **kafka1**
- `hostname`: what the container calls itself on the Docker network. Is `hostname` the machine name given to Kafka? Short answer: **No — not your physical machine name.**. It is the **container’s internal network hostname inside Docker**.
- `container_name`: the actual Docker container name (useful for logs, exec, etc).

## 2) Port mapping (host ↔ container)

```yml
ports:
  - "9092:9092"
  - "29092:29092"

```
This exposes **two Kafka listener** ports to your machine:
| Host Port (Your Machine) | Container Port (Kafka Inside Docker) | Intended client                                                         |
| --------: | -------------: | ----------------------------------------------------------------------- |
|      9092 |           9092 | Typical “connect from my laptop” port.Laptop → Docker → Kafka                                   |
|     29092 |          29092 | Special case for *Docker Desktop host routing* (`host.docker.internal`). Special routing path via Docker Desktop |

> Kafka commonly needs different ports/addresses depending on where the client runs (same docker network vs your local machine vs another container using host routing).

## 3) Key concept: Kafka Listeners vs Advertised Listeners

Kafka networking has **two related settings**:

**A) KAFKA_LISTENERS**

Where Kafka **binds** and listens inside the container.
- Think: “Which network interfaces/ports should Kafka open?”

**B) KAFKA_ADVERTISED_LISTENERS**

What Kafka **tells clients** to connect to.
- Think: “When a client asks for broker address, what host:port should Kafka return?”

If these don’t match your real network reality, you get classic errors like:
- client connects but then fails on metadata fetch
- “connection to node -1 could not be established”
- works in container but not from host (or vice versa)


## 4) Environment variables — detailed explanation

`KAFKA_NODE_ID: 1`

A unique integer ID for this node in KRaft mode.
- In multi-node setups each broker/controller has its own node id.

`KAFKA_PROCESS_ROLES: broker,controller`

This is the big KRaft flag.

It means this single node is:
- **broker** (handles produce/consume requests)
- **controller** (handles cluster metadata leadership)

So this is a **combined broker+controller** node (common for dev/single-node).

`CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk`

KRaft clusters require a **cluster id**.
- It’s used to format and identify the cluster metadata log.
- In real setups you usually generate it once and keep it stable.

`KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER`

Tells Kafka: “The listener named `CONTROLLER` is the one controllers use to talk to each other / run controller quorum traffic”.

`KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka1:9093`

Defines the controller quorum membership.

Format:

<nodeId>@<host>:<controllerPort>

Here it means:
- node 1
- reachable at kafka1:9093
- and it’s the only voter (single-node quorum)

`KAFKA_LISTENER_SECURITY_PROTOCOL_MAP`

```yaml
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT,DOCKER:PLAINTEXT
```

Kafka supports multiple listeners, each with a “name”.
This mapping says each listener uses **PLAINTEXT** (no TLS, no SASL).

Listener names in this file:
- `CONTROLLER` — controller quorum traffic
- `INTERNAL` — inter-broker + container-to-container traffic on docker network
- `EXTERNAL` — host machine clients connecting via `localhost` (or host IP)
- `DOCKER` — clients connecting using `host.docker.internal`

`KAFKA_LISTENERS`

```yaml
KAFKA_LISTENERS: CONTROLLER://kafka1:9093,INTERNAL://kafka1:19092,EXTERNAL://0.0.0.0:9092,DOCKER://0.0.0.0:29092
```

This is where the broker **binds**:

| Listener name | Bind address    | Purpose                                                    |
| ------------- | --------------- | ---------------------------------------------------------- |
| CONTROLLER    | `kafka1:9093`   | KRaft controller quorum                                    |
| INTERNAL      | `kafka1:19092`  | broker-to-broker + other containers on same Docker network |
| EXTERNAL      | `0.0.0.0:9092`  | allow connections coming into container on 9092            |
| DOCKER        | `0.0.0.0:29092` | allow connections coming into container on 29092           |


Notes:
- `0.0.0.0` means “listen on all interfaces” (inside container).
- `kafka1:19092` is a common “internal docker listener” pattern.

`KAFKA_ADVERTISED_LISTENERS`

```yaml
KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka1:19092,EXTERNAL://${DOCKER_HOST_IP:-127.0.0.1}:9092,DOCKER://host.docker.internal:29092
```

This is what Kafka returns to clients as “connect here”.

| Listener name | Advertised address                  | Who it helps                                                      |
| ------------- | ----------------------------------- | ----------------------------------------------------------------- |
| INTERNAL      | `kafka1:19092`                      | other containers using docker DNS (same compose network)          |
| EXTERNAL      | `${DOCKER_HOST_IP:-127.0.0.1}:9092` | your laptop apps using localhost (or a specific host IP)          |
| DOCKER        | `host.docker.internal:29092`        | containers that need to reach the host via Docker Desktop routing |



Key detail:
- `${DOCKER_HOST_IP:-127.0.0.1}` means:
    - if `DOCKER_HOST_IP` env var exists, use it
    - otherwise default to `127.0.0.1`
- Useful when `localhost` is not correct (remote docker host, VM, etc).

`KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL`

This picks which listener name Kafka uses for **broker-to-broker communication**.

Here:
- brokers talk to each other via `INTERNAL` (even though you only have 1 broker right now)

Why it matters:
- In multi-broker clusters, this must be reachable between brokers (usually docker network DNS).

## 5) Single-node replication + transactions settings (dev-friendly)


`KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1`

Kafka’s internal `__consumer_offsets` topic replication factor.
- In production this is usually **>= 3**
- In a single-broker dev setup it **must be 1**.

`KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1`

Replication factor for transaction state log.
- Must be 1 for single broker.

`KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1`

Minimum in-sync replicas for transaction log.
- Single broker means min ISR must be 1 or transactions won’t work.

## 6) How to connect (common cases)

**From your host machine (your laptop)**

Use the `EXTERNAL` listener:
 - `localhost:9092` (if `DOCKER_HOST_IP` not set)
- or `${DOCKER_HOST_IP}:9092`

**From another container on the same docker-compose network**

Use the `INTERNAL` listener:

- `kafka1:19092`

**From a container that uses host routing (Docker Desktop specific)**

Use the `DOCKER` listener:
- `host.docker.internal:29092`

## 7) Why have 3 listener types (INTERNAL / EXTERNAL / DOCKER)?

Because Kafka clients need different “return addresses” depending on where they run:
- **Inside docker network**: `kafka1` is resolvable via Docker DNS → `kafka1:19092`
- **On your laptop**: `kafka1` is NOT resolvable → you need `127.0.0.1:9092` (or host IP)
- **Some docker host routing scenarios**: easiest is `host.docker.internal:29092`

This compose is basically making Kafka “work everywhere” in a dev environment without constantly changing configs.

## 8) Common pitfalls (and quick checks)
- If host apps connect to `9092` but fail after initial connection:
    - usually `KAFKA_ADVERTISED_LISTENERS` is wrong (Kafka returns an unreachable address).

- If a container can’t connect using `localhost:9092`:
 - inside containers, `localhost` means that container, not your host.
 - use `kafka1:19092` or `host.docker.internal:29092` depending on scenario.

### memorize this
- `LISTENERS` = where Kafka binds
- `ADVERTISED_LISTENERS` = what Kafka **announces**
- `INTERNAL` = container-to-container
- `EXTERNAL` = your laptop (localhost / host IP)
- `DOCKER` = Docker Desktop host routing (host.docker.internal)
- KRaft mode is enabled by:
    - `KAFKA_PROCESS_ROLES`
    - `KAFKA_NODE_ID`
    - `KAFKA_CONTROLLER_*`
    - `CLUSTER_ID`

----------
# In Details

# image: confluentinc/cp-kafka:7.4.0 — Detailed Explanation

This line tells Docker **which image to pull and run** for your Kafka container.

```yaml
image: confluentinc/cp-kafka:7.4.0
```

## 1️⃣ Image Name Structure

Docker image format:

```code
<repository>/<image-name>:<tag>
```
| Part           | Meaning                             |
| -------------- | ----------------------------------- |
| `confluentinc` | Docker Hub organization (publisher) |
| `cp-kafka`     | Image name                          |
| `7.4.0`        | Version tag                         |


## 2️⃣ What is confluentinc?

`confluentinc` is the official Docker Hub organization of Confluent, the company founded by Kafka’s original creators.

Confluent maintains production-grade Kafka container images.

This is not a random community image.

3️⃣ What is cp-kafka?

cp-kafka stands for:

Confluent Platform - Kafka

This image contains:

Apache Kafka broker

Confluent packaging scripts

KRaft mode support

Production-ready configurations

Health checks and startup logic

It is not just “vanilla Apache Kafka binaries.”
It includes Confluent’s runtime tooling.