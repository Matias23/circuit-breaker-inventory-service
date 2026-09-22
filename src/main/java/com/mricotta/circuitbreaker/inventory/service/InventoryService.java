package com.mricotta.circuitbreaker.inventory.service;

import com.mricotta.circuitbreaker.inventory.dto.CreateInventoryItemRequest;
import com.mricotta.circuitbreaker.inventory.dto.InventoryItemResponse;
import com.mricotta.circuitbreaker.inventory.dto.StockCheckResponse;
import java.util.List;

public interface InventoryService {

    StockCheckResponse checkStock(Long productId, int quantity);

    List<InventoryItemResponse> listInventory();

    InventoryItemResponse createItem(CreateInventoryItemRequest request);
}
