package com.github.fourbitben;

/**
 * Interface for creating an event, will send this event to the consumer.
 *
 * @author Ben Nelson
 */
public interface Producer<T> {
    /**
     * Create an event to be processed.
     *
     * @return the event
     */
    T createEvent();
}
