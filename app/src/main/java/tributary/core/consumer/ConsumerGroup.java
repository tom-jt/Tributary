package tributary.core.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tributary.api.Message;
import tributary.api.enums.RebalancingType;
import tributary.api.res.ConsumerGroupResponse;
import tributary.core.cluster.ClusterSubscriber;
import tributary.core.cluster.TributaryCluster;

public class ConsumerGroup implements ClusterSubscriber {
    private final Map<RebalancingType, RebalancingStrategy> rebalancingMap = new HashMap<>() {
        {
            put(RebalancingType.Range, new RangeRebalancing());
            put(RebalancingType.RoundRobin, new RoundRobinRebalancing());
        }
    };

    private String id;
    private String topicId;
    private RebalancingType rebalancingType;
    private Map<String, Consumer> consumers = new LinkedHashMap<>();

    public ConsumerGroup(String id, String topicId, RebalancingType rebalancingType) {
        this.id = id;
        this.topicId = topicId;
        this.rebalancingType = rebalancingType;
        TributaryCluster.getInstance().subscribe(this);
    }

    public void createConsumer(String id) {
        Consumer consumer = new Consumer(id);
        consumers.put(id, consumer);
        rebalance();
    }

    public boolean removeConsumer(String id) {
        if (consumers.remove(id) != null) {
            rebalance();
            return true;
        }

        return false;
    }

    public void setRebalancingStrategy(RebalancingType rebalancingType) {
        this.rebalancingType = rebalancingType;
        rebalance();
    }

    public void rebalance() {
        List<Consumer> consumerIds = new ArrayList<>(consumers.values());
        List<String> partitionIds = TributaryCluster.getInstance().getPartitionIds(topicId);
        rebalancingMap.get(rebalancingType).rebalance(consumerIds, partitionIds);
    }

    public void update(String givenTopicId) {
        if (givenTopicId.equals(topicId)) {
            rebalance();
        }
    }

    public Message<?> consume(String consumerId, String partitionId) {
        return consumers.get(consumerId).consume(partitionId);
    }

    public List<Message<?>> playback(String consumerId, String partitionId, int offset) {
        return consumers.get(consumerId).playback(partitionId, offset);
    }

    public ConsumerGroupResponse show() {
        ConsumerGroupResponse res = new ConsumerGroupResponse(id, topicId, rebalancingType);

        for (Consumer consumer : consumers.values()) {
            res.addConsumer(consumer.getId(), consumer.getPartitionIds());
        }

        return res;
    }
}
