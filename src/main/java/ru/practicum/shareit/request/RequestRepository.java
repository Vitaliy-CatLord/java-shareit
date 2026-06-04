package ru.practicum.shareit.request;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RequestRepository {
    private static final Map<Long, ItemRequest> storage = new HashMap<>();

    public ItemRequest save(ItemRequest request) {
        request.setId(getNextId());
        storage.put(request.getId(), request);
        return request;
    }

    public ItemRequest findById(long requestId) {
        return storage.get(requestId);
    }

    public List<ItemRequest> findAll() {
        return storage.values().stream()
                .toList();
    }

    public void deleteById(long requestId) {
        storage.remove(requestId);
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
