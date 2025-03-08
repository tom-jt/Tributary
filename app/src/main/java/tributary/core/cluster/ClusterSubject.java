package tributary.core.cluster;

public interface ClusterSubject {
    public void notifySubscribers(String topicId);

    public void subscribe(ClusterSubscriber sub);
}
