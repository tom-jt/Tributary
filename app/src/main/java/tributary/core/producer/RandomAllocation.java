package tributary.core.producer;

import java.util.List;
import java.util.Random;

import tributary.api.Message;

public class RandomAllocation implements AllocationStrategy {
    @Override
    public String allocatePartition(List<String> partitionIds, Message<?> msg) {
        Random rand = new Random();
        return partitionIds.get(rand.nextInt(partitionIds.size()));
    }
}
