# Google Docs Link 🌍

https://docs.google.com/document/d/1SbrVqJ8AbTkvvLjRMnK0vXr1_e1zcFSWv7Ho0xV41bM/edit?usp=sharing

# Task 1) Preliminary Design (5 marks) 🏛️

## An analysis of the engineering requirements

The system requirements outline that the system must be able to create a number of producers and consumers which can produce messages to topics and their respective partitions, as well as consume messages from partitions. These behaviours must be able to be carried out concurrently. They all exist under a single cluster, which will be represented through the singleton pattern so that it can be accessed by every producer and consumer conveniently.

Multiple consumers can read from a single partition concurrently, meaning they will sequentially read the next available unread message in the partition they have been assigned. Producers are able to either allocate randomly or manually to partitions through their allocation strategy.

The system requires an API which allows functionality from the internal workings of the system to be called and used by other developers when implementing systems that require producers and consumers which work concurrently (such as a system scheduler). The API functions will be able to return values such as values of generic message type which can be helpful for developers using the API. Consumers need to be able to replay their consumed messages from a specified point onwards. Consumers need to track their consumed messages so that they can be replayed. The consumer must know which partition these messages came from in order to determine which messages to replay.

As consumers/groups can be actively added into the system, rebalancing strategies need to be made to reassign consumers to partitions to balance loads. The system should be able to dynamically switch between these two strategies making the strategy pattern appropriate.

Additionally, the system requires a CLI which we can utilise to perform operations on the system (for testing purposes and also to show how the system works). The CLI will be able to call API functions and methods to invoke internal system functionality. The CLI will use generic messages returned from the API to output information.

To implement the above requirements, we will utilise the structure below.

Classes & relevant fields/methods

- **Tributary Cluster**
  - Singleton
  - Has a list of **Topics**
  - write(String topicId, Message msg, AllocationStrategy allocStrat) returns Message
  - read(String topicId, String partitionId) returns void
  - createTopic(String id, T type) returns void
  - removeTopic(String id) returns boolean
  - createPartition(String topicId, String partitionId) returns void
  - removePartition(String topicId, String partitionId) returns boolean
  - createConsumerGroup(String groupId, String topicId, RebalancingStrategy rebalanceStrat) returns void
- **Topic**
  - Generic event type
  - Id {get; private set;}
  - Has a list of **Partitions**
  - createPartition(String partitionId) returns void
  - removePartition(String partitionId) returns boolean
  - write(String partitionId, Message msg) returns Message
  - read(String partitionId) returns void
  - getPartitionIds() returns List&lt;String>
- **Partition**
  - Id {get; private set;}
  - File pointer to current message consuming (if we are not deleting as we go)
  - Synchronised write(Message msg) returns void
  - Synchronised read(String id) returns Message
- **Producer**
  - Id {get; private set;}
  - Generic event type
  - Has a **Allocation Strategy**
  - produce(String topicId, String file) returns void
  - produce(String topicId, String file, String partitionId) returns void
- **Allocation Strategy**
  - Abstract class
  - Abstract execute(List&lt;String> ids) returns String
  - Extended by
    - **Random Allocation**
      - Overrides execute(List&lt;String> ids)
    - **Manual Allocation**
      - partitionId {private get; set;}
      - Overrides execute(List&lt;String> ids)
- **Consumer**
  - Has a list of **Partitions** consuming from
  - Has a list of **Message** that it has consumed
- **Consumer Group**
  - Id {get; private set;}
  - Rebalancing strategy
  - TopicId
  - Has a list of **Consumers**
- **Rebalance Strategy**
  - Extended by
    - **Round Robin Rebalance**
    - **Range Rebalance**
- **Message**
  - Has a **Header**
    - Datetime
    - ID
    - Payload type
  - Key
  - Value

## A list of **usability tests** - a “checklist” of scenarios which someone testing your system via the command line interface could use to verify that your system works according to the requirements.

Ensure producers produce and consumers consume.

- Create cluster
- Create topic and partition
- Create one producer
- Create one consumer group
- Create one consumer assigned to the group.
- Produce
- Print all topics and partitions
- Consume
- Replay consumer

