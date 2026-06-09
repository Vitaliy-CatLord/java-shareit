package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOut;
import ru.practicum.shareit.item.service.ItemServiceImp;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemServiceImp itemService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDtoOut postItem(@RequestHeader(USER_HEADER) Long userId, @Valid @RequestBody ItemDto newItem) {
        log.info("Выполнение запроса на создание предмета {}", newItem);
        return itemService.createItem(userId, newItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoOut putItem(@RequestHeader(USER_HEADER) Long userId,
                              @PathVariable Long itemId,
                              @Valid @RequestBody ItemDto updateItem) {
        log.info("Выполнение запроса на изменение вещи с ID {} юзером с ID {}", itemId, userId);
        return itemService.updateItem(userId, itemId, updateItem);
    }

    @GetMapping("/{itemId}")
    public ItemDtoOut getItemById(@RequestHeader(USER_HEADER) Long userId,
                                  @PathVariable Long itemId) {
        log.info("Выполнение запроса на получение вещи с ID {}", itemId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoOut> getOwnerItems(@RequestHeader(USER_HEADER) Long userId) {
        log.info("Выполнение запроса на получение всех вещей пользователя {}", userId);
        return itemService.getOwnerItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDtoOut> searchItem(@RequestParam String text) {
        log.info("Выполнение запроса на поиск вещи {}", text);
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoOut createComment(@RequestHeader(USER_HEADER) Long userId,
                                       @Validated @RequestBody CommentDto commentDto,
                                       @PathVariable Long itemId) {
        log.info("POST Запрос на создание комментария id = {}", itemId);
        return itemService.createComment(userId, commentDto, itemId);
    }

    @DeleteMapping("/{id}")
    public void removeItem(@PathVariable Long id) {
        log.info("Выполнение запроса на удаление вещи c ID {}", id);
        itemService.removeItem(id);
    }
}
