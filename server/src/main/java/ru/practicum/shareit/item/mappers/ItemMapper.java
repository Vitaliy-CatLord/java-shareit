package ru.practicum.shareit.item.mappers;

import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.request.models.ItemRequest;
import ru.practicum.shareit.user.models.User;

import java.util.List;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {

        ItemDto dto = new ItemDto();
        if (item.getId() != null) {
            dto.setId(item.getId());
        }
        if (item.getName() != null) {
            dto.setName(item.getName());
        }
        if (item.getDescription() != null) {
            dto.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            dto.setAvailable(item.getAvailable());
        }
        if (item.getRequest() != null) {
            dto.setRequestId(item.getRequest().getId());
        }
        return dto;
    }

    // Перегрузка для владельца: добавляем last/next и комментарии
    public static ItemDto toItemDto(Item item,
                                    BookingShortDto lastBooking,
                                    BookingShortDto nextBooking,
                                    List<CommentDto> comments) {
        ItemDto dto = toItemDto(item);
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(comments);
        return dto;
    }

    public static Item toItem(ItemDto dto, User owner, ItemRequest request) {

        Item item = new Item();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable() != null ? dto.getAvailable() : false);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }
}
