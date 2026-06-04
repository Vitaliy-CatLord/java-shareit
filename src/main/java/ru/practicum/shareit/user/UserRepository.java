package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {
    private static final Map<Long, User> storage = new HashMap<>();

    public User save(User user) {
        user.setId(getNextId());
        storage.put(user.getId(), user);
        return user;
    }

    public User update(long id, User user) {
        User oldUser = findById(id);
        oldUser.setId(id);
        if (user.getName() != null) {
            oldUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
        }
        storage.put(id, oldUser);
        return oldUser;
    }

    public User findById(long userId) {
        return storage.get(userId);
    }

    public Optional<User> findByEmail(String email) {
        return storage.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
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
