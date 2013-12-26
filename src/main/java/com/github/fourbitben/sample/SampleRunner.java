package com.github.fourbitben.sample;

import com.github.fourbitben.Consumer;
import com.github.fourbitben.ConsumerFactory;
import com.github.fourbitben.EventFactory;
import com.github.fourbitben.disruptor.WorkHandlerRunner;
import com.github.fourbitben.queue.BlockingQueueRunner;
import org.apache.log4j.*;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class SampleRunner {
    public static void main(String[] args) throws Exception {
        // Initialize logger
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getRootLogger().removeAllAppenders();
        // Don't append, buffer I/O writes
        FileAppender fileAppender = new FileAppender(new PatternLayout("%d [%t] %-5p %c - %m%n"), "SampleRunner.log", false, true, 8 * 1024);
        Logger.getRootLogger().addAppender(fileAppender);

        // Run No-Op Tests
        System.out.println("## No Op Producer");
        System.out.println();
        System.out.println("### Single Thread");
        System.out.println();
        System.out.println("50M messages");
        System.out.println("Queue: " + NumberFormat.getNumberInstance(Locale.US).format(runNoOpBlockingQueue(1,   50000000)) + "ms");
        System.out.println("Ring:  " + NumberFormat.getNumberInstance(Locale.US).format(runNoOpRingWorkHandler(1, 50000000)) + "ms");
        System.out.println();
        System.out.println("### Multi Thread (8 Threads, 1 per Core)");
        System.out.println("50M messages");
        System.out.println("Queue: " + NumberFormat.getNumberInstance(Locale.US).format(runNoOpBlockingQueue(8,   50000000)) + "ms");
        System.out.println("Ring:  " + NumberFormat.getNumberInstance(Locale.US).format(runNoOpRingWorkHandler(8, 50000000)) + "ms");
        System.out.println();

        // Run Print Tests
        System.out.println("## Print Producer");
        System.out.println();
        System.out.println("### Single Thread");
        System.out.println();
        System.out.println("10M messages");
        System.out.println("Queue: " + NumberFormat.getNumberInstance(Locale.US).format(runPrintBlockingQueue(1,   10000000)) + "ms");
        System.out.println("Ring:  " + NumberFormat.getNumberInstance(Locale.US).format(runPrintRingWorkHandler(1, 10000000)) + "ms");
        System.out.println();
        System.out.println("### Multi Thread (8 Threads, 1 per Core)");
        System.out.println("10M messages");
        System.out.println("Queue: " + NumberFormat.getNumberInstance(Locale.US).format(runPrintBlockingQueue(8,   10000000)) + "ms");
        System.out.println("Ring:  " + NumberFormat.getNumberInstance(Locale.US).format(runPrintRingWorkHandler(8, 10000000)) + "ms");

        // Destroy Logger
        fileAppender.setImmediateFlush(true);
        Logger.getRootLogger().info("The end.");
    }

    private static long runNoOpBlockingQueue(int numThreads, long size) throws Exception {
        // Create the blocking (aka bounded) queue
        final int BUFFER_SIZE = 1024;
        BlockingQueue<ValueEvent> queue = new LinkedBlockingDeque<>(BUFFER_SIZE);

        ConsumerFactory<ValueEvent> consumerFactory = new ConsumerFactory<com.github.fourbitben.sample.ValueEvent>() {
            @Override
            public Consumer<ValueEvent> createConsumer(int ordinal, int totalNumberOfConsumers) {
                return new NoOpConsumer();
            }
        };

        EventFactory<ValueEvent> eventFactory = new EventFactory<com.github.fourbitben.sample.ValueEvent>() {
            @Override
            public ValueEvent createEvent(long totalPublish) {
                return new ValueEvent();
            }

            @Override
            public void setEventValue(ValueEvent event, long currentPublishNumber) {
                event.setValue(currentPublishNumber);
            }
        };

        BlockingQueueRunner<ValueEvent> queueRunner = new BlockingQueueRunner<>(queue, consumerFactory, eventFactory);
        return queueRunner.run(numThreads, size);
    }

    private static long runPrintBlockingQueue(int numThreads, long size) throws Exception {
        // Create the blocking (aka bounded) queue
        final int BUFFER_SIZE = 1024;
        BlockingQueue<ValueEvent> queue = new LinkedBlockingDeque<>(BUFFER_SIZE);

        ConsumerFactory<ValueEvent> consumerFactory = new ConsumerFactory<com.github.fourbitben.sample.ValueEvent>() {
            @Override
            public Consumer<ValueEvent> createConsumer(int ordinal, int totalNumberOfConsumers) {
                return new PrintConsumer();
            }
        };

        EventFactory<ValueEvent> eventFactory = new EventFactory<com.github.fourbitben.sample.ValueEvent>() {
            @Override
            public ValueEvent createEvent(long totalPublish) {
                return new ValueEvent();
            }

            @Override
            public void setEventValue(ValueEvent event, long currentPublishNumber) {
                event.setValue(currentPublishNumber);
            }
        };

        BlockingQueueRunner<ValueEvent> queueRunner = new BlockingQueueRunner<>(queue, consumerFactory, eventFactory);
        return queueRunner.run(numThreads, size);
    }

    private static long runNoOpRingWorkHandler(int numThreads, long size) throws Exception {
        ConsumerFactory<ValueEvent> consumerFactory = new ConsumerFactory<com.github.fourbitben.sample.ValueEvent>() {
            @Override
            public Consumer<ValueEvent> createConsumer(int ordinal, int totalNumberOfConsumers) {
                return new com.github.fourbitben.sample.NoOpConsumer();
            }
        };

        EventFactory<ValueEvent> eventFactory = new EventFactory<com.github.fourbitben.sample.ValueEvent>() {
            @Override
            public com.github.fourbitben.sample.ValueEvent createEvent(long totalPublish) {
                return new com.github.fourbitben.sample.ValueEvent();
            }

            @Override
            public void setEventValue(ValueEvent event, long currentPublishNumber) {
                event.setValue(currentPublishNumber);
            }
        };

        WorkHandlerRunner<ValueEvent> runner = new WorkHandlerRunner<>(ValueEvent.EVENT_FACTORY, consumerFactory, eventFactory);
        return runner.run(numThreads, size);
    }

    private static long runPrintRingWorkHandler(int numThreads, long size) throws Exception {
        ConsumerFactory<ValueEvent> consumerFactory = new ConsumerFactory<com.github.fourbitben.sample.ValueEvent>() {
            @Override
            public Consumer<ValueEvent> createConsumer(int ordinal, int totalNumberOfConsumers) {
                return new PrintConsumer();
            }
        };

        EventFactory<ValueEvent> eventFactory = new EventFactory<com.github.fourbitben.sample.ValueEvent>() {
            @Override
            public com.github.fourbitben.sample.ValueEvent createEvent(long totalPublish) {
                return new com.github.fourbitben.sample.ValueEvent();
            }

            @Override
            public void setEventValue(ValueEvent event, long currentPublishNumber) {
                event.setValue(currentPublishNumber);
            }
        };

        WorkHandlerRunner<ValueEvent> runner = new WorkHandlerRunner<>(ValueEvent.EVENT_FACTORY, consumerFactory, eventFactory);
        return runner.run(numThreads, size);
    }
}
