package tributary.cli;

import java.util.Random;

import tributary.api.ConsumerController;
import tributary.api.Message;

public class ParallelConsumer extends Thread {
    private String consumerId;
    private String partitionId;
    private ConsumerController group;

    public ParallelConsumer(String consumerId, String partitionId, ConsumerController group) {
        this.consumerId = consumerId;
        this.partitionId = partitionId;
        this.group = group;
    }

    @Override
    public void run() {
        try {
            Random rand = new Random();
            Thread.sleep(0L, rand.nextInt(2));
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        Message<?> msg = group.consume(consumerId, partitionId);
        if (msg == null) {
            System.out.println(
                    String.format("Parallel consumer [%s] cannot consume from partition [%s] because it is empty",
                            consumerId, partitionId));
        } else {
            System.out.println(String.format("Parallel consumer [%s] consumed event [%s] with data [%s]", consumerId,
                    msg.getId(), msg.getValue()));
        }
    }
}
