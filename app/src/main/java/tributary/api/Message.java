package tributary.api;

import java.time.LocalDateTime;

/**
 * @invariant all local variables != null and are read-only
 */
public class Message<T> {
    // Header
    private final LocalDateTime dateTimeCreated;
    private final String id;

    // Key
    private final String key;

    // Value
    private final T value;

    public Message(LocalDateTime dateTimeCreated, String id, String key, T value) {
        this.dateTimeCreated = dateTimeCreated;
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public Message(Message<T> msg) {
        this(msg.getDateTimeCreated(), msg.getId(), msg.getKey(), msg.getValue());
    }

    public String getId() {
        return id;
    }

    public Class<?> getType() {
        return value.getClass();
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public LocalDateTime getDateTimeCreated() {
        return dateTimeCreated;
    }
}
