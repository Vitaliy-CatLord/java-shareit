package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public Item postItem(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody ItemDto newItem) {
        log.info("Выполнение запроса на создание предмета {}", newItem);
        return itemService.createItem(userId, newItem);
    }

    @PatchMapping("/{itemId}")
    public Item putItem(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable Long itemId, @Valid @RequestBody ItemDto updateItem) {
        log.info("Выполнение запроса на изменение вещи с ID {} юзером с ID {}", itemId, userId);
        return itemService.updateItem(userId, itemId, updateItem);
    }

    @GetMapping("/{id}")
    public Item getItemById(@PathVariable Long id) {
        log.info("Выполнение запроса на получение вещи с ID {}", id);
        return itemService.getItemById(id);
    }

    @GetMapping
    public List<Item> getOwnerItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Выполнение запроса на получение всех вещей пользователя {}", userId);
        return itemService.getOwnerItems(userId);
    }

    @GetMapping("/search")
    public List<Item> searchItem(@RequestParam String text) {
        log.info("Выполнение запроса на поиск вещи {}", text);
        return itemService.searchItems(text);
    }

    @DeleteMapping("/{id}")
    public void removeItem(@PathVariable Long id) {
        log.info("Выполнение запроса на удаление вещи c ID {}", id);
        itemService.removeItem(id);
    }
}
