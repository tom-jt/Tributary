package tributary.core.consumer;

import java.util.ArrayList;
import java.util.List;

import tributary.api.Message;
import tributary.core.cluster.TributaryCluster;

public class Consumer {
    private String id;
    private List<String> partitionIds = new ArrayList<>();
    private List<MessageLog> msgLogs = new ArrayList<>();

    public Consumer(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<String> getPartitionIds() {
        return partitionIds;
    }

    public void appendPartitionId(String id) {
        partitionIds.add(id);
    }

    public void clearPartitionIds() {
        partitionIds.clear();
    }

    public Message<?> consume(String partitionId) {
        MessageLog msgLog = TributaryCluster.getInstance().consume(id, partitionId);
        if (msgLog == null) {
            return null;
        }

        msgLogs.add(msgLog);
        return msgLog.getMsg();
    }

    public List<Message<?>> playback(String id, int offset) {
        List<Message<?>> msgList = new ArrayList<>();

        for (MessageLog msg : msgLogs) {
            if (msg.getOffset() >= offset && msg.getPartitionId().equals(id)) {
                msgList.add(msg.getMsg());
            }
        }
        return msgList;
    }
}
