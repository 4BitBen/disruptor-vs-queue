package com.github.fourbitben;

import com.codahale.metrics.Timer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EventTimerManager<T> {
    private ConcurrentMap<T, Timer.Context> contexts = new ConcurrentHashMap<>();

    public void startTimer(T key, Timer.Context context) {
        if(contexts.putIfAbsent(key, context) != null) {
            throw new IllegalArgumentException(key + " already exists.");
        }
    }

    public void stopTimer(T key) {
        Timer.Context context = contexts.remove(key);
        if(context == null) {
            throw new IllegalArgumentException(key + " does not exist or has already been processed.");
        }
        context.stop();
    }

    public void destroy() {
        if(!contexts.isEmpty()) {
            throw new IllegalArgumentException("Not all messages processed: " + contexts.keySet());
        }
    }
}
