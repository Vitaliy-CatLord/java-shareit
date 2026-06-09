package ru.practicum.shareit.item.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.Exceptions.NotFoundException;
import ru.practicum.shareit.Exceptions.ValidationException;
import ru.practicum.shareit.booking.models.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.models.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.item.mappers.CommentMapper;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.mappers.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOut;
import ru.practicum.shareit.item.models.Comment;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.user.models.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemServiceImp implements ItemService {

    CommentRepository commentRepository;
    BookingRepository bookingRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;

    @Override
    @Transactional
    public ItemDtoOut createItem(Long userId, ItemDto dto) {
        if (dto.getName().isEmpty() || dto.getName().isBlank()) {
            throw new ValidationException("У предмета должно быть название");
        }
        if (dto.getDescription().isEmpty() || dto.getDescription().isBlank()) {
            throw new ValidationException(String.format("У %s должно быть описание", dto.getName()));
        }
        if (dto.getAvailable() == null) {
            throw new ValidationException(String.format("У %s должна быть обозначена Доступность", dto.getName()));
        }
        Optional<User> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            throw new NotFoundException(String.format("Пользователя с ID %s не существует", userId));
        }
        Item item = ItemMapper.toItem(dto);
        item.setOwner(owner.get());
        return ItemMapper.toItemDtoOut(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDtoOut updateItem(Long userId, Long itemId, ItemDto dto) {
        Optional<User> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            throw new NotFoundException(String.format("Пользователя с ID %s не существует", userId));
        }
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) {
            throw new NotFoundException(String.format("Предмет с ID %s не найден", itemId));
        }
        if (!item.get().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId +
                    " не является владельцем вещи с id = " + itemId);
        }
        Item oldItem = item.get();

        String name = dto.getName();
        if (name != null && !name.isBlank()) {
            oldItem.setName(name);
        }
        String description = dto.getDescription();
        if (description != null && !description.isBlank()) {
            oldItem.setDescription(description);
        }
        Boolean isAvailable = dto.getAvailable();
        if (isAvailable != null) {
            oldItem.setAvailable(isAvailable);
        }
        itemRepository.save(oldItem);
        return ItemMapper.toItemDtoOut(oldItem);

    }

    @Override
    @Transactional
    public ItemDtoOut getItemById(Long userId, Long itemId) {
        Optional<User> owner = userRepository.findById(userId);
        if (owner.isEmpty()) {
            throw new NotFoundException(String.format("Пользователя с ID %s не существует", userId));
        }
        Optional<Item> itemGet = itemRepository.findById(itemId);
        if (itemGet.isEmpty()) {
            throw new NotFoundException(String.format("Вещь с ID %s не существует", itemId));
        }
        Item item = itemGet.get();
        ItemDtoOut itemDtoOut = ItemMapper.toItemDtoOut(itemGet.get());
        itemDtoOut.setComments(getAllItemComments(itemId));
        if (!item.getOwner().getId().equals(userId)) {
            return itemDtoOut;
        }
        List<BookingDtoOut> bookingDTOList = bookingRepository.findAllByItemAndStatusOrderByStartDateAsc(item, BookingStatus.APPROVED)
                .stream()
                .map(BookingMapper::toBookingOut)
                .toList();
        itemDtoOut.setLastBooking(getLastBooking(bookingDTOList, LocalDateTime.now()));
        itemDtoOut.setNextBooking(getNextBooking(bookingDTOList, LocalDateTime.now()));
        return itemDtoOut;
    }

    @Transactional
    public List<ItemDtoOut> getOwnerItems(Long userId) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDtoOut)
                .toList();
    }

    public List<ItemDtoOut> getAll() {
        return itemRepository.findAll().stream()
                .map(ItemMapper::toItemDtoOut)
                .toList();
    }

    public List<ItemDtoOut> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDtoOut)
                .toList();
    }

    @Transactional
    public void removeItem(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    @Transactional
    public CommentDtoOut createComment(Long userId, CommentDto commentDto, Long itemId) {
        Optional<User> userGet = userRepository.findById(userId);
        if (userGet.isEmpty()) {
            throw new NotFoundException(String.format("Пользователя с ID %s не существует", userId));
        }
        User user = userGet.get();

        Optional<Item> itemGet = itemRepository.findById(itemId);
        if (itemGet.isEmpty()) {
            throw new NotFoundException("У пользователя с id = " + userId + " не " +
                    "существует вещи с id = " + itemId);
        }
        Item item = itemGet.get();

        List<Booking> userBookings = bookingRepository.findAllByUserBookings(userId, itemId, LocalDateTime.now());

        if (userBookings.isEmpty()) {
            throw new ValidationException("У пользователя с id   " + userId + " должно быть хотя бы одно бронирование предмета с id " + itemId);
        }

        return CommentMapper.toCommentDtoOut(commentRepository.save(CommentMapper.toComment(commentDto, item, user)));
    }

    List<CommentDtoOut> getAllItemComments(Long itemId) {
        List<Comment> comments = commentRepository.findAllByItemId(itemId);

        return comments.stream()
                .map(CommentMapper::toCommentDtoOut)
                .toList();
    }

    private BookingDtoOut getLastBooking(List<BookingDtoOut> bookings, LocalDateTime time) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        return bookings
                .stream()
                .filter(bookingDTO -> bookingDTO.getStart().isBefore(time))
                .reduce((booking1, booking2) -> booking2)
                .orElse(null);
    }

    private BookingDtoOut getNextBooking(List<BookingDtoOut> bookings, LocalDateTime time) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        return bookings
                .stream()
                .filter(bookingDTO -> bookingDTO.getStart().isAfter(time))
                .findFirst()
                .orElse(null);
    }
}
