package tributary.core.producer;

import java.util.HashMap;
import java.util.Map;

import tributary.api.Message;
import tributary.api.enums.AllocationType;
import tributary.core.cluster.TributaryCluster;

public class Producer {
    private final Map<AllocationType, AllocationStrategy> allocationMap = new HashMap<>() {
        {
            put(AllocationType.Manual, new ManualAllocation());
            put(AllocationType.Random, new RandomAllocation());
        }
    };

    private String id;
    private AllocationType allocationType;

    public Producer(String id, AllocationType allocationType) {
        this.id = id;
        this.allocationType = allocationType;
    }

    public void produce(String topicId, Message<?> msg) {
        TributaryCluster.getInstance().produce(topicId, msg, allocationMap.get(allocationType));
    }

    public String getId() {
        return id;
    }
}
