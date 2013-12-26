package com.github.fourbitben;

/**
 * Used by the runner to create a consumer to do work.
 *
 * @author Ben Nelson
 */
public interface ConsumerFactory<T> {
    /**
     * Create a consumer.
     *
     * @param ordinal the ordinal of this consumer - 0 to <code>totalNumberOfConsumers</code>-1
     * @param totalNumberOfConsumers the total number of consumers that will be created
     * @return the newly constructed consumer
     */
    Consumer<T> createConsumer(int ordinal, int totalNumberOfConsumers);
}
