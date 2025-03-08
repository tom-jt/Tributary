package tributary.cli;

import java.util.Random;

import tributary.api.Message;
import tributary.api.ProducerController;
import tributary.api.TributaryController;
import tributary.api.res.TopicResponse;

public class ParallelProducer extends Thread {
    private TributaryController tributary;
    private ProducerController producer;
    private String topicId;
    private Message<?> msg;

    public ParallelProducer(TributaryController tributary, ProducerController producer, String topicId,
            Message<?> msg) {
        this.tributary = tributary;
        this.producer = producer;
        this.topicId = topicId;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            Random rand = new Random();
            Thread.sleep(0L, rand.nextInt(2));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        producer.produce(topicId, msg);

        TopicResponse res = new TopicResponse(tributary.showTopic(topicId));
        System.out.println(String.format("Parallel producing event with id [%s] in partition with id [%s]", msg.getId(),
                res.getPartitionWithMessage(msg)));
    }
}
