package com.github.fourbitben.sample;

import com.github.fourbitben.Consumer;

/**
 * Sample implementation. Prints the event to System.out.
 *
 * @author Ben Nelson
 */
public class NoOpConsumer implements Consumer<ValueEvent> {
    @Override
    public void onEvent(ValueEvent event) {
        // Do Nothing
    }
}
