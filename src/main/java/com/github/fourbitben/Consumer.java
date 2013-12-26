package com.github.fourbitben;

/**
 * Interface to implement, will receive events to process
 *
 * @author Ben Nelson
 */
public interface Consumer<T> {
    /**
     * Called when an event needs to be processed.
     *
     * @param event the event to process
     */
    void onEvent(T event);
}
