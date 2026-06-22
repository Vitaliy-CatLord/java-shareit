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
        try {
            return BookingState.valueOf(bookingState.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
