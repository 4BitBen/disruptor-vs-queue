package com.github.fourbitben.disruptor;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.fourbitben.ConsumerFactory;
import com.github.fourbitben.EventTimerManager;
import com.lmax.disruptor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Runner that uses the disruptor event handler infrastructure. When using this, all handlers will receive all
 * events and must only process the ones they care about.
 *
 * @author Ben Nelson
 */
public class WorkHandlerRunner<T> {
    private static final Logger logger = LoggerFactory.getLogger(WorkHandlerRunner.class);

    private final EventFactory<T> disruptorEventFactory;
    private final ConsumerFactory<T> consumerFactory;
    private final com.github.fourbitben.EventFactory<T> eventFactory;

    public WorkHandlerRunner(EventFactory<T> disruptorEventFactory, ConsumerFactory<T> consumerFactory, com.github.fourbitben.EventFactory<T> eventFactory) {
        this.disruptorEventFactory = disruptorEventFactory;
        this.consumerFactory = consumerFactory;
        this.eventFactory = eventFactory;
    }

    /**
     * Create a pool of consumers, all reading from a ring buffer to process events.
     *
     * @param NUM_THREADS total number of consumers to create in the pool
     * @param totalEventsToPublish total events to publish to the queue
     * @throws InterruptedException if there is a problem talking to the blocking queue
     */
    public long run(int NUM_THREADS, long totalEventsToPublish) throws InterruptedException {
        // Initialize Metrics
        final MetricRegistry metrics = new MetricRegistry();
        Timer enqueueTimer = metrics.timer("ring.enqueue");
        Timer consumeTimer = metrics.timer("ring.consume");
        Timer eventTimer   = metrics.timer("event.consume");
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MICROSECONDS)
                .formattedFor(Locale.US)
                .build();
        consoleReporter.start(100, TimeUnit.HOURS); // Pick an arbitrarily long period of time. Will print out the report when I'm done

        EventTimerManager<T> eventTimerManager = new EventTimerManager<>();

        ExecutorService executor;
        if(NUM_THREADS == 1) {
            executor = Executors.newSingleThreadExecutor();
        } else {
            executor = Executors.newFixedThreadPool(NUM_THREADS);
        }

        RingWorkHandlerConsumer[] ringConsumers = new RingWorkHandlerConsumer[NUM_THREADS];
        for(int i = 0; i < ringConsumers.length; i++) {
            ringConsumers[i] = new RingWorkHandlerConsumer<>(consumerFactory.createConsumer(i, NUM_THREADS), consumeTimer, eventTimerManager);
        }

        WorkerPool<T> workerPool = new WorkerPool<>(disruptorEventFactory, new FatalExceptionHandler(), ringConsumers);
        RingBuffer<T> valueEventRingBuffer = workerPool.start(executor);

        final long startTime = System.currentTimeMillis();
        publishEvents(totalEventsToPublish, valueEventRingBuffer, enqueueTimer, eventTimer, eventTimerManager);

        workerPool.drainAndHalt();
        executor.shutdown();
        eventTimerManager.destroy();
        final long endTime = System.currentTimeMillis();
        logger.info("It took " + (endTime - startTime) + "ms to process " + totalEventsToPublish + " messages.");
        for(RingWorkHandlerConsumer consumer : ringConsumers) {
            logger.info("Processed " + consumer.getMessagesProcessed() + " messages.");
        }

        consoleReporter.report();
        consoleReporter.stop();

        return (endTime - startTime);
    }

    private void publishEvents(long size, RingBuffer<T> valueEventRingBuffer, Timer timer, Timer eventTimer, EventTimerManager<T> eventTimerManager) {
        final EventTranslatorOneArg<T, Long> eventTranslator = new EventTranslatorOneArg<T, Long>() {
            @Override
            public void translateTo(T event, long sequence, Long arg0) {
                eventFactory.setEventValue(event, arg0);
            }
        };
        for(long i = 0; i < size; i++) {
            // Start the global timer for this event
            T event = eventFactory.createEvent(size);
            eventFactory.setEventValue(event, i);
            eventTimerManager.startTimer(event, eventTimer.time());
            Timer.Context timerContext = timer.time();
            valueEventRingBuffer.publishEvent(eventTranslator, i);
            timerContext.stop();
        }
    }
}
