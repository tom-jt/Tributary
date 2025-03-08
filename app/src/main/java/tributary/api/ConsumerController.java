package tributary.api;

import java.util.List;

import tributary.api.enums.RebalancingType;
import tributary.api.res.ConsumerGroupResponse;
import tributary.core.consumer.ConsumerGroup;

/**
 * @invariant all local variables != null and are read-only, each ConsumerController
 * always has a 1-1 relationship with a ConsumerGroup
 */
public class ConsumerController {
    private final ConsumerGroup group;

    public ConsumerController(String groupId, String topicId, RebalancingType rebalancing) {
        this.group = new ConsumerGroup(groupId, topicId, rebalancing);
    }

    /**
     * Create a consumer inside of the consumer group stored in this controller
     * @param consumerId - the ID of the consumer to be created
     * @precondition consumerId must be unique amongst other consumers
     * @postcondition a Consumer is now created in this consumer group with given consumerId
     */
    public void createConsumer(String consumerId) {
        group.createConsumer(consumerId);
    }

    /**
     * Remove a consumer with a given ID
     * @param consumerId - the ID of the consumer to be removed
     * @return boolean indicating if the consumer was removed successfully
     * @precondition consumerId must refer to an existing consumer in this consumer group
     * @postcondition the Consumer associated with the given consumerId (if is exists)
     * is now removed from the consumer group stored in this controller
     */
    public boolean removeConsumer(String consumerId) {
        return group.removeConsumer(consumerId);
    }

    /**
     * Consume a message from a partition in the TributaryCluster
     * @param consumerId - the ID of the consumer used to consume
     * @param partitionId - the ID of the partition to consume from
     * @return the consumed Message from the given partition, or null if the
     * given partition is empty
     * @precondition consumerId must refer to an existing consumer in this
     * consumer group, and the partitionId must refer to an existing partition
     * in the cluster
     * @postcondition the consumed Message will be removed from the given Partition
     */
    public Message<?> consume(String consumerId, String partitionId) {
        return group.consume(consumerId, partitionId);
    }

    /**
    * Show properties of this consumer group, including its consumers and the
    * partitions each consumer is assigned to
    * @return ConsumerGroupResponse that contains all of the information to be shown
    * @postcondition returns ConsumerGroupResponse != null
    */
    public ConsumerGroupResponse showConsumerGroup() {
        return group.show();
    }

    /**
     * Playback the messages from a given partition consumed by a given consumer,
     * starting at some offset
     * @param consumerId
     * @param partitionId
     * @param offset - index indicating where to begin playback, offset = the
     * index of which messages were added to the given partition (e.g. if 5
     * messages added to a partition, to start playback from the 2nd earliest
     * in that partition, offset = 2)
     * @return List of Messages consumed by the given consumer from the given
     * Parittion, on or after the offset
     * @precondition consumerId must refer to an existing consumer in this
     * consumer group, the partitionId must refer to an existing partition
     * in the cluster, and offset >= 1
     * @postcondition returned list of messages only includes messages from the
     * given partition and that are consumed by the given consumer
     */
    public List<Message<?>> playback(String consumerId, String partitionId, int offset) {
        return group.playback(consumerId, partitionId, offset);
    }

    /**
     * Change the rebalancing strategy used by this consumer group
     * @param rebalancing - enum indicating the new rebalancing method
     * @precondition rebalancing != null
     */
    public void setRebalancingStrategy(RebalancingType rebalancing) {
        group.setRebalancingStrategy(rebalancing);
    }
}
