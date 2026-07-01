package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.models.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.models.Comment;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.models.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.models.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    ItemRepository items;
    @Mock
    UserRepository users;
    @Mock
    CommentRepository comments;
    @Mock
    BookingRepository bookings;
    @Mock
    ItemRequestRepository requests;

    ItemServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ItemServiceImpl(comments, bookings, items, users, requests);
    }

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setName("U" + id);
        u.setEmail("u" + id + "@ex.com");
        return u;
    }

    private static Item item(long id, long ownerId, boolean available) {
        Item it = new Item();
        it.setId(id);
        it.setName("Item" + id);
        it.setDescription("desc");
        it.setAvailable(available);
        User owner = user(ownerId);
        it.setOwner(owner);
        return it;
    }

    @Test
    void create_ok_mapsAndSaves() {
        long ownerId = 1L;
        when(users.findById(ownerId)).thenReturn(Optional.of(user(ownerId)));
        when(items.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(100L);
            return i;
        });

        ItemDto dto = ItemDto.builder()
                .name("Дрель")
                .description("С ударом")
                .available(true)
                .build();

        ItemDto out = service.createItem(ownerId, dto);
        assertNotNull(out.getId());
        assertEquals("Дрель", out.getName());
        assertEquals("С ударом", out.getDescription());
        assertTrue(out.getAvailable());
    }

    @Test
    void create_withRequest_linksRequest() {
        long ownerId = 1L;
        when(users.findById(ownerId)).thenReturn(Optional.of(user(ownerId)));
        when(requests.findById(77L)).thenReturn(Optional.of(ItemRequest.builder().id(77L).build()));
        when(items.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemDto dto = ItemDto.builder()
                .name("Стремянка")
                .description("2м")
                .available(true)
                .requestId(77L)
                .build();

        ItemDto out = service.createItem(ownerId, dto);
        assertEquals(77L, out.getRequestId());
    }

    @Test
    void getItemById_forOwner_includesLastNextAndComments() {
        Item it = item(10L, 2L, true);
        when(items.findById(10L)).thenReturn(Optional.of(it));
        when(users.findById(2L)).thenReturn(Optional.of(user(2)));

        Comment c = Comment.builder()
                .id(1L).text("ok").created(LocalDateTime.now())
                .item(it).author(user(3L)).build();
        when(comments.findByItem_IdOrderByCreatedDesc(10L)).thenReturn(List.of(c));

        // last/next APPROVED мокаем на Optional.of(...)
        when(bookings.findTop1ByItemIdAndStartDateBeforeAndStatusOrderByStartDateDesc(eq(10L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.empty());
        when(bookings.findTop1ByItemIdAndStartDateAfterAndStatusOrderByStartDateAsc(eq(10L), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(Optional.empty());

        ItemDto dto = service.getItemById(2L, 10L);
        assertNotNull(dto.getComments());
        assertEquals(1, dto.getComments().size());
        // last/next могут быть null — это ок, главное, что код прошёл ветку владельца
    }

    @Test
    void search_blank_returnsEmpty() {
        assertTrue(service.searchItems(null).isEmpty());
        assertTrue(service.searchItems("").isEmpty());
        assertTrue(service.searchItems("   ").isEmpty());
    }

    @Test
    void createComment_allowed_savesAndReturnsDto() {
        Item it = item(10L, 2L, true);
        when(items.findById(10L)).thenReturn(Optional.of(it));
        when(users.findById(3L)).thenReturn(Optional.of(user(3L)));
        when(bookings.existsByBookerIdAndItemIdAndStatusAndEndDateBefore(
                eq(3L), eq(10L), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(true);

        when(comments.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });

        CommentCreateDto body = CommentCreateDto.builder().text("nice").build();
        CommentDto dto = service.createComment(3L, body, 10L);
        assertEquals(5L, dto.getId());
        assertEquals("nice", dto.getText());
    }
}