package com.mricotta.circuitbreaker.inventory.mapper;

import com.mricotta.circuitbreaker.inventory.dto.InventoryItemResponse;
import com.mricotta.circuitbreaker.inventory.entity.InventoryItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryItemMapper {

    InventoryItemResponse toDto(InventoryItem item);

    List<InventoryItemResponse> toDtoList(List<InventoryItem> items);
}
