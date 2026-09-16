package com.mricotta.circuitbreaker.inventory.config;

import com.mricotta.circuitbreaker.inventory.entity.InventoryItem;
import com.mricotta.circuitbreaker.inventory.repository.InventoryItemRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class InventoryDataInitializer {

    /**
     * Seeds the in-memory database on startup. The keyboard is seeded with zero stock so the
     * "out of stock" path can be exercised without editing any data first.
     */
    @Bean
    public ApplicationRunner seedInventory(InventoryItemRepository inventoryItemRepository) {
        return args -> {
            if (inventoryItemRepository.count() > 0) {
                return;
            }
            var items = List.of(
                    InventoryItem.builder().name("Laptop").quantity(10).build(),
                    InventoryItem.builder().name("Mouse").quantity(50).build(),
                    InventoryItem.builder().name("Keyboard").quantity(0).build());
            inventoryItemRepository.saveAll(items);
            log.info("Seeded {} inventory items", items.size());
        };
    }
}
