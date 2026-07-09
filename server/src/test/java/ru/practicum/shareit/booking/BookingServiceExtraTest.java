package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.models.Booking;
import ru.practicum.shareit.booking.models.BookingState;
import ru.practicum.shareit.booking.models.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.user.models.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class BookingServiceExtraTest {
    @Autowired
    private BookingService bookingService;

    @MockBean
    private BookingRepository bookingRepository;

    // Моки для зависимостей сервиса (нужны, чтобы не падали userIsExist/itemIsExist)
    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Гарантируем, что пользователь существует (чтобы не падал userIsExist внутри сервиса)
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
    }

    @Test
    void findAllByState_all() {
        Long bookerId = 1L;
        int from = 0;
        int size = 10;

        Booking b1 = createBooking(1L, bookerId, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        Booking b2 = createBooking(2L, bookerId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

        when(bookingRepository.findAllBookingsByBookerId(eq(bookerId), any()))
                .thenReturn(List.of(b1, b2));

        List<BookingDto> result = bookingService.findAllByState(bookerId, BookingState.ALL, from, size);

        assertThat(result).hasSize(2);
        verify(bookingRepository).findAllBookingsByBookerId(eq(bookerId), any());
    }

    @Test
    void findAllByState_current() {
        Long bookerId = 2L;
        int from = 5;
        int size = 5;

        // Текущая бронь: start <= now <= end
        Booking current = createBooking(3L, bookerId, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(10));

        when(bookingRepository.findAllCurrentBookingsByBookerId(anyLong(), any(), any()))
                .thenReturn(List.of(current));

        List<BookingDto> result = bookingService.findAllByState(bookerId, BookingState.CURRENT, from, size);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findAllCurrentBookingsByBookerId(eq(bookerId), any(), any());
    }

    @Test
    void findAllByState_past() {
        Long bookerId = 3L;
        int from = 0;
        int size = 20;

        // Прошедшая бронь: end < now
        Booking past = createBooking(4L, bookerId, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));

        when(bookingRepository.findAllPastBookingsByBookerId(anyLong(), any(), any()))
                .thenReturn(List.of(past));

        List<BookingDto> result = bookingService.findAllByState(bookerId, BookingState.PAST, from, size);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findAllPastBookingsByBookerId(eq(bookerId), any(), any());
    }

    @Test
    void findAllByState_future() {
        Long bookerId = 4L;
        int from = 10;
        int size = 10;

        // Будущая бронь: start > now
        Booking future = createBooking(5L, bookerId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        when(bookingRepository.findAllFutureBookingsByBookerId(anyLong(), any(), any()))
                .thenReturn(List.of(future));

        List<BookingDto> result = bookingService.findAllByState(bookerId, BookingState.FUTURE, from, size);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findAllFutureBookingsByBookerId(eq(bookerId), any(), any());
    }

    @Test
    void findAllByState_waiting() {
        Long bookerId = 5L;
        int from = 0;
        int size = 5;

        Booking waiting = createBookingWithStatus(6L, bookerId, BookingStatus.WAITING);

        when(bookingRepository.findAllWaitingBookingsByBookerId(anyLong(), any(), any()))
                .thenReturn(List.of(waiting));

        List<BookingDto> result = bookingService.findAllByState(bookerId, BookingState.WAITING, from, size);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findAllWaitingBookingsByBookerId(eq(bookerId), any(), any());
    }

    @Test
    void findAllByState_rejected() {
        Long bookerId = 6L;
        int from = 0;
        int size = 5;

        Booking rejected = createBookingWithStatus(7L, bookerId, BookingStatus.REJECTED);

        when(bookingRepository.findAllRejectedBookingsByBookerId(anyLong(), any(), any()))
                .thenReturn(List.of(rejected));

        List<BookingDto> result = bookingService.findAllByState(bookerId, BookingState.REJECTED, from, size);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findAllRejectedBookingsByBookerId(eq(bookerId), any(), any());
    }

    @Test
    void findAllByState_userNotFound_throwsNotFound() {
        Long bookerId = 999L;

        // Возвращаем пустой Optional, чтобы сработал userIsExist -> NotFoundException
        when(userRepository.findById(eq(bookerId))).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                        bookingService.findAllByState(bookerId, BookingState.ALL, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void findAllOwnersBooking_all() {
        Long ownerId = 1L;
        int from = 0;
        int size = 10;

        Booking b1 = createBookingWithOwner(1L, 2L, ownerId, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        Booking b2 = createBookingWithOwner(2L, 3L, ownerId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));

        when(bookingRepository.findAllBookingsByOwnerId(eq(ownerId), any()))
                .thenReturn(List.of(b1, b2));

        List<BookingDto> result = bookingService.findAllOwnersBooking(ownerId, BookingState.ALL, from, size);

        assertThat(result).hasSize(2);
        verify(bookingRepository).findAllBookingsByOwnerId(eq(ownerId), any());
    }

    @Test
    void findAllOwnersBooking_current() {
        Long ownerId = 2L;
        int from = 5;
        int size = 5;

        // Текущая бронь: start <= now <= end
        Booking current = createBookingWithOwner(3L, 4L, ownerId,
                LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(10));

        when(bookingRepository.findAllCurrentBookingsByOwnerId(anyLong(), any(), any()))
                .thenReturn(List.of(current));

        List<BookingDto> result = bookingService.findAllOwnersBooking(ownerId, BookingState.CURRENT, from, size);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findAllCurrentBookingsByOwnerId(eq(ownerId), any(), any());
    }

    @Test
    void findAllOwnersBooking_past() {
        Long ownerId = 3L;
        int from = 0;
        int size = 20;

        // Прошедшая бронь: end < now
        Booking past = createBookingWithOwner(4L, 5L, ownerId,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));

        when(bookingRepository.findAllPastBookingsByOwnerId(anyLong(), any(), any()))
                .thenReturn(List.of(past));

        List<BookingDto> result = bookingService.findAllOwnersBooking(ownerId, BookingState.PAST, from, size);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findAllPastBookingsByOwnerId(eq(ownerId), any(), any());
    }

    @Test
    void findAllOwnersBooking_future() {
        Long ownerId = 4L;
        int from = 10;
        int size = 10;

        // Будущая бронь: start > now
        Booking future = createBookingWithOwner(5L, 6L, ownerId,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        when(bookingRepository.findAllFutureBookingsByOwnerId(anyLong(), any(), any()))
                .thenReturn(List.of(future));

        List<BookingDto> result = bookingService.findAllOwnersBooking(ownerId, BookingState.FUTURE, from, size);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findAllFutureBookingsByOwnerId(eq(ownerId), any(), any());
    }

    @Test
    void findAllOwnersBooking_waiting() {
        Long ownerId = 5L;
        int from = 0;
        int size = 5;

        Booking waiting = createBookingWithStatusAndOwner(6L, 7L, ownerId, BookingStatus.WAITING);

        when(bookingRepository.findAllWaitingBookingsByOwnerId(anyLong(), any(), any()))
                .thenReturn(List.of(waiting));

        List<BookingDto> result = bookingService.findAllOwnersBooking(ownerId, BookingState.WAITING, from, size);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findAllWaitingBookingsByOwnerId(eq(ownerId), any(), any());
    }

    @Test
    void findAllOwnersBooking_rejected() {
        Long ownerId = 6L;
        int from = 0;
        int size = 5;

        Booking rejected = createBookingWithStatusAndOwner(7L, 8L, ownerId, BookingStatus.REJECTED);

        when(bookingRepository.findAllRejectedBookingsByOwnerId(anyLong(), any()))
                .thenReturn(List.of(rejected));

        List<BookingDto> result = bookingService.findAllOwnersBooking(ownerId, BookingState.REJECTED, from, size);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findAllRejectedBookingsByOwnerId(eq(ownerId), any());
    }

    @Test
    void findAllOwnersBooking_ownerNotFound_throwsNotFound() {
        Long ownerId = 999L;

        // Возвращаем пустой Optional, чтобы сработал userIsExist -> NotFoundException
        when(userRepository.findById(eq(ownerId))).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookingService.findAllOwnersBooking(ownerId, BookingState.ALL, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    private Booking createBooking(Long id, Long bookerId, LocalDateTime start, LocalDateTime end) {
        User booker = new User();
        booker.setId(bookerId);

        Item item = new Item();
        item.setId(1L);
        item.setAvailable(true);

        Booking booking = new Booking();
        booking.setId(id);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setStatus(BookingStatus.APPROVED);
        return booking;
    }

    private Booking createBookingWithStatus(Long id, Long bookerId, BookingStatus status) {
        Booking b = createBooking(id, bookerId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));
        b.setStatus(status);
        return b;
    }

    private Booking createBookingWithOwner(Long id, Long bookerId, Long ownerId, LocalDateTime start, LocalDateTime end) {
        User booker = new User();
        booker.setId(bookerId);

        User owner = new User();
        owner.setId(ownerId);

        Item item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(id);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setStatus(BookingStatus.APPROVED);
        return booking;
    }

    private Booking createBookingWithStatusAndOwner(Long id, Long bookerId, Long ownerId, BookingStatus status) {
        Booking b = createBookingWithOwner(id, bookerId, ownerId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));
        b.setStatus(status);
        return b;
    }
}
