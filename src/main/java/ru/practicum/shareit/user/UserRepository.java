package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {
    private static final Map<Long, User> storage = new HashMap<>();

    public User save(User user) {
        user.setId(getNextId());
        storage.put(user.getId(), user);
        return user;
    }

    public void update(long id, User user) {
        storage.put(id, user);
    }

    public User findById(long userId) {
        return storage.get(userId);
    }

    public List<User> findAll() {
        return storage.values().stream()
                .toList();
    }

    public void deleteById(long userId) {
        storage.remove(userId);
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
