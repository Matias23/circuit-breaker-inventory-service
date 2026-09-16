package com.mricotta.circuitbreaker.inventory.exception;

/**
 * Thrown while the injected fault is active, so the service answers 500 to every stock check.
 * It is the lever the order service's circuit breaker will be exercised against.
 */
public class InventoryFaultException extends RuntimeException {

    public InventoryFaultException(Long productId) {
        super("Inventory fault simulation is enabled; stock check for product %d rejected".formatted(productId));
    }
}
