package tributary.api;

import tributary.api.res.TopicResponse;
import tributary.core.cluster.TributaryCluster;

public class TributaryController {
    /**
     * Create a topic that stores a specific type of events
     * @param topicId - the ID of the topic to be created
     * @param type - the type of events the topic accepts (e.g. String, Integer,
     * or any user-defined Class)
     * @precondition topicId must be unique amongst other topics
     * @postcondition a Topic is now created in the cluster with the given topicId
     */
    public void createTopic(String topicId, Class<?> type) {
        TributaryCluster.getInstance().createTopic(topicId, type);
    }

    /**
     * Remove a topic with a given ID
     * @param topicId - the ID of the topic to be removed
     * @return boolean indicating if the topic was removed successfully
     * @precondition topicId must refer to an existing topic in the cluster
     * @postcondition the Topic associated with the given topicId (if is exists)
     * is now removed from the cluster
     */
    public boolean removeTopic(String topicId) {
        return TributaryCluster.getInstance().removeTopic(topicId);
    }

    /**
     * Show properties of a topic, including its partitions and the messages
     * stored in each partition
     * @param topicId - the ID of the topic to be shown
     * @return TopicResponse that contains all of the information to be shown
     * @precondition topicId must refer to an existing topic in the cluster
     * @postcondition returns TopicResponse != null
     */
    public TopicResponse showTopic(String topicId) {
        return TributaryCluster.getInstance().showTopic(topicId);
    }

    /**
     * Create a partition within a given topic
     * @param partitionId - the ID of the partition to be created
     * @param topicId - the ID of the topic the partition belongs to
     * @precondition topicId must refer to an existing topic in the cluster, and
     * partitionId must be unique amongst all partitions everywhere
     * @postcondition a Partition is now created in the Topic with the given topicId
     */
    public void createPartition(String partitionId, String topicId) {
        TributaryCluster.getInstance().createPartition(partitionId, topicId);
    }

    /**
     * Remove a partition with a specified ID from the cluster
     * @param partitionId
     * @param topicId
     * @return boolean indicating if the partition was removed successfully
     * @precondition partitionId must refer to an existing partition in the cluster
     * @postcondition the Partition associated with the given partitionId (if is exists)
     * is now removed from its associated Topic
     */
    public boolean removePartition(String partitionId) {
        return TributaryCluster.getInstance().removePartition(partitionId);
    }
}
