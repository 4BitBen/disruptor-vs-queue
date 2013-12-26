package com.github.fourbitben;

/**
 * Factory for creating a new event
 *
 * @author Ben Nelson
 */
public interface EventFactory<T> {
    /**
     * Create a new event
     *
     * @param totalPublish the total number of events that will be published
     * @return a newly created event
     */
    T createEvent(long totalPublish);

    /**
     * Set the value for the event
     *
     * @param event the event - either that was created or is being re-used
     * @param currentPublishNumber the current number we are in publishing
     */
    void setEventValue(T event, long currentPublishNumber);
}
