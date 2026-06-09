package ru.practicum.shareit.item.mappers;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOut;
import ru.practicum.shareit.item.models.Item;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {

        ItemDto dto = new ItemDto();
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setRequestId(item.getRequest().getId());
        return dto;
    }

    public static ItemDtoOut toItemDtoOut(Item item) {

        ItemDtoOut out = new ItemDtoOut();
        out.setId(item.getId());
        out.setName(item.getName());
        out.setDescription(item.getDescription());
        out.setAvailable(item.getAvailable());
        if (item.getRequest() != null) {
            out.setRequestId(item.getRequest().getId());
        }
        return out;
    }

    public static Item toItem(ItemDto dto) {
        Item item = new Item();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        return item;
    }
}
