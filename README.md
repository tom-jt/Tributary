This library and API is based on a heavily simplified version of the event streaming infrastructure Apache Kafka.

# Tributary Cluster and Topics

A Tributary Cluster contains a series of topics. A topic contains events which are logically grouped together.

<img width="477" alt="tributaryClusterExample" src="https://github.com/user-attachments/assets/18b4008a-2746-4d0d-b161-b57b4f718bf4" />

# Topics and Partitions

Within each topic, there are a series of partitions - each partition is a queue where new messages are appended at the end of the partition.

<img width="819" alt="topicExample" src="https://github.com/user-attachments/assets/b12cdad8-a729-42f9-9882-2691cbec8e54" />

# Message

A unit of data within a Tributary with generic-typed event data.

<img width="455" alt="messageStructure" src="https://github.com/user-attachments/assets/debfc1a8-dcc5-41d0-a7d6-c0dc3e0432b8" />

# Producers

A Producer is responsible for sending messages to the Tributary system. Producers can indicate whether to send a message to a particular partition by providing the corresponding partition key or requesting random allocation. There are two types of producers:

- Random Producers: the producer requests the Tributary system to randomly assign a message to a partition

- Manual Producers: the producer requests the Tributary system to assign a message to a particular partition by providing its corresponding key.

Once a producer has been created with one of the two above message allocation methods, it cannot change its message allocation method.

# Consumers and Consumer Groups

Consumers are responsible for consuming (processing) messages stored in partition queues. A consumer consumes messages from a partition in the order that they were produced, and keeps track of the messages that have been consumed. Consumers operate as part of a consumer group. Each partition can be consumed by only one consumer per consumer group. Consumers can consume from more than one partition within a consumer group.

A consumer group consists of one or more consumers, that are together capable of consuming from all the partitions in a topic.

Each topic can have multiple consumer groups. While each consumer group assigned to the same topic may contain a different number of consumers, they will all consume from the same number of partitions, i.e. all the partitions in a topic will always be handled by any consumer group assigned to the topic.

When a new consumer group is created, the consumers in the group begin their consumption from the first unconsumed message in all of the topics partitions they are assigned to. I.e., all consumers that share a partition consume messages parallel to each other, so that each message is only consumed once (except in controlled replays).

<img width="691" alt="consumerAllocation1" src="https://github.com/user-attachments/assets/98028aa5-27cb-4cd8-9fb0-a0487550f371" />

<img width="685" alt="consumerAllocation2" src="https://github.com/user-attachments/assets/bfa19da4-b498-4eeb-97e9-66f58315dccb" />

## Consumer Rebalancing

The system is be able to dynamically change the rebalancing strategy between one of two rebalancing strategies - range rebalancing, and round robin rebalancing.

Range - The partitions are divided up evenly and allocated to the consumers. If there is an odd number of partitions, the first consumer takes one extra.

<img width="414" alt="rangeAllocation" src="https://github.com/user-attachments/assets/898a55e9-61ed-48a0-b85a-0ae530a8be18" />

Round Robin - In a round robin fashion, the partitions are allocated like cards being dealt out, where consumers take turns being allocated the next partition.

<img width="392" alt="roundAllocation" src="https://github.com/user-attachments/assets/075d78ee-92e2-4d5c-9d3e-58ed891a22d5" />

# Replay Messages

A controlled replay takes in some point in time and allows messages from that point onwards to be streamed through the pipeline, until the most recent message at the latest offset is reached.

<img width="774" alt="controlledReplay" src="https://github.com/user-attachments/assets/ab57cf80-a249-4800-a702-edc900ac5598" />
