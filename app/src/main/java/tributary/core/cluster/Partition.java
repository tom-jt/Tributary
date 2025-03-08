package tributary.core.cluster;

import java.util.ArrayList;
import java.util.List;

import tributary.api.Message;
import tributary.core.consumer.MessageLog;

public class Partition {
    private String id;
    private List<Message<?>> msgs = new ArrayList<>();
    private int offset = 0;

    public Partition(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<Message<?>> getMessages() {
        return msgs;
    }

    public synchronized MessageLog consume() {
        if (msgs.size() != 0) {
            offset++;
            return new MessageLog(msgs.remove(0), offset, id);
        }
        return null;
    }

    public synchronized void produce(Message<?> msg) {
        msgs.add(msg);
    }
}
