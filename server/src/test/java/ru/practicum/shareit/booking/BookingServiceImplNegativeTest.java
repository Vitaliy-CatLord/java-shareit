package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.models.Booking;
import ru.practicum.shareit.booking.models.BookingState;
import ru.practicum.shareit.booking.models.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImp;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.models.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplNegativeTest {

    private BookingRepository bookings;
    private UserRepository users;
    private ItemRepository items;

    private BookingServiceImp service;

    @BeforeEach
    void setUp() {
        bookings = mock(BookingRepository.class);
        users = mock(UserRepository.class);
        items = mock(ItemRepository.class);
        service = new BookingServiceImp(bookings, users, items);
    }

    // ---------- creating test ----------

    @Test
    void create_nullBody_throwsVE() {
        assertThrows(ValidationException.class, () -> service.createBooking(1L, null));
    }

    @Test
    void create_itemIdNull_throwsVE() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();
        assertThrows(ValidationException.class, () -> service.createBooking(1L, dto));
    }

    @Test
    void create_startAfterEnd_throwsVE() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusHours(3))
                .end(LocalDateTime.now().plusHours(1))
                .build();
        assertThrows(ValidationException.class, () -> service.createBooking(1L, dto));
    }

    @Test
    void create_userNotFound_throwsNFE() {
        when(users.findById(1L)).thenReturn(Optional.empty());

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(NotFoundException.class, () -> service.createBooking(1L, dto));
    }

    @Test
    void create_itemNotFound_throwsNFE() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(items.findById(2L)).thenReturn(Optional.empty());

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(NotFoundException.class, () -> service.createBooking(1L, dto));
    }

    @Test
    void create_itemNotAvailable_throwsVE() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        Item it = new Item();
        it.setId(2L);
        it.setOwner(user(99L));
        it.setAvailable(false);
        when(items.findById(2L)).thenReturn(Optional.of(it));

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(ValidationException.class, () -> service.createBooking(1L, dto));
    }

    @Test
    void create_ownerBooksOwnItem_throwsVE() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        Item it = new Item();
        it.setId(2L);
        it.setOwner(user(1L));
        it.setAvailable(true);
        when(items.findById(2L)).thenReturn(Optional.of(it));

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(ValidationException.class, () -> service.createBooking(1L, dto));
    }

    // ---------- approve booking ----------

    @Test
    void aproveBookingStatus_bookingNotFound_throwsNFE() {
        when(bookings.findById(9L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.aproveBooking(1L, 9L, true));
    }

    @Test
    void aproveBooking_notOwner_throwsVE() {
        Booking b = new Booking();
        Item it = new Item();
        it.setOwner(user(10L));
        b.setItem(it);
        b.setStatus(BookingStatus.WAITING);
        when(bookings.findById(7L)).thenReturn(Optional.of(b));

        assertThrows(ValidationException.class, () -> service.aproveBooking(99L, 7L, true));
    }

    @Test
    void aproveBooking_notWaiting_throwsIllegalState() {
        Booking b = new Booking();
        Item it = new Item();
        it.setOwner(user(1L));
        b.setItem(it);
        b.setStatus(BookingStatus.APPROVED);
        when(bookings.findById(7L)).thenReturn(Optional.of(b));

        assertThrows(ValidationException.class, () -> service.aproveBooking(1L, 7L, true));
    }

    // ---------- getById ----------

    @Test
    void findBookingByUserId_bookingNotFound_throwsNFE() {
        when(bookings.findById(5L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.findBookingByUserId(1L, 5L));
    }

    @Test
    void findBookingByUserId_notBookerAndNotOwner_throwsVE() {
        Booking b = new Booking();
        b.setBooker(user(2L));
        Item it = new Item();
        it.setOwner(user(3L));
        b.setItem(it);
        when(bookings.findById(5L)).thenReturn(Optional.of(b));

        assertThrows(ValidationException.class, () -> service.findBookingByUserId(99L, 5L));
    }

    // ---------- findAllByState ----------

    @Test
    void findAllByState_userNotExists_throwsNFE() {
        assertThrows(NotFoundException.class,
                () -> service.findAllByState(123L, BookingState.ALL, 0, 10));
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id + "@ex.com");
        return u;
    }
}