package com.github.fourbitben.queue;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.fourbitben.ConsumerFactory;
import com.github.fourbitben.EventFactory;
import com.github.fourbitben.EventTimerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.*;

/**
 * Runner to test out a blocking queue.
 */
public class BlockingQueueRunner<T> {
    private static final Logger logger = LoggerFactory.getLogger(BlockingQueueRunner.class);

    private final BlockingQueue<T> queue;
    private final ConsumerFactory<T> consumerFactory;
    private final EventFactory<T> eventFactory;

    /**
     * Create a new runner.
     *
     * @param queue the queue to send events through
     * @param consumerFactory factory to creating the consumer
     * @param eventFactory factory for creating the event
     */
    public BlockingQueueRunner(BlockingQueue<T> queue, ConsumerFactory<T> consumerFactory, EventFactory<T> eventFactory) {
        this.queue = queue;
        this.consumerFactory = consumerFactory;
        this.eventFactory = eventFactory;
    }

    /**
     * Create a pool of consumers, all reading from a queue to process events.
     *
     * @param NUM_THREADS total number of consumers to create in the pool
     * @param totalEventsToPublish total events to publish to the queue
     *
     * @return how much time (in ms) it took to process the events
     *
     * @throws InterruptedException if there is a problem talking to the blocking queue
     */
    public long run(int NUM_THREADS, long totalEventsToPublish) throws InterruptedException {
        // Initialize Metrics
        final MetricRegistry metrics = new MetricRegistry();
        Timer enqueueTimer = metrics.timer("queue.enqueue");
        Timer dequeueTimer = metrics.timer("queue.dequeue");
        Timer consumeTimer = metrics.timer("queue.consume");
        Timer eventTimer   = metrics.timer("event.consume");
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MICROSECONDS)
                .formattedFor(Locale.US)
                .build();
        consoleReporter.start(100, TimeUnit.HOURS);

        EventTimerManager<T> eventTimerManager = new EventTimerManager<>();

        // Create the executor to wrap the consumers
        ExecutorService executor;
        if(NUM_THREADS == 1) {
            executor = Executors.newSingleThreadExecutor();
        } else {
            executor = Executors.newFixedThreadPool(NUM_THREADS);
        }

        // Create the consumers and begin to listen to the blocking queue
        QueueConsumer[] queueConsumers = new QueueConsumer[NUM_THREADS];
        for(int i = 0; i < NUM_THREADS; i++) {
            queueConsumers[i] = new QueueConsumer<T>(queue, consumerFactory.createConsumer(0, NUM_THREADS), dequeueTimer, consumeTimer, eventTimerManager);
            executor.execute(queueConsumers[i]);
        }

        // Produce the events
        final long startTime = System.currentTimeMillis();
        for(long i = 0; i < totalEventsToPublish; i++) {
            // Create the event
            T event = eventFactory.createEvent(totalEventsToPublish);
            eventFactory.setEventValue(event, i);

            // Start the global timer for this event
            eventTimerManager.startTimer(event, eventTimer.time());

            // Enqueue
            Timer.Context enqueueTimerContext = enqueueTimer.time();
            queue.put(event);
            enqueueTimerContext.stop();
        }

        // All done, tell the consumers
        for(QueueConsumer consumer : queueConsumers) {
            consumer.shutdown();
        }

        // Wait for them to complete
        while(queue.peek() != null) { Thread.sleep(10);}

        executor.shutdown();
        eventTimerManager.destroy();

        final long endTime = System.currentTimeMillis();

        // Print out stats
        logger.info("It took " + (endTime - startTime) + "ms to process " + totalEventsToPublish + " messages.");
        for(QueueConsumer consumer : queueConsumers) {
            logger.info("Processed " + consumer.getMessagesProcessed() + " messages.");
        }


        consoleReporter.report();
        consoleReporter.stop();

        return (endTime - startTime);
    }
}
