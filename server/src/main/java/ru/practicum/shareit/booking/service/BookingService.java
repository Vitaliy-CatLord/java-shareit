package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.models.BookingState;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(Long userId, BookingCreateDto bookingDto);

    BookingDto aproveBooking(Long userId, Long bookingId, Boolean approved);

    BookingDto findBookingByUserId(Long userId, Long bookingId);

    List<BookingDto> findAllByState(Long bookerId, BookingState state, Integer from, Integer size);

    List<BookingDto> findAllOwnersBooking(Long ownerId, BookingState state, Integer from, Integer size);
}
