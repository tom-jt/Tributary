package tributary.core.consumer;

import java.util.List;

public interface RebalancingStrategy {
    public void rebalance(List<Consumer> consumers, List<String> partitionIds);
}
