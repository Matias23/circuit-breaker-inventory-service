package com.mricotta.circuitbreaker.inventory.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mricotta.circuitbreaker.inventory.dto.InventoryItemResponse;
import com.mricotta.circuitbreaker.inventory.dto.StockCheckResponse;
import com.mricotta.circuitbreaker.inventory.exception.InventoryFaultException;
import com.mricotta.circuitbreaker.inventory.exception.InventoryItemNotFoundException;
import com.mricotta.circuitbreaker.inventory.service.FaultService;
import com.mricotta.circuitbreaker.inventory.service.InventoryService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private FaultService faultService;

    @Test
    void listInventory_returnsOkWithEveryItem() throws Exception {
        given(inventoryService.listInventory()).willReturn(List.of(
                new InventoryItemResponse(1L, "Laptop", 10, Instant.now()),
                new InventoryItemResponse(3L, "Keyboard", 0, Instant.now())));

        mockMvc.perform(get("/v1/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].quantity").value(10))
                .andExpect(jsonPath("$[1].id").value(3))
                .andExpect(jsonPath("$[1].quantity").value(0));
    }

    @Test
    void checkStock_whenAvailable_returnsOkWithStockCheck() throws Exception {
        given(inventoryService.checkStock(1L, 2)).willReturn(new StockCheckResponse(1L, 2, 10, true));

        mockMvc.perform(get("/v1/inventory/1").param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.requested").value(2))
                .andExpect(jsonPath("$.available").value(10))
                .andExpect(jsonPath("$.inStock").value(true));
    }

    @Test
    void checkStock_whenItemMissing_returnsNotFound() throws Exception {
        given(inventoryService.checkStock(99L, 1)).willThrow(new InventoryItemNotFoundException(99L));

        mockMvc.perform(get("/v1/inventory/99").param("quantity", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Inventory item with id 99 not found"))
                .andExpect(jsonPath("$.path").value("/v1/inventory/99"));
    }

    @Test
    void checkStock_whenFaultEnabled_returnsInternalServerError() throws Exception {
        given(inventoryService.checkStock(1L, 2)).willThrow(new InventoryFaultException(1L));

        mockMvc.perform(get("/v1/inventory/1").param("quantity", "2"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Inventory fault simulation is enabled; stock check for product 1 rejected"));
    }

    @Test
    void toggleFault_returnsTheResultingState() throws Exception {
        given(faultService.toggle()).willReturn(true);

        mockMvc.perform(post("/v1/inventory/toggle-fault"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faultEnabled").value(true));
    }
}
