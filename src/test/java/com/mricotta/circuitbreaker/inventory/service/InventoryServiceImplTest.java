package com.mricotta.circuitbreaker.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

import com.mricotta.circuitbreaker.inventory.dto.InventoryItemResponse;
import com.mricotta.circuitbreaker.inventory.entity.InventoryItem;
import com.mricotta.circuitbreaker.inventory.exception.InventoryFaultException;
import com.mricotta.circuitbreaker.inventory.exception.InventoryItemNotFoundException;
import com.mricotta.circuitbreaker.inventory.mapper.InventoryItemMapper;
import com.mricotta.circuitbreaker.inventory.repository.InventoryItemRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private InventoryItemMapper inventoryItemMapper;

    @Mock
    private FaultService faultService;

    @Captor
    private ArgumentCaptor<Sort> sortCaptor;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void checkStock_whenEnoughStock_returnsInStock() {
        given(faultService.isFaultEnabled()).willReturn(false);
        given(inventoryItemRepository.findById(1L)).willReturn(Optional.of(item(1L, "Laptop", 10)));

        var response = inventoryService.checkStock(1L, 2);

        assertThat(response.productId()).isEqualTo(1L);
        assertThat(response.requested()).isEqualTo(2);
        assertThat(response.available()).isEqualTo(10);
        assertThat(response.inStock()).isTrue();
    }

    @Test
    void checkStock_whenNotEnoughStock_returnsOutOfStockWithoutThrowing() {
        given(faultService.isFaultEnabled()).willReturn(false);
        given(inventoryItemRepository.findById(3L)).willReturn(Optional.of(item(3L, "Keyboard", 0)));

        var response = inventoryService.checkStock(3L, 1);

        assertThat(response.available()).isZero();
        assertThat(response.inStock()).isFalse();
    }

    @Test
    void checkStock_whenFaultEnabled_throwsWithoutHittingTheDatabase() {
        given(faultService.isFaultEnabled()).willReturn(true);

        assertThatThrownBy(() -> inventoryService.checkStock(1L, 2))
                .isInstanceOf(InventoryFaultException.class)
                .hasMessageContaining("fault simulation is enabled");

        then(inventoryItemRepository).shouldHaveNoInteractions();
    }

    @Test
    void checkStock_whenItemMissing_throwsNotFound() {
        given(faultService.isFaultEnabled()).willReturn(false);
        given(inventoryItemRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.checkStock(99L, 1))
                .isInstanceOf(InventoryItemNotFoundException.class)
                .hasMessage("Inventory item with id 99 not found");
    }

    @Test
    void listInventory_returnsEveryItemSortedById() {
        var items = List.of(item(1L, "Laptop", 10), item(3L, "Keyboard", 0));
        var expected = List.of(
                new InventoryItemResponse(1L, "Laptop", 10, Instant.now()),
                new InventoryItemResponse(3L, "Keyboard", 0, Instant.now()));
        given(inventoryItemRepository.findAll(sortCaptor.capture())).willReturn(items);
        given(inventoryItemMapper.toDtoList(items)).willReturn(expected);

        assertThat(inventoryService.listInventory()).containsExactlyElementsOf(expected);
        assertThat(sortCaptor.getValue()).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Test
    void listInventory_whenFaultEnabled_stillAnswers() {
        given(inventoryItemRepository.findAll(any(Sort.class))).willReturn(List.of());
        given(inventoryItemMapper.toDtoList(List.of())).willReturn(List.of());

        assertThat(inventoryService.listInventory()).isEmpty();

        // The inspection endpoint must stay usable while the stock checks are failing.
        then(faultService).shouldHaveNoInteractions();
    }

    private static InventoryItem item(Long id, String name, int quantity) {
        return InventoryItem.builder().id(id).name(name).quantity(quantity).build();
    }
}
