package ru.practicum.shareit.booking.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.models.Booking;
import ru.practicum.shareit.booking.models.BookingState;
import ru.practicum.shareit.booking.models.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.models.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImp implements BookingService {
    BookingRepository bookingRepository;
    UserRepository userRepository;
    ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingCreateDto bookingDto) {
        validateBookingCreateDto(bookingDto);

        User user = userIsExist(userId);
        Item item = itemIsExist(bookingDto.getItemId());

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь не доступна для бронирования.");
        }
        if (user.getId().equals(item.getOwner().getId())) {
            throw new ValidationException("Бронирующий и владелец - один человек.");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ValidationException("Дата окончания не может быть раньше или равна дате начала");
        }

        Booking booking = BookingMapper.toBooking(user, item, bookingDto);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto aproveBooking(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = bookingIsExist(bookingId);

        Item item = booking.getItem();
        if (item == null) {
            throw new ValidationException("У брони нет предмета");
        }

        User owner = item.getOwner();
        if (owner == null || !owner.getId().equals(ownerId)) {
            throw new ValidationException("Пользователь не является владельцем предмета");
        }

        if (!BookingStatus.WAITING.equals(booking.getStatus())) {
            throw new ValidationException("Бронь не находится в статусе WAITING");
        }

        BookingStatus newStatus = (Boolean.TRUE.equals(approved))
                ? BookingStatus.APPROVED
                : BookingStatus.REJECTED;

        booking.setStatus(newStatus);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto findBookingByUserId(Long userId, Long bookingId) {
        Booking booking = bookingIsExist(bookingId);
        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Пользователь не владелец и не автор бронирования ");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> findAllByState(Long bookerId, BookingState bookingState, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        userIsExist(bookerId);
        return switch (bookingState) {
            case ALL -> bookingRepository.findAllBookingsByBookerId(bookerId, pageable).stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
            case CURRENT ->
                    bookingRepository.findAllCurrentBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case PAST ->
                    bookingRepository.findAllPastBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case FUTURE ->
                    bookingRepository.findAllFutureBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case WAITING ->
                    bookingRepository.findAllWaitingBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case REJECTED ->
                    bookingRepository.findAllRejectedBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            default -> throw new IllegalArgumentException("Неизвестный статус бронирования" + bookingState);
        };
    }

    @Override
    public List<BookingDto> findAllOwnersBooking(Long ownerId, BookingState bookingState, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        userIsExist(ownerId);
        return switch (bookingState) {
            case ALL -> bookingRepository.findAllBookingsByOwnerId(ownerId, pageable).stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
            case CURRENT ->
                    bookingRepository.findAllCurrentBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case PAST -> bookingRepository.findAllPastBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
            case FUTURE ->
                    bookingRepository.findAllFutureBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case WAITING ->
                    bookingRepository.findAllWaitingBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingDto)
                            .toList();
            case REJECTED -> bookingRepository.findAllRejectedBookingsByOwnerId(ownerId, pageable).stream()
                    .map(BookingMapper::toBookingDto)
                    .toList();
            default -> throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
        };
    }

    User userIsExist(Long userId) {
        Optional<User> userById = userRepository.findById(userId);
        if (userById.isEmpty()) {
            throw new NotFoundException("Пользователь не найден.");
        }
        return userById.get();
    }

    Item itemIsExist(Long itemId) {
        Optional<Item> itemById = itemRepository.findById(itemId);
        if (itemById.isEmpty()) {
            throw new NotFoundException("Вещь не найдена.");
        }
        return itemById.get();
    }

    Booking bookingIsExist(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь с ID " + bookingId + " не найдена."));
    }

    void validateBookingCreateDto(BookingCreateDto createDto) {
        if(createDto == null) {
            throw new ValidationException("Заявка брони нулевая");
        }
        if(createDto.getItemId() == null) {
            throw new ValidationException("Идентификатор вещи в заявке нулевой");
        }

        if(createDto.getStart().isAfter(createDto.getEnd())) {
            throw new ValidationException("Старт заявки указан после ее завершения");
        }
    }

}
