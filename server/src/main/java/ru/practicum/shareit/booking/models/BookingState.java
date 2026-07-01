package ru.practicum.shareit.booking.models;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED,
    UNKNOWN;

    public static BookingState from(String bookingState) {
        if (bookingState == null) {
            return ALL;
        }
        return BookingState.valueOf(bookingState.toUpperCase());
    }
}