Ensure random allocation and manual allocation works

- Create cluster
- Create topic and multiple partitions
- Create producer
- Produce randomly
- Print topics and partitions
- Produce to selected partition
- Print topics and partitions.

Ensure round robin rebalancing works

- Create cluster
- Create topic with seven partitions
- Create producer and produce unique messages to each partition.
- Create consumer group with round-robin rebalancing
- Create 2 consumers and assign to group.
- Consume from all in parallel.
- Replay both consumers to observe that the first read from partition 0, 2, 4, 6 and the second read from 1, 3, 5.
- Create new consumer and add to group.
- Produce unique messages to all partitions.
- Consume from all in parallel.
- Replay both consumers to observe the first read from partition 0, 3, 6 and the second read from 1, 4 and the third read from 2, 5.

Ensure range rebalancing works

- Create cluster
- Create topic with seven partitions
- Create producer and produce unique messages to each partition.
- Create consumer group with range rebalancing
- Create 2 consumers and assign to group.
- Consume from all in parallel.
- Replay both consumers to observe that the first read from partition 0, 1, 2, 3 and the second read from 4, 5, 6.
- Create new consumer and add to group.
- Produce unique messages to all partitions.
- Consume from all in parallel.
- Replay both consumers to observe the first read from partition 0, 1, 2 and the second read from 3, 4 and the third read from 5, 6.

Ensure parallel producing/consuming works

- Create cluster
- Create topic with one partition
- Create three producers as random allocation
- Using the parallel produce command, initiate production of nine events (three per producer). The order is specified based on the order given in the command arguments.
- Print topics and partitions and ensure order/information matches.
- Create three consumer groups
- Create one consumer per group and assign to each group.
- Run parallel consume.
- Replay each consumer to ensure there are no duplicates or missing messages.

## An initial **UML diagram** showing the entities, relationships between entities and key methods/fields in each entity (does not have to be a complete list, it just needs to be a first-version API)

See PDFs **uml-v1** and **uml-v2**.

## Your design for a **Java API** by which someone could use your solution to setup an event-driven system.

Features we need

- Create a system cluster.
- Create a topic
- Create a partition
- Create a producer
- Create a consumer
- Create a consumer group
- Tell a producer to produce an item
- Tell a consumer to consume from a source.
- Tell many producers to produce many items.
- Tell all consumers to consume anything when produced.
- Tell a consumer to replay all consumed events (with constraints)
- Update rebalancing method for a consumer group
- Delete a consumer
- Print topic / print partition

Specific implementation

- Controllers
  - **TributaryController**
    - Interacts with the **TributaryCluster**
    - Exposed methods
      - CreateTopic()
      - RemoveTopic()
      - CreatePartition()
      - RemovePartition()
      - ShowTopic()
  - **ProducerController**
    - Stores and interacts with **Producers**
    - Exposed methods
      - CreateProducer()
      - RemoveProducer()
      - Produce()
  - **ConsumerController**
    - Stores and intereacts with **ConsumerGroups **and by extension **Consumers**
    - Exposed methods
      - CreateConsumer()
      - RemoveConsumer()
      - CreateConsumerGroup()
      - Consume()
      - ShowConsumerGroup()
      - SetRebalancingStrategy()
- Responses
  - **TopicResponse**
    - Stores the Topic ID, its Partition IDs, and their respective list of events
  - **ConsumerGroupResponse**
    - Stores the CustomerGroup ID, its Consumer IDs, and their respective list of Partition IDs they are subscribed to

## A **testing plan** which explains how you are going to structure your tests for the system. You will need a mix of unit tests on individual components as well as integration tests to check that components work together. Your usability tests will need to be incorporated into this plan as well.

**Unit tests:**

- Create cluster and ensure cluster exists
- Create topic and ensure empty topic exists
- Create partitions one at a time, ensure they exist
- Create multiple topics and create partitions in each.
- Create producer and ensure it exists
- Create consumer group and ensure it exists and is empty
- Create two consumer groups and assign one consumer to one only
- Create producer and produce, ensure message is returned and in partition.
- Create consumer and consume from partition. Ensure partition is empty and consumer has the item.
- Replay consumer which has consumed message. Ensure message = original message.
- Delete a consumer. Ensure it has been removed from system.

**Integration tests (including usability tests converted to run with Gradle):**

- Ensure producers produce and consumers consume.
- Ensure random allocation and manual allocation works
- Ensure round robin rebalancing works
- Ensure range rebalancing works
- Ensure parallel producing/consuming works
- Ensure runtime change in rebalancing works

## When it comes to implementation, there are two ways you can go about implementing a solution:

- **Feature-driven** - essentially, working through the table in Section 3.2 and getting one section working at a time. This approach is easier to usability test incrementally, harder to incorporate with multiple people, and can result in less cohesive software design and test design (more of an Incremental Design Approach)
- **Component-driven** - creating each component and writing unit tests individually, before bringing the pieces together and usability testing at the end. This approach is harder to usability test incrementally, easier to incorporate with multiple people but can result in more cohesive software and test design (more of a Big Design Up Front Approach)

You will need to pick one approach and justify your decision.

**Our choice: component-driven**

Reasoning: The system can be very nicely split into 3 components, Tributary Cluster, Consumers, and Producers. These components can be completed and unit tested, before bringing them together to achieve the final product. Compared to feature-driven implementation, this is also easier for multiple people to work on different components simultaneously without sacrificing cohesive software design. Also, as it is similar to the BDUF approach, we already have a preliminary design set out, so it aligns with this approach more as we have a good idea of the required components and structure.

# Task 2 Video Link 💻

https://youtu.be/CxUjUlpxSY4

# Task 3) Final Design (15 marks) 🏢

## Your final testing plan and your final list of usability tests

**Usability tests**

- See usability tests in **app/src/test/java/tributary/input.md** for a list usability tests (each usability test has a ‘##’ heading at the start, e.g. ‘## Simple message lifecycle’) and a very detailed list of the associated commands ran for each test. It is quite long.

**Unit and Integration tests**

- Create cluster
- Create topic in cluster
- Create partition in topic
- Create multiple partitions in multiple topics
- Assign consumer to group and topic
- Test producer producing to partition
- Test consumer consuming from partition
- Test consumer playing back consumed events
- Test consumer removal
- Test partition removal (and rebalance)
- Test simple message lifecycle
- Test random producer allocation
- Test manual producer allocation
- Test range rebalancing
- Test round robin rebalancing
- Test changing rebalancing method at runtime
- Test parallel consume
- Test parallel produce

## An overview of the Design Patterns used in your solution

**Singleton pattern:**

We used the singleton pattern to represent our Tributary Cluster. This is because the tributary cluster always exists in only one instance for all producers, consumers and topics. By using the singleton pattern, we are able to create and pass global references of the tributary cluster to any class that needs to use it through the static method getInstance(). It allows consumers and producers to interact with partitions and topics which reducing coupling and increasing code cohesion, as rather than producers and consumers being highly coupled with topics, the cluster and partitions, they can remain loosely coupled with the partition only and can use it for interacting with other classes in the system.

**Strategy pattern:**

We used the strategy pattern twice in our system in situations where system behaviour for certain instances of classes could be chosen by the user/developer at runtime. These include choosing the allocation strategy for producers (either Random allocation or Manual allocation). Additionally, we used the strategy pattern for the rebalancing strategies of consumer groups (Range and RoundRobin). This choice was trivial as it was clear that the specific strategy could be chosen and modified by the user during runtime (through our CLI there is a command which allows for users to change the rebalancing strategy of consumer groups for example). The use of the strategy pattern adheres to the open / close principle as it allows for new strategies to be added promoting extension of the system without needing much modification.

**Observer pattern:**

We used the observer pattern in the system to allow the tributary cluster to notify consumer groups when partitions are added or removed from topics. Consumer groups subscribe to the tributary cluster when created and rebalancing must occur when consumers are added and deleted, and when partitions are added and deleted. Rebalancing is handled within the consumer group class, so rebalancing when adding and removing consumers is easy as this is all isolated to the consumer class and is highly cohesive without requiring coupling. However, deletion and addition of partitions is handled through the cluster, meaning notifying consumers would require high coupling. To combat this, we employed the observer pattern, as consumer groups can observe the tributary cluster and be notified by the tributary cluster whenever a deletion happens. Using this pattern allows us to preserve a low amount of coupling between the consumer and cluster classes while achieving our desired functionality.

## Explanation of how you accommodated for the design considerations in your solution

**Concurrency:**

When designing concurrent solutions, considerations need to be made to prevent concurrency issues from occurring such as race conditions, livelock and deadlock. Livelock and deadlock are unable to occur in our solution as we utilise “synchronized” methods. Livelock occurs when threads constantly try to take over from each other meaning no actual code is being executed (all the instructions being executed by the computer is just blocking and waking threads), and deadlock occurs when two threads need synchronisation primitives from each other (such as locks) and hence put each other to sleep meaning the program cannot run. These two scenarios are handled by Java in this case as the synchronised methods handle blocking and waking of concurrent threads when one thread is executing code inside the method. To prevent the final concurrency issue - race conditions - we ensured that all resources shared by concurrent threads were only accessed and modified inside the synchronised methods. This means that only one thread can update the state of these shared resources at a time meaning race conditions cannot occur.

For parallel consumption, our API implements consumption from partitions behind a synchronised method for each consumer. This means that developers using our API, if they wish to, can design their program to call consumers concurrently on threads if they wish, or they can call a single consumer at a time (it is up to them). Our API does not force the developer to use one or another. Hence, to demonstrate that our solution can handle concurrent consumption, our CLI has the ability to create many threads of consumers and instruct them to all consume concurrently.

For parallel producing, our API also implements producing to partitions behind a synchronised method for each producer, as it ensures only one producer can modify the partition at a time. This means that developers using our API can concurrently call producers on threads in their own programs or they can call a single producer at a time if they wish. Our CLI showcases that our solution is able to handle concurrent threads producing to partitions by creating multiple threads which simultaneously make calls to the API to produce to partitions (some to the same partition, some different).

In general - our implementation does not have pre-implemented API methods for parallel producing and consuming. Our producers and consumers are thread-safe, meaning they do support multithreading, however multithreading is up to the individual developer to implement to their own specifications. Our CLI shows that multithreading works as per the engineering requirements.

**Generics:**

Our solution utilises generics to allow for events to use any datatype specified by the developer implementing a system with our API. This means that the system can be used by any developer for any datatype wanting to use a system which produces and consumes events (like a system scheduler for example). Our topics also contain an attribute which specifies which datatype of event a topic can take (which again is specified by the developer). As our API is design by contract, there is no error checking to ensure that the datatype used by each producer and topic is consistent, so it is up to developers to employ defensive programming in their implementations to ensure that the data types being used in topics are consistent.

## Your final UML diagram

See **design.pdf** in repo.

## A brief reflection on the assignment, including on the challenges you faced and whether you changed your development approach

Our solution successfully implemented all the requirements outlined in the engineering requirements.

In reflection of the assignment, we feel that our solution is a strong approach to meeting the engineering requirements outlined in the assignment. Our solution successfully utilises design patterns in situations where it makes logical sense given the requirements provided. The solution is loosely coupled and highly cohesive due to our use of patterns, and our classes remain single purpose and logically designed. The use of patterns such as strategy patterns has allowed us to adhere to the open close principle.

As a solution which can be implemented by developers as an API or library, our solution is strong as it utilises generics meaning developers can specify any datatype they wish to use for events and topics. Our API provides methods to developers so they can create their own multithreading implementations to their own specifications if they wish (as we did in our CLI to demonstrate that multithreading is supported in producers and consumers), as our producing and consuming methods are thread-safe.

The biggest challenges we faced for the assignment were surrounding interpreting the engineering requirements and specification, as it can be hard to determine what kind of solution to implement. Our final development approach ended up being mostly the same as our initial plans, however we realised we needed to add some functionality which we had not considered while we were coding (such as the observer pattern we implemented). This was easy to add to the system as our system is loosely coupled and hence it mostly required extension of existing classes through new methods, and creation of new classes.
