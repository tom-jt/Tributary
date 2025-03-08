package tributary.api.res;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tributary.api.Message;

/**
 * @invariant all local variables != null and are read-only
 */
public class TopicResponse {
    private String topicId;
    private Class<?> topicType;
    private Map<String, List<Message<?>>> partitions = new LinkedHashMap<>();

    public TopicResponse(String topicId, Class<?> topicType) {
        this.topicId = topicId;
        this.topicType = topicType;
    }

    public TopicResponse(TopicResponse res) {
        this(res.getTopicId(), res.getTopicType());
        for (String partitionId : res.getPartitionIds()) {
            partitions.put(partitionId, res.getMessages(partitionId));
        }
    }

    public String getTopicId() {
        return topicId;
    }

    public Class<?> getTopicType() {
        return topicType;
    }

    public List<String> getPartitionIds() {
        return new ArrayList<>(partitions.keySet());
    }

    /**
     * Get the ID of the partition that contains a particular message
     * @param msg - message
     * @return ID of the partition that contains msg
     * @precondition msg is stored in one of the partitions
     * @postcondition returns value != null
     */
    public String getPartitionWithMessage(Message<?> msg) {
        return partitions.keySet().stream().filter(s -> partitions.get(s).contains(msg)).findAny().get();
    }

    /**
     * Get all messages stored in a particular partition
     * @param partitionId - ID of the partition to check
     * @return List of messages from the partition with ID partitionId
     * @precondition partitionId belongs to a partition that exists in this topic
     * @postcondition returns value != null
     */
    public List<Message<?>> getMessages(String partitionId) {
        return partitions.get(partitionId);
    }

    /**
     * Add a partition to this TopicResponse along with its stored list of messages
     * @param partitionId - ID of the partition we want to add
     * @param msgs - List of all messages that partition is storing
     * @precondition partitionId refers to a existing pastition in this Topic,
     */
    public void addPartition(String partitionId, List<Message<?>> msgs) {
        partitions.put(partitionId, msgs);
    }
}
