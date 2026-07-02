package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.models.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_HEADER = "X-Sharer-User-Id";
    private static final String BOOKING_ID = "/{bookingId}";

    @PostMapping
    public BookingDto create(@RequestHeader(USER_HEADER) Long userId,
                             @RequestBody BookingCreateDto bookingDto) {
        log.info("Выполнение запроса на создание предмета {} пользователем {}", bookingDto, userId);
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping(BOOKING_ID)
    public BookingDto updateStatus(@RequestHeader(USER_HEADER) Long userId,
                                   @PathVariable("bookingId")
                                   Long bookingId,
                                   @RequestParam(name = "approved") Boolean approved) {
        log.info("Выполнение запроса на обновление статуса бронирования {} от владельца вещи с id: {}, статус {}",
                bookingId, userId, approved);
        return bookingService.aproveBooking(userId, bookingId, approved);
    }

    @GetMapping(BOOKING_ID)
    public BookingDto findBookingById(@RequestHeader(USER_HEADER) Long userId,
                                      @PathVariable("bookingId")
                                      Long bookingId) {
        log.info("Выполнение запроса на получение данных о бронировании от пользователя с id: {}", userId);
        return bookingService.findBookingByUserId(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> findBookingByState(@RequestHeader(USER_HEADER) Long userId,
                                               @RequestParam(value = "state", defaultValue = "ALL") String bookingState,
                                               @RequestParam(value = "from", defaultValue = "0") Integer from,
                                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("GET запрос на получение списка всех бронирований текущего пользователя с id: {} и статусом {}", userId, bookingState);
        return bookingService.findAllByState(userId, BookingState.from(bookingState), from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllOwnerBooking(@RequestHeader(USER_HEADER) Long ownerId,
                                               @RequestParam(value = "state", defaultValue = "ALL") String bookingState,
                                               @RequestParam(value = "from", defaultValue = "0") Integer from,
                                               @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("GET запрос на получение списка всех бронирований текущего владельца с id: {} и статусом {}", ownerId, bookingState);
        return bookingService.findAllOwnersBooking(ownerId, BookingState.from(bookingState), from, size);
    }
}
