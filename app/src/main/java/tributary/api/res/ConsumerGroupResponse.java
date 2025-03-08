package tributary.api.res;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tributary.api.enums.RebalancingType;

/**
 * @invariant all local variables != null and are read-only
 */
public class ConsumerGroupResponse {
    private final String consumerGroupId;
    private final String topicId;
    private final RebalancingType rebalancingType;
    private final Map<String, List<String>> consumers = new LinkedHashMap<>();

    public ConsumerGroupResponse(String consumerGroupId, String topicId, RebalancingType rebalancingType) {
        this.consumerGroupId = consumerGroupId;
        this.topicId = topicId;
        this.rebalancingType = rebalancingType;
    }

    public ConsumerGroupResponse(ConsumerGroupResponse res) {
        this(res.getConsumerGroupId(), res.getTopicId(), res.getRebalancingType());
        for (String consumerId : res.getConsumerIds()) {
            consumers.put(consumerId, res.getPartitionIds(consumerId));
        }
    }

    public String getConsumerGroupId() {
        return consumerGroupId;
    }

    public String getTopicId() {
        return topicId;
    }

    public List<String> getConsumerIds() {
        return new ArrayList<>(consumers.keySet());
    }

    public RebalancingType getRebalancingType() {
        return rebalancingType;
    }

    /**
    * Get all partitions assigned to a particular consumer
    * @param consumerId - ID of the consumer to check
    * @return List of partitions assigned to the consumer with ID consumerId
    * @precondition consumerId belongs to a consumer that exists in this consumer group
    * @postcondition returns value != null
    */
    public List<String> getPartitionIds(String consumerId) {
        return consumers.get(consumerId);
    }

    /**
    * Add append a partition to this ConsumerGroupResponse along with its assignmed list of partitionIDs
    * @param consumerId - ID of the partition we want to add
    * @param partitionIds - List of all partitionIDs that the consumer is assignmed to
    * @precondition consumerId refers to an existing consumer in this ConsumerGroup
    */
    public void addConsumer(String consumerId, List<String> partitionIds) {
        consumers.put(consumerId, partitionIds);
    }
}
