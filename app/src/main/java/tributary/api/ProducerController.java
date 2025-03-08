package tributary.api;

import tributary.api.enums.AllocationType;
import tributary.core.producer.Producer;

/**
 * @invariant all local variables != null and are read-only, each ProducerController
 * always has a 1-1 relationship with a Producer
 */
public class ProducerController {
    private final Producer producer;
    private final Class<?> type;

    public ProducerController(String producerId, AllocationType allocation, Class<?> type) {
        this.producer = new Producer(producerId, allocation);
        this.type = type;
    }

    /**
     * Get the type of event this producer produces
     * @return Class representing the type of event
     * @postcondition returned type != null
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Produce a message into a particular topic
     * @param topicId - ID of the topic to produce a message to
     * @param msg - The message itself, stored in a Message instance
     * @precondition msg != null, topicId refers to an existing topic, and
     * msg's generic type matches the type of both this producer and the topic
     * we are producer to
     * (e.g. if this producer and topic have type Integer, then this method only
     * accepts Message<Integer>)
     * @postcondition given msg is now stored in a partition in the given topic,
     * which partition it is stored in complies with the AllocationType
     * provided in this class' constructor
     */
    public void produce(String topicId, Message<?> msg) {
        producer.produce(topicId, msg);
    }
}
