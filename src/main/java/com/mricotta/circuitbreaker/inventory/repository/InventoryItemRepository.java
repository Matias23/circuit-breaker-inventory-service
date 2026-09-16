package com.mricotta.circuitbreaker.inventory.repository;

import com.mricotta.circuitbreaker.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
}
