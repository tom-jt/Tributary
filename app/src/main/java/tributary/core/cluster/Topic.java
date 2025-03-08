package tributary.core.cluster;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tributary.api.Message;
import tributary.api.res.TopicResponse;
import tributary.core.consumer.MessageLog;

public class Topic {
    private String id;
    private Class<?> type;
    private Map<String, Partition> partitions = new LinkedHashMap<>();

    public Topic(String id, Class<?> type) {
        this.id = id;
        this.type = type;
    }

    public List<String> getPartitionIds() {
        return new ArrayList<>(partitions.keySet());
    }

    public String getId() {
        return id;
    }

    public void createParition(String partitionId) {
        partitions.put(partitionId, new Partition(partitionId));
    }

    public boolean removePartition(String partitionId) {
        return partitions.remove(partitionId) != null;
    }

    public boolean hasPartition(String partitionId) {
        return partitions.get(partitionId) != null;
    }

    public MessageLog consume(String partitionId) {
        return partitions.get(partitionId).consume();
    }

    public void produce(String partitionId, Message<?> msg) {
        partitions.get(partitionId).produce(msg);
    }

    public TopicResponse show() {
        TopicResponse res = new TopicResponse(id, type);
        for (String partitionId : partitions.keySet()) {
            res.addPartition(partitionId, partitions.get(partitionId).getMessages());
        }
        return res;
    }
}
