package tributary.core.producer;

import java.util.List;

import tributary.api.Message;

public interface AllocationStrategy {
    public String allocatePartition(List<String> partitionIds, Message<?> msg);
}
