package tributary.core.consumer;

import java.util.List;

public class RangeRebalancing implements RebalancingStrategy {
    public void rebalance(List<Consumer> consumers, List<String> partitionIds) {
        int consumerSize = consumers.size();
        int partitionSize = partitionIds.size();
        int numPerConsumer = partitionSize / consumerSize;
        int numLeftOver = partitionSize % consumerSize;

        int partitionCounter = 0;
        for (int i = 0; i < consumerSize; i++) {
            Consumer consumer = consumers.get(i);
            consumer.clearPartitionIds();

            for (int j = 0; j < numPerConsumer + (i < numLeftOver ? 1 : 0); j++) {
                consumer.appendPartitionId(partitionIds.get(partitionCounter));
                partitionCounter++;
            }
        }
    }
}
