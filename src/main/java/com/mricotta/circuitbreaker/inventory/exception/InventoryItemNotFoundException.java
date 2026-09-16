package com.mricotta.circuitbreaker.inventory.exception;

public class InventoryItemNotFoundException extends RuntimeException {

    public InventoryItemNotFoundException(Long productId) {
        super("Inventory item with id %d not found".formatted(productId));
    }
}
