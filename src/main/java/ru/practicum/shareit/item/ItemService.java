package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOut;

import java.util.List;

public interface ItemService {
    ItemDtoOut createItem(Long userId, ItemDto dto);

    ItemDtoOut updateItem(Long userId, Long itemId, ItemDto dto);

    ItemDtoOut getItemById(Long userId, Long itemId);

    List<ItemDtoOut> getOwnerItems(Long userId);

    List<ItemDtoOut> searchItems(String text);

    void removeItem(Long itemId);
}
