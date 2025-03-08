package tributary.core.consumer;

import tributary.api.Message;

public class MessageLog {
    private Message<?> msg;
    private int offset;
    private String partitionId;

    public MessageLog(Message<?> msg, int offset, String partitionId) {
        this.msg = msg;
        this.offset = offset;
        this.partitionId = partitionId;
    }

    public Message<?> getMsg() {
        return msg;
    }

    public int getOffset() {
        return offset;
    }

    public String getPartitionId() {
        return partitionId;
    }
}
