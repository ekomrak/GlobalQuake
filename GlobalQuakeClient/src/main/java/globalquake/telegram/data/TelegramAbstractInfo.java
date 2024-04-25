package globalquake.telegram.data;

public abstract class TelegramAbstractInfo<T> {
    private Integer messageId;

    public TelegramAbstractInfo(T t) {
        updateWith(t);
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public abstract void updateWith(T t);

    public abstract boolean equalsTo(T t);
}
