package tributary.core.producer;

import java.util.List;

import tributary.api.Message;

public class ManualAllocation implements AllocationStrategy {
    @Override
    public String allocatePartition(List<String> partitionIds, Message<?> msg) {
        return msg.getKey();
    }
}
