package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exceptions.NotFoundException;
import ru.practicum.shareit.Exceptions.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemService {
    ItemRepository itemRepository;
    UserRepository userRepository;

    public Item createItem(long userId, ItemDto dto) {
        if (dto.getName().isEmpty() || dto.getName().isBlank()) {
            throw new ValidationException("У предмета должно быть название");
        }
        if (dto.getDescription().isEmpty() || dto.getDescription().isBlank()) {
            throw new ValidationException("У предмета должно быть описание");
        }
        if (dto.getAvailable() == null) {
            throw new ValidationException("У предмета должна быть обозначена Доступность");
        }
        User owner = userRepository.findById(userId);
        if (owner == null) {
            throw new NotFoundException("Пользователя с ID " + userId + " не существует");
        }
        Item item = ItemMapper.toItem(dto);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    public Item updateItem(long userId, long itemId, ItemDto dto) {
        Item newItem = ItemMapper.toItem(dto);
        Item item = itemRepository.findById(itemId);
        if (item.getOwner().getId() == userId) {
            return itemRepository.updateItem(itemId, newItem);
        } else {
            throw new NotFoundException("Владелец вещи другой человек");
        }
    }

    public Item getItemById(long itemId) {
        return itemRepository.findById(itemId);
    }

    public List<Item> getOwnerItems(long userId) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getOwner().getId() == userId)
                .toList();
    }

    public List<Item> getAll() {
        return itemRepository.findAll().stream()
                .toList();
    }

    public List<Item> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        return itemRepository.findAll().stream()
                .filter(item -> item.getAvailable().equals(true))
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .toList();
    }

    public void removeItem(long itemId) {
        itemRepository.deleteById(itemId);
    }
}
