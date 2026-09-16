package com.mricotta.circuitbreaker.inventory.dto;

public record StockCheckResponse(
        Long productId,
        int requested,
        int available,
        boolean inStock) {
}
