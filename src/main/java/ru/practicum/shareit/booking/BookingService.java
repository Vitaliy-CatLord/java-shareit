package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

public interface BookingService {
    BookingDtoOut add(Long userId, BookingDto bookingDto);

    BookingDtoOut updateBookingStatus(Long userId, Long bookingId, Boolean approved);

    BookingDtoOut findBookingByUserId(Long userId, Long bookingId);

    List<BookingDtoOut> findAllByState(Long bookerId, String state, Integer from, Integer size);

    List<BookingDtoOut> findAllOwnersBooking(Long ownerId, String state, Integer from, Integer size);
}
