package com.mricotta.circuitbreaker.inventory.service;

import com.mricotta.circuitbreaker.inventory.dto.InventoryItemResponse;
import com.mricotta.circuitbreaker.inventory.dto.StockCheckResponse;
import com.mricotta.circuitbreaker.inventory.exception.InventoryFaultException;
import com.mricotta.circuitbreaker.inventory.exception.InventoryItemNotFoundException;
import com.mricotta.circuitbreaker.inventory.mapper.InventoryItemMapper;
import com.mricotta.circuitbreaker.inventory.repository.InventoryItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final Sort BY_ID = Sort.by(Sort.Direction.ASC, "id");

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemMapper inventoryItemMapper;
    private final FaultService faultService;

    /**
     * Read-only on purpose: the check never reserves or decrements stock, so the same request can be
     * replayed as many times as the circuit breaker experiment needs.
     */
    @Override
    @Transactional(readOnly = true)
    public StockCheckResponse checkStock(Long productId, int quantity) {
        // Checked before touching the database: the fault stands for the whole service being down.
        if (faultService.isFaultEnabled()) {
            throw new InventoryFaultException(productId);
        }
        var item = inventoryItemRepository.findById(productId)
                .orElseThrow(() -> new InventoryItemNotFoundException(productId));
        var available = item.getQuantity();
        return new StockCheckResponse(productId, quantity, available, available >= quantity);
    }

    /**
     * Not affected by the injected fault: this is the inspection endpoint, and it is most useful
     * precisely while the stock checks are failing.
     */
    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> listInventory() {
        return inventoryItemMapper.toDtoList(inventoryItemRepository.findAll(BY_ID));
    }
}
