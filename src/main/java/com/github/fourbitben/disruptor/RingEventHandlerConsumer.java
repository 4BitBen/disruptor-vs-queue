package com.github.fourbitben.disruptor;

import com.github.fourbitben.Consumer;
import com.lmax.disruptor.EventHandler;

public class RingEventHandlerConsumer<T> implements EventHandler<T> {

    private long messagesProcessed;

    private final int ordinal;
    private final int numberOfConsumers;

    private Consumer<T> consumer;

    public RingEventHandlerConsumer(int ordinal, int numberOfConsumers, Consumer<T> consumer) {
        this.ordinal = ordinal;
        this.numberOfConsumers = numberOfConsumers;
        this.messagesProcessed = 0;
        this.consumer = consumer;
    }

    @Override
    public void onEvent(T event, long sequence, boolean endOfBatch) throws Exception {
        if(sequence % numberOfConsumers == ordinal) {
            messagesProcessed++;
            consumer.onEvent(event);
        }
    }

    public long getMessagesProcessed() {
        return messagesProcessed;
    }
}
