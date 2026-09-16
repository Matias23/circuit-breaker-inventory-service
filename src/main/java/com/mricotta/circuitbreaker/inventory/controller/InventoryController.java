package com.mricotta.circuitbreaker.inventory.controller;

import com.mricotta.circuitbreaker.inventory.dto.FaultStatusResponse;
import com.mricotta.circuitbreaker.inventory.dto.StockCheckResponse;
import com.mricotta.circuitbreaker.inventory.service.FaultService;
import com.mricotta.circuitbreaker.inventory.service.InventoryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/inventory")
@RequiredArgsConstructor
@Validated
public class InventoryController {

    private final InventoryService inventoryService;
    private final FaultService faultService;

    @GetMapping("/{productId}")
    public ResponseEntity<StockCheckResponse> checkStock(
            @PathVariable Long productId, @RequestParam @Positive int quantity) {
        return ResponseEntity.ok(inventoryService.checkStock(productId, quantity));
    }

    @PostMapping("/toggle-fault")
    public ResponseEntity<FaultStatusResponse> toggleFault() {
        return ResponseEntity.ok(new FaultStatusResponse(faultService.toggle()));
    }
}
