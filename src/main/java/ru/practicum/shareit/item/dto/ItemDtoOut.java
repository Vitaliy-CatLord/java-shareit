package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoOut {
    Long id;
    String name;
    String description;
    Boolean available;
    BookingDtoOut lastBooking;
    List<CommentDtoOut> comments;
    BookingDtoOut nextBooking;
    Long requestId;
}
