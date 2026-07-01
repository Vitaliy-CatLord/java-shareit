package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.models.BookingState;

import static org.junit.jupiter.api.Assertions.*;

class BookingStateTest {

    @Test
    void from_null_returnsALL() {
        assertEquals(BookingState.ALL, BookingState.from(null));
    }

    @Test
    void from_valid_ignoreCase() {
        assertEquals(BookingState.CURRENT, BookingState.from("current"));
        assertEquals(BookingState.WAITING, BookingState.from("WaItInG"));
    }

    @Test
    void from_invalid_throwIAE() {
        assertThrows(IllegalArgumentException.class, () -> BookingState.from("WHAT??"));
    }
}