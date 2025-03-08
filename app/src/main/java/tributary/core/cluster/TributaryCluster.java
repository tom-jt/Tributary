package tributary.core.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tributary.api.Message;
import tributary.api.res.TopicResponse;
import tributary.core.consumer.MessageLog;
import tributary.core.producer.AllocationStrategy;

public class TributaryCluster implements ClusterSubject {
    private static TributaryCluster instance;

    private Map<String, Topic> topics = new HashMap<>();
    private List<ClusterSubscriber> subs = new ArrayList<>();

    public static TributaryCluster getInstance() {
        if (instance == null) {
            instance = new TributaryCluster();
        }

        return instance;
    }

    public MessageLog consume(String consumerId, String partitionId) {
        for (Topic topic : topics.values()) {
            if (topic.hasPartition(partitionId)) {
                return topic.consume(partitionId);
            }
        }

        return null;
    }

    public void produce(String topicId, Message<?> msg, AllocationStrategy strategy) {
        Topic topic = topics.get(topicId);
        String partitionId = strategy.allocatePartition(topic.getPartitionIds(), msg);
        topics.get(topicId).produce(partitionId, msg);
    }

    public void createTopic(String topicId, Class<?> type) {
        topics.put(topicId, new Topic(topicId, type));
    }

    public boolean removeTopic(String topicId) {
        return topics.remove(topicId) != null;
    }

    public void createPartition(String partitionId, String topicId) {
        topics.get(topicId).createParition(partitionId);
        notifySubscribers(topicId);
    }

    public boolean removePartition(String partitionId) {
        for (Topic topic : topics.values()) {
            if (topic.removePartition(partitionId)) {
                notifySubscribers(topic.getId());
                return true;
            }
        }

        return false;
    }

    public List<String> getPartitionIds(String topicId) {
        return topics.get(topicId).getPartitionIds();
    }

    public TopicResponse showTopic(String topicId) {
        return topics.get(topicId).show();
    }

    public void notifySubscribers(String topicId) {
        for (ClusterSubscriber sub : subs) {
            sub.update(topicId);
        }
    }

    public void subscribe(ClusterSubscriber sub) {
        subs.add(sub);
    }
}
