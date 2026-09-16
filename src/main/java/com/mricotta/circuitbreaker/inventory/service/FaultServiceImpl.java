package com.mricotta.circuitbreaker.inventory.service;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FaultServiceImpl implements FaultService {

    // Singleton bean, so the flag is process-wide; atomic because virtual threads read it concurrently.
    private final AtomicBoolean faultEnabled = new AtomicBoolean(false);

    @Override
    public boolean toggle() {
        boolean previous;
        // AtomicBoolean has no flip operation, so compare-and-set until this thread wins the race.
        do {
            previous = faultEnabled.get();
        } while (!faultEnabled.compareAndSet(previous, !previous));
        var enabled = !previous;
        log.warn("Fault simulation toggled to {}", enabled);
        return enabled;
    }

    @Override
    public boolean isFaultEnabled() {
        return faultEnabled.get();
    }
}
