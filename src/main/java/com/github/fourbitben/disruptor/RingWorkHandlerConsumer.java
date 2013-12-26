package com.github.fourbitben.disruptor;

import com.codahale.metrics.Timer;
import com.github.fourbitben.Consumer;
import com.lmax.disruptor.WorkHandler;
import com.github.fourbitben.EventTimerManager;

public class RingWorkHandlerConsumer<T> implements WorkHandler<T> {

    private long messagesProcessed = 0;

    private Consumer<T> consumer;

    private Timer consumeTimer;

    private EventTimerManager<T> eventTimerManager;

    public RingWorkHandlerConsumer(Consumer<T> consumer, Timer consumeTimer, EventTimerManager<T> eventTimerManager) {
        this.consumer = consumer;
        this.consumeTimer = consumeTimer;
        this.eventTimerManager = eventTimerManager;
    }

    @Override
    public void onEvent(T event) throws Exception {
        messagesProcessed++;
        Timer.Context consumeTimerContext = consumeTimer.time();
        consumer.onEvent(event);
        consumeTimerContext.stop();

        eventTimerManager.stopTimer(event);
    }

    public long getMessagesProcessed() {
        return messagesProcessed;
    }
}
