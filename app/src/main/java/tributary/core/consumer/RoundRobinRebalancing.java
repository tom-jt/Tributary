package tributary.core.consumer;

import java.util.List;

public class RoundRobinRebalancing implements RebalancingStrategy {
    public void rebalance(List<Consumer> consumers, List<String> partitionIds) {
        if (consumers.size() == 0) {
            return;
        }

        for (Consumer consumer : consumers) {
            consumer.clearPartitionIds();
        }

        int consumerCounter = 0;
        for (String partitionId : partitionIds) {
            consumers.get(consumerCounter).appendPartitionId(partitionId);
            consumerCounter++;
            consumerCounter %= consumers.size();
        }
    }
}
