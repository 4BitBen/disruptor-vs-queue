package com.github.fourbitben.sample;

import com.github.fourbitben.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample implementation. Prints the event to System.out.
 *
 * @author Ben Nelson
 */
public class PrintConsumer implements Consumer<ValueEvent> {
    Logger logger = LoggerFactory.getLogger(PrintConsumer.class);

    @Override
    public void onEvent(ValueEvent event) {
        logger.info(event.toString());
    }
}
