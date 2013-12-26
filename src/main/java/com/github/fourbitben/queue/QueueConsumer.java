package com.github.fourbitben.queue;

import com.codahale.metrics.Timer;
import com.github.fourbitben.Consumer;
import com.github.fourbitben.EventTimerManager;

import java.util.concurrent.BlockingQueue;

/**
 * Queue based implementation
 *
 * @author Ben Nelson
 */
public class QueueConsumer<T> implements Runnable {
    private final BlockingQueue<T> queue;

    private boolean shutdown;

    private long messagesProcessed;

    private Consumer<T> consumer;

    private Timer dequeueTimer;

    private Timer consumeTimer;

    private EventTimerManager<T> eventTimerManager;

    public QueueConsumer(BlockingQueue<T> queue, Consumer<T> consumer, Timer dequeueTimer, Timer consumeTimer, EventTimerManager<T> eventTimerManager) {
        this.queue = queue;
        this.shutdown = false;
        this.messagesProcessed = 0;
        this.consumer = consumer;
        this.dequeueTimer = dequeueTimer;
        this.consumeTimer = consumeTimer;
        this.eventTimerManager = eventTimerManager;
    }

    @Override
    public void run() {
        while(true) {
            Timer.Context dequeueTimerContext = dequeueTimer.time();
            T event = getNextEvent();
            dequeueTimerContext.stop();
            if(event != null) {
                messagesProcessed++;
                Timer.Context consumeTimerContext = consumeTimer.time();
                consumer.onEvent(event);
                consumeTimerContext.stop();

                eventTimerManager.stopTimer(event);
            } else if(shutdown) {
                break;
            }
        }
    }

    private T getNextEvent() {
        while(true) {
            T event = queue.poll();
            if(event != null) {
                return event;
            } else if(shutdown) {
                return null;
            }
        }
    }

    public long getMessagesProcessed() {
        return messagesProcessed;
    }

    public void shutdown() {
        shutdown = true;
    }
}
