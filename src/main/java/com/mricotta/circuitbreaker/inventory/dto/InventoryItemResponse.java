package com.mricotta.circuitbreaker.inventory.dto;

import java.time.Instant;

public record InventoryItemResponse(
        Long id,
        String name,
        Integer quantity,
        Instant updatedAt) {
}
