package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

public class BookingMapper {
    public static Booking toBooking(User user, Item item, BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStartDate(bookingDto.getStart());
        booking.setEndDate(bookingDto.getEnd());
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    public static BookingDtoOut toBookingOut(Booking booking) {
        BookingDtoOut dtoOut = new BookingDtoOut();
        dtoOut.setId(booking.getId());
        dtoOut.setItem(ItemMapper.toItemDtoOut(booking.getItem()));
        dtoOut.setStart(booking.getStartDate());
        dtoOut.setEnd(booking.getEndDate());
        dtoOut.setBooker(UserMapper.toUserDto(booking.getBooker()));
        dtoOut.setStatus(booking.getStatus());
        return dtoOut;

    }

    public static BookingItemDto toBookingItemDto(Booking booking) {
        BookingItemDto dto = new BookingItemDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        return dto;
    }
}
