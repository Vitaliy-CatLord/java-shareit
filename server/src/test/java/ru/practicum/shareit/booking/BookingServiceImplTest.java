package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.models.Booking;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    BookingRepository bookings;
    @Mock
    UserRepository users;
    @Mock
    ItemRepository items;

    BookingServiceImp service;

    @BeforeEach
    void setUp() {
        service = new BookingServiceImp(bookings, users, items);
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id + "@ex.com");
        return u;
    }

    private static Item item(long id, long ownerId, boolean available) {
        User owner = user(ownerId);
        Item it = new Item();
        it.setId(id);
        it.setName("Item" + id);
        it.setOwner(owner);
        it.setAvailable(available);
        return it;
    }

    @Test
    void create_valid_savesWAITING_andReturnsDto() {
        long userId = 1L;
        when(users.findById(userId)).thenReturn(Optional.of(user(userId)));
        when(items.findById(10L)).thenReturn(Optional.of(item(10L, 2L, true)));

        Booking saved = new Booking();
        saved.setId(100L);
        saved.setStartDate(LocalDateTime.now().plusHours(1));
        saved.setEndDate(LocalDateTime.now().plusHours(2));
        saved.setBooker(user(userId));
        saved.setItem(item(10L, 2L, true));
        saved.setStatus(BookingStatus.WAITING);

        when(bookings.save(any(Booking.class))).thenReturn(saved);

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        BookingDto out = service.createBooking(userId, dto);

        // проверяем, что сохранили WAITING
        ArgumentCaptor<Booking> cap = ArgumentCaptor.forClass(Booking.class);
        verify(bookings).save(cap.capture());
        assertEquals(BookingStatus.WAITING, cap.getValue().getStatus());

        // и что вернулся заполненный DTO
        assertEquals(100L, out.getId());
        assertEquals(10L, out.getItem().getId());
        assertEquals("Item10", out.getItem().getName());
        assertEquals(userId, out.getBooker().getId());
        assertEquals(BookingStatus.WAITING, out.getStatus());
    }

    @Test
    void create_nullBody_throwsVE() {
        assertThrows(ValidationException.class, () -> service.createBooking(1L, null));
    }

    @Test
    void create_missingItem_throwsNFE() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(items.findById(10L)).thenReturn(Optional.empty());

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(NotFoundException.class, () -> service.createBooking(1L, dto));
    }

    @Test
    void create_itemNotAvailable_throwsVE() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(items.findById(10L)).thenReturn(Optional.of(item(10L, 2L, false)));

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(ValidationException.class, () -> service.createBooking(1L, dto));
    }

    @Test
    void create_ownerEqualsBooker_throwsVE() {
        long ownerId = 5L;
        when(users.findById(ownerId)).thenReturn(Optional.of(user(ownerId)));
        when(items.findById(10L)).thenReturn(Optional.of(item(10L, ownerId, true)));

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        assertThrows(ValidationException.class, () -> service.createBooking(ownerId, dto));
    }

    @Test
    void update_ownerApproves_waitingToApproved() {
        Booking b = new Booking();
        b.setId(1L);
        b.setItem(item(10L, 2L, true));
        b.setBooker(user(3L));
        b.setStatus(BookingStatus.WAITING);

        when(bookings.findById(1L)).thenReturn(Optional.of(b));
        when(bookings.save(any(Booking.class))).thenAnswer(invocation -> {
            //возвращаем тот же объект, что передали в save
            return invocation.getArgument(0);
        });

        BookingDto dto = service.aproveBooking(2L, 1L, true);
        assertEquals(BookingStatus.APPROVED, dto.getStatus());
    }

    @Test
    void update_wrongOwner_throwsVE() {
        Booking b = new Booking();
        b.setId(1L);
        b.setItem(item(10L, 2L, true));
        b.setBooker(user(3L));
        b.setStatus(BookingStatus.WAITING);

        when(bookings.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(ValidationException.class, () -> service.aproveBooking(99L, 1L, true));
    }

    @Test
    void update_notWaiting_throwsIllegalState() {
        Booking b = new Booking();
        b.setId(1L);
        b.setItem(item(10L, 2L, true));
        b.setBooker(user(3L));
        b.setStatus(BookingStatus.APPROVED);

        when(bookings.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(ValidationException.class, () -> service.aproveBooking(2L, 1L, false));
    }

    @Test
    void findBookingByUserId_forBooker_ok() {
        Booking b = new Booking();
        b.setId(1L);
        b.setItem(item(10L, 2L, true));
        b.setBooker(user(3L));
        b.setStatus(BookingStatus.WAITING);
        when(bookings.findById(1L)).thenReturn(Optional.of(b));

        BookingDto dto = service.findBookingByUserId(3L, 1L);
        assertEquals(1L, dto.getId());
    }
}