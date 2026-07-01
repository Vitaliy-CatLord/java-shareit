package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto dto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto dto);

    ItemDto getItemById(Long userId, Long itemId);

    List<ItemDto> getOwnerItems(Long userId);

    List<ItemDto> searchItems(String text);

    void removeItem(Long itemId);

    CommentDto createComment(Long userId, CommentCreateDto commentDto, Long itemId);
}
