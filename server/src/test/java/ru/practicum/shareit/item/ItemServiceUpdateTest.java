package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.models.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.models.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ItemServiceUpdateTest {

    @Autowired
    private ItemService itemService;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ItemRequestRepository requestRepository;

    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 2L;
    private static final Long REQUEST_ID = 3L;

    @BeforeEach
    void setUp() {
        // Гарантируем существование пользователя при проверке владельца
        User owner = new User();
        owner.setId(USER_ID);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(owner));
    }

    @Test
    void updateItem_success_fullUpdate() {
        Item existingItem = new Item();
        existingItem.setId(ITEM_ID);
        existingItem.setName("Old Name");
        existingItem.setDescription("Old Desc");
        existingItem.setAvailable(false);
        User owner = new User();
        owner.setId(USER_ID);
        existingItem.setOwner(owner);

        when(itemRepository.findById(eq(ITEM_ID))).thenReturn(Optional.of(existingItem));

        ItemDto dto = new ItemDto();
        dto.setName("New Name");
        dto.setDescription("New Desc");
        dto.setAvailable(true);

        ItemDto result = itemService.updateItem(USER_ID, ITEM_ID, dto);

        assertThat(result.getId()).isEqualTo(ITEM_ID);
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("New Desc");
        assertThat(result.getAvailable()).isTrue();

        verify(itemRepository).save(eq(existingItem));
    }

    @Test
    void updateItem_partialUpdate_nullFieldsNotOverwritten() {
        Item existingItem = new Item();
        existingItem.setId(ITEM_ID);
        existingItem.setName("Keep Name");
        existingItem.setDescription("Keep Desc");
        existingItem.setAvailable(true);
        User owner = new User();
        owner.setId(USER_ID);
        existingItem.setOwner(owner);

        when(itemRepository.findById(eq(ITEM_ID))).thenReturn(Optional.of(existingItem));

        ItemDto dto = new ItemDto();
        // Обновляем только available; name и description — null
        dto.setAvailable(false);

        ItemDto result = itemService.updateItem(USER_ID, ITEM_ID, dto);

        assertThat(result.getName()).isEqualTo("Keep Name");          // не перезаписываем null
        assertThat(result.getDescription()).isEqualTo("Keep Desc");  // не перезаписываем blank/null
        assertThat(result.getAvailable()).isFalse();                 // обновили

        verify(itemRepository).save(eq(existingItem));
    }

    @Test
    void updateItem_updateWithRequestId() {
        ItemRequest request = new ItemRequest();
        request.setId(REQUEST_ID);

        Item existingItem = new Item();
        existingItem.setId(ITEM_ID);
        User owner = new User();
        owner.setId(USER_ID);
        existingItem.setOwner(owner);

        when(requestRepository.findById(eq(REQUEST_ID))).thenReturn(Optional.of(request));
        when(itemRepository.findById(eq(ITEM_ID))).thenReturn(Optional.of(existingItem));

        ItemDto dto = new ItemDto();
        dto.setRequestId(REQUEST_ID);

        itemService.updateItem(USER_ID, ITEM_ID, dto);

        // Проверяем, что у предмета установлен request
        assertThat(existingItem.getRequest()).isEqualTo(request);
        verify(requestRepository).findById(eq(REQUEST_ID));
        verify(itemRepository).save(eq(existingItem));
    }

    @Test
    void updateItem_ownerCheck_success() {
        User owner = new User();
        owner.setId(USER_ID);

        Item existingItem = new Item();
        existingItem.setId(ITEM_ID);
        existingItem.setOwner(owner);

        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(owner));
        when(itemRepository.findById(eq(ITEM_ID))).thenReturn(Optional.of(existingItem));

        ItemDto dto = new ItemDto();
        dto.setName("Updated");

        itemService.updateItem(USER_ID, ITEM_ID, dto);

        verify(itemRepository).save(any());
    }

    @Test
    void updateItem_notOwner_throwsNotFound() {
        Long anotherUserId = 999L;
        User anotherUser = new User();
        anotherUser.setId(anotherUserId);

        User realOwner = new User();
        realOwner.setId(USER_ID);

        Item existingItem = new Item();
        existingItem.setId(ITEM_ID);
        existingItem.setOwner(realOwner);

        when(userRepository.findById(eq(anotherUserId))).thenReturn(Optional.of(anotherUser));
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(realOwner));
        when(itemRepository.findById(eq(ITEM_ID))).thenReturn(Optional.of(existingItem));

        ItemDto dto = new ItemDto();
        dto.setName("Try Update");

        assertThatThrownBy(() -> itemService.updateItem(anotherUserId, ITEM_ID, dto))
                .isInstanceOf(ru.practicum.shareit.exceptions.NotFoundException.class)
                .hasMessageContaining("не является владельцем вещи");

        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_userNotFound_throwsNotFound() {
        Long nonExistingUserId = 999L;
        when(userRepository.findById(eq(nonExistingUserId))).thenReturn(Optional.empty());

        ItemDto dto = new ItemDto();
        dto.setName("Update");

        assertThatThrownBy(() -> itemService.updateItem(nonExistingUserId, ITEM_ID, dto))
                .isInstanceOf(ru.practicum.shareit.exceptions.NotFoundException.class)
                .hasMessageContaining("Пользователя с ID");

        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void updateItem_itemNotFound_throwsNotFound() {
        User owner = new User();
        owner.setId(USER_ID);
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(owner));
        when(itemRepository.findById(eq(ITEM_ID))).thenReturn(Optional.empty());

        ItemDto dto = new ItemDto();
        dto.setName("Update");

        assertThatThrownBy(() -> itemService.updateItem(USER_ID, ITEM_ID, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(String.format("Предмет с ID %s не найден", ITEM_ID));
    }


}

