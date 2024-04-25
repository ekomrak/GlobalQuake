package globalquake.telegram.data;

import java.util.HashMap;
import java.util.Map;

public abstract class TelegramAbstractInfo<T> {
    private final Map<Long, Integer> messages = new HashMap<>();

    public TelegramAbstractInfo(T t) {
        updateWith(t);
    }

    public Map<Long, Integer> getMessages() {
        return messages;
    }

    public abstract void updateWith(T t);

    public abstract boolean equalsTo(T t);
}
