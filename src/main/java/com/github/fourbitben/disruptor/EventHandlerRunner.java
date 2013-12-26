package com.github.fourbitben.disruptor;

import com.github.fourbitben.ConsumerFactory;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runner that uses the disruptor event handler infrastructure. When using this, all handlers will receive all
 * events and must only process the ones they care about.
 *
 * @author Ben Nelson
 */
public class EventHandlerRunner<T> {
    private static final Logger logger = LoggerFactory.getLogger(EventHandlerRunner.class);
    private final EventFactory<T> disruptorEventFactory;
    private final ConsumerFactory<T> consumerFactory;
    private final com.github.fourbitben.EventFactory<T> eventFactory;

    public EventHandlerRunner(EventFactory<T> disruptorEventFactory, ConsumerFactory<T> consumerFactory, com.github.fourbitben.EventFactory<T> eventFactory) {
        this.disruptorEventFactory = disruptorEventFactory;
        this.consumerFactory = consumerFactory;
        this.eventFactory = eventFactory;
    }

    /**
     * Create a pool of consumers, all reading from a ring buffer to process events.
     *
     * @param NUM_THREADS total number of consumers to create in the pool
     * @param totalEventsToPublish total events to publish to the queue
     * @param ringConsumers the placeholder for creating consumers
     * @throws InterruptedException if there is a problem talking to the blocking queue
     */
    public void run(int NUM_THREADS, int totalEventsToPublish, RingEventHandlerConsumer<T>[] ringConsumers) throws InterruptedException {
        final int RING_BUFFER_SIZE = 1024;

        ExecutorService executor;
        if(NUM_THREADS == 1) {
            executor = Executors.newSingleThreadExecutor();
        } else {
            executor = Executors.newFixedThreadPool(NUM_THREADS);
        }

        Disruptor<T> disruptor =
                new Disruptor<>(disruptorEventFactory, RING_BUFFER_SIZE, executor,
                        ProducerType.SINGLE,
                        new YieldingWaitStrategy());

        for(int i = 0; i < NUM_THREADS; i++) {
            ringConsumers[i] = new RingEventHandlerConsumer<T>(i, NUM_THREADS, consumerFactory.createConsumer(i, NUM_THREADS));
        }

        disruptor.handleEventsWith(ringConsumers);

        RingBuffer<T> eventRingBuffer = disruptor.start();

        final long startTime = System.currentTimeMillis();
        publishEvents(totalEventsToPublish, eventRingBuffer);

        disruptor.shutdown();
        executor.shutdown();
        final long endTime = System.currentTimeMillis();
        logger.info("It took " + (endTime - startTime) + "ms to process " + totalEventsToPublish + " messages.");
        for(RingEventHandlerConsumer consumer : ringConsumers) {
            logger.info("Processed " + consumer.getMessagesProcessed() + " messages.");
        }
    }

    private void publishEvents(int size, RingBuffer<T> valueEventRingBuffer) {
        final EventTranslatorOneArg<T, Long> eventTranslator = new EventTranslatorOneArg<T, Long>() {
            @Override
            public void translateTo(T event, long sequence, Long arg0) {
                eventFactory.setEventValue(event, arg0);
            }
        };
        for(long i = 0; i < size; i++) {
            valueEventRingBuffer.publishEvent(eventTranslator, i);
        }
    }
}
