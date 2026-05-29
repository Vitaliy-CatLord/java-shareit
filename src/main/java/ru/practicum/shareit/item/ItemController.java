package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    ItemService itemService;

    @PostMapping
    public ItemDto postItem(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody ItemDto newItem) {
        log.info("Выполнение запроса на создание предмета {}", newItem);
        return itemService.createItem(userId, newItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto putItem(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable Long itemId, @Valid @RequestBody ItemDto updateItem) {
        log.info("Выполнение запроса на изменение вещи с ID {} юзером с ID {}", updateItem, userId);
        return itemService.updateItem(userId, itemId, updateItem);
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@PathVariable Long id) {
        log.info("Выполнение запроса на получение вещи с ID {}", id);
        return itemService.getItemById(id);
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Выполнение запроса на получение всех вещей пользователя {}", userId);
        return itemService.getOwnerItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam @NotNull @NotBlank String text) {
        log.info("Выполнение запроса на поиск вещи {}", text);
        return itemService.searchItems(text);
    }

    @DeleteMapping("/{id}")
    public void removeItem(@PathVariable Long id) {
        log.info("Выполнение запроса на удаление вещи c ID {}", id);
        itemService.removeItem(id);
    }
}
