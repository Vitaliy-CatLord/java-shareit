package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.models.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplNegativeTest {

    private ItemRepository items;
    private UserRepository users;
    private CommentRepository comments;
    private BookingRepository bookings;
    private ItemRequestRepository itemRequests;

    private ItemServiceImpl service;

    @BeforeEach
    void setUp() {
        items = mock(ItemRepository.class);
        users = mock(UserRepository.class);
        comments = mock(CommentRepository.class);
        bookings = mock(BookingRepository.class);
        itemRequests = mock(ItemRequestRepository.class);
        service = new ItemServiceImpl(comments, bookings, items, users, itemRequests);
    }

    // ---------- creating test ----------

    @Test
    void create_nullBody_throwsVE() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.createItem(1L, null));
        assertTrue(ex.getMessage().contains("Предмет должен быть не нулевым"));
    }

    @Test
    void create_blankName_throwsVE() {
        ItemDto dto = ItemDto.builder()
                .name("  ")
                .description("d")
                .available(true)
                .build();
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.createItem(1L, dto));
        assertTrue(ex.getMessage().contains("У предмета должно быть название"));
    }

    @Test
    void create_blankDescription_throwsVE() {
        ItemDto dto = ItemDto.builder()
                .name("N")
                .description(" ")
                .available(true)
                .build();
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.createItem(1L, dto));
        assertTrue(ex.getMessage().contains("У предмета должно быть описание"));
    }

    @Test
    void create_availableNull_throwsVE() {
        ItemDto dto = ItemDto.builder()
                .name("N")
                .description("d")
                .available(null)
                .build();
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.createItem(1L, dto));
        assertTrue(ex.getMessage().contains("У предмета должна быть обозначена Доступность"));
    }

    @Test
    void create_ownerNotFound_throwsNFE() {
        when(users.findById(9L)).thenReturn(Optional.empty());

        ItemDto dto = ItemDto.builder()
                .name("N")
                .description("d")
                .available(true)
                .build();

        assertThrows(NotFoundException.class, () -> service.createItem(9L, dto));
    }

    @Test
    void create_requestNotFound_throwsNFE() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(itemRequests.findById(100L)).thenReturn(Optional.empty());

        ItemDto dto = ItemDto.builder()
                .name("N")
                .description("d")
                .available(true)
                .requestId(100L)
                .build();

        assertThrows(NotFoundException.class, () -> service.createItem(1L, dto));
    }

    // ---------- updating test ----------

    @Test
    void update_itemNotFound_throwsNFE() {
        assertThrows(NotFoundException.class,
                () -> service.updateItem(1L, 5L, ItemDto.builder().build()));
    }

    @Test
    void update_notOwner_throwsNFE() {
        User owner = user(10L);
        Item item = new Item();
        item.setId(7L);
        item.setOwner(owner);

        assertThrows(NotFoundException.class,
                () -> service.updateItem(99L, 7L, ItemDto.builder().name("x").build()));
        verify(items, never()).save(any());
    }

    @Test
    void update_requestNotFound_throwsNFE() {
        User owner = user(1L);
        Item item = new Item();
        item.setId(7L);
        item.setOwner(owner);

        ItemDto patch = ItemDto.builder().requestId(500L).build();
        assertThrows(NotFoundException.class,
                () -> service.updateItem(1L, 7L, patch));
    }

    // ---------- search ----------

    @Test
    void search_blank_returnsEmptyList() {
        assertTrue(service.searchItems("   ").isEmpty());
        verify(items, never()).search(any());
    }

    // ---------- addComment ----------

    @Test
    void addComment_blankText_throwsIAE() {
        assertThrows(ValidationException.class,
                () -> service.createComment(1L, CommentCreateDto.builder().text(" ").build(), 2L));
    }

    @Test
    void addComment_itemNotFound_throwsNFE() {
        User u = user(1L);
        when(users.findById(1L)).thenReturn(Optional.of(u));
        when(items.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.createComment(1L, CommentCreateDto.builder().text("ok").build(), 2L));
    }

    @Test
    void addComment_userNotFound_throwsNFE() {
        Item it = new Item();
        it.setId(2L);
        when(users.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.createComment(1L, CommentCreateDto.builder().text("ok").build(), 2L));
    }

    @Test
    void addComment_noCompletedBooking_throwsVE() {
        Item it = new Item();
        it.setId(2L);
        it.setOwner(user(10L));
        when(items.findById(2L)).thenReturn(Optional.of(it));
        when(users.findById(1L)).thenReturn(Optional.of(user(1L)));

        assertThrows(ValidationException.class,
                () -> service.createComment(1L, CommentCreateDto.builder().text("ok").build(), 2L));
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("N" + id);
        u.setEmail("E" + id + "@ex.com");
        return u;
    }
}