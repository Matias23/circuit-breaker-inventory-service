package com.mricotta.circuitbreaker.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Zero is allowed on purpose: an item can be created already out of stock. */
public record CreateInventoryItemRequest(
        @NotBlank String name,
        @NotNull @PositiveOrZero Integer quantity) {
}
