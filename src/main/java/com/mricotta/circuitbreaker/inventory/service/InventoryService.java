package com.mricotta.circuitbreaker.inventory.service;

import com.mricotta.circuitbreaker.inventory.dto.StockCheckResponse;

public interface InventoryService {

    StockCheckResponse checkStock(Long productId, int quantity);
}
