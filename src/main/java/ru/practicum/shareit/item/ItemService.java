package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

public class ItemService {
    ItemRepository itemRepository;
    UserRepository userRepository;

    public ItemDto createItem (long userId, ItemDto dto) {
        Item item = ItemMapper.toItem(dto);
        item.setOwner(userRepository.findById(userId));
        itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    public ItemDto updateItem(long userId, long itemId, ItemDto dto) {
        Item oldItem = itemRepository.findById(itemId);
        if(oldItem.getOwner().getId() == userId) {
            oldItem.setName(dto.getName());
            oldItem.setDescription(dto.getDescription());
            oldItem.setAvailable(dto.isAvailable());
            itemRepository.update(itemId, oldItem);
            return ItemMapper.toItemDto(oldItem);
        }
        return dto;

    }

    public ItemDto getItemById(long itemId) {
        return ItemMapper.toItemDto(itemRepository.findById(itemId));
    }

    public List<ItemDto> getOwnerItems(long userId) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getOwner().getId() == userId)
                .map(ItemMapper::toItemDto)
                .toList();
    }

    public List<ItemDto> getAll () {
        return itemRepository.findAll().stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    public List<ItemDto> searchItems(String text) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getName().contains(text) || item.getDescription().contains(text))
                .map(ItemMapper::toItemDto)
                .toList();
    }

    public void removeItem(long itemId) {
        itemRepository.deleteById(itemId);
    }
}
