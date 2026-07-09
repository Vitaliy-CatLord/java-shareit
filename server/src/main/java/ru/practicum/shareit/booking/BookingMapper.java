package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.models.Booking;
import ru.practicum.shareit.booking.models.BookingStatus;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.user.models.User;

public class BookingMapper {
    public static Booking toBooking(User user, Item item, BookingCreateDto bookingDto) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStartDate(bookingDto.getStart());
        booking.setEndDate(bookingDto.getEnd());
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    public static BookingDto toBookingDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStartDate());
        dto.setEnd(booking.getEndDate());
        dto.setStatus(booking.getStatus());
        dto.setItem(BookingDto.ItemShortDto.builder()
                .id(booking.getItem().getId())
                .name(booking.getItem().getName())
                .build());
        dto.setBooker(BookingDto.UserShortDto.builder()
                .id(booking.getBooker().getId())
                .build());

        return dto;
    }
}
