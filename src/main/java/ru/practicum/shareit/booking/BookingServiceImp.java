package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.Exceptions.NotFoundException;
import ru.practicum.shareit.Exceptions.ValidationException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImp implements BookingService{
    BookingRepository bookingRepository;
    UserRepository userRepository;
    ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDtoOut add(Long userId, BookingDto bookingDto) {
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
        return BookingMapper.toBookingOut(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDtoOut updateBookingStatus(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingIsExist(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Пользователь не является владельцем");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Бронь не cо статусом WAITING");
        }
        BookingStatus newStatus = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(newStatus);
        return BookingMapper.toBookingOut(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoOut findBookingByUserId(Long userId, Long bookingId) {
        Booking booking = bookingIsExist(bookingId);
        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Пользователь не владелец и не автор бронирования ");
        }
        return BookingMapper.toBookingOut(booking);
    }

    @Override
    public List<BookingDtoOut> findAllByState(Long bookerId, String bookingState, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        userIsExist(bookerId);
        return switch (BookingState.from(bookingState)) {
            case ALL -> bookingRepository.findAllBookingsByBookerId(bookerId, pageable).stream()
                    .map(BookingMapper::toBookingOut)
                    .toList();
            case CURRENT ->
                    bookingRepository.findAllCurrentBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingOut)
                            .toList();
            case PAST ->
                    bookingRepository.findAllPastBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingOut)
                            .toList();
            case FUTURE ->
                    bookingRepository.findAllFutureBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingOut)
                            .toList();
            case WAITING ->
                    bookingRepository.findAllWaitingBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingOut)
                            .toList();
            case REJECTED ->
                    bookingRepository.findAllRejectedBookingsByBookerId(bookerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingOut)
                            .toList();
            default -> throw new IllegalArgumentException("Неизвестный статус бронирования" + bookingState);
        };
    }

    @Override
    public List<BookingDtoOut> findAllOwnersBooking(Long ownerId, String bookingState, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        userIsExist(ownerId);
        return switch (BookingState.from(bookingState)) {
            case ALL -> bookingRepository.findAllBookingsByOwnerId(ownerId, pageable).stream()
                    .map(BookingMapper::toBookingOut)
                    .toList();
            case CURRENT ->
                    bookingRepository.findAllCurrentBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingOut)
                            .toList();
            case PAST -> bookingRepository.findAllPastBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                    .map(BookingMapper::toBookingOut)
                    .toList();
            case FUTURE ->
                    bookingRepository.findAllFutureBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingOut)
                            .toList();
            case WAITING ->
                    bookingRepository.findAllWaitingBookingsByOwnerId(ownerId, LocalDateTime.now(), pageable).stream()
                            .map(BookingMapper::toBookingOut)
                            .toList();
            case REJECTED -> bookingRepository.findAllRejectedBookingsByOwnerId(ownerId, pageable).stream()
                    .map(BookingMapper::toBookingOut)
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
        Optional<Booking> bookingById = bookingRepository.findById(bookingId);
        if (bookingById.isEmpty()) {
            throw new NotFoundException("Бронь не найдена.");
        }

        return bookingById.get();
    }

}
