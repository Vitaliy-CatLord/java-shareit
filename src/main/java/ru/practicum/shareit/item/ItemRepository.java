package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ItemRepository {
    private static final Map<Long, Item> storage = new HashMap<>();

    public Item save(Item item) {
        item.setId(getNextId());
        storage.put(item.getId(), item);
        return item;
    }

    public Item findById(long itemId) {
        return storage.get(itemId);
    }

    public List<Item> findAll() {
        return storage.values().stream()
                .toList();
    }

    public List<Item> findByItemId(long itemId) {
        return storage.values().stream()
                .filter(item -> item.getOwner().getId() == itemId)
                .toList();
    }

    public Item updateItem(long id, Item item) {
        Item oldItem = findById(id);
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        storage.put(id, oldItem);
        return oldItem;
    }

    public void deleteById(long itemId) {
        storage.remove(itemId);
    }

    private long getNextId() {
        long currentMaxId = storage.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
