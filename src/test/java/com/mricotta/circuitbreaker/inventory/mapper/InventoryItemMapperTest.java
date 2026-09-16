package com.mricotta.circuitbreaker.inventory.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.mricotta.circuitbreaker.inventory.entity.InventoryItem;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class InventoryItemMapperTest {

    private final InventoryItemMapper inventoryItemMapper = Mappers.getMapper(InventoryItemMapper.class);

    @Test
    void toDto_copiesEveryField() {
        var updatedAt = Instant.now();
        var dto = inventoryItemMapper.toDto(item(1L, "Laptop", 10, updatedAt));

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Laptop");
        assertThat(dto.quantity()).isEqualTo(10);
        assertThat(dto.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toDtoList_mapsEveryItem() {
        var items = List.of(item(1L, "Laptop", 10, Instant.now()), item(3L, "Keyboard", 0, Instant.now()));

        assertThat(inventoryItemMapper.toDtoList(items))
                .extracting("id", "name", "quantity")
                .containsExactly(tuple(1L, "Laptop", 10), tuple(3L, "Keyboard", 0));
    }

    private static InventoryItem item(Long id, String name, int quantity, Instant updatedAt) {
        return InventoryItem.builder().id(id).name(name).quantity(quantity).updatedAt(updatedAt).build();
    }
}
