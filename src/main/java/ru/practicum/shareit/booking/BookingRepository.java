package ru.practicum.shareit.booking;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BookingRepository {
    private static final Map<Long, Booking> storage = new HashMap<>();

    public Booking save(Booking booking) {
        booking.setId(getNextId());
        storage.put(booking.getId(), booking);
        return booking;
    }

    public Booking findById(long id) {
        return storage.get(id);
    }

    public List<Booking> findAll() {
        return storage.values().stream()
                .toList();
    }

    public void deleteById(long id) {
        storage.remove(id);
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
