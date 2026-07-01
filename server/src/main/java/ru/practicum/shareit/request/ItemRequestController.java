package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.MissingUserHeaderException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;


@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService service;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto create(@RequestHeader(value = USER_HEADER, required = false) Long userId,
                                 @RequestBody ItemRequestCreateDto request) {
        if (userId == null) {
            throw new MissingUserHeaderException("Заголовок " + USER_HEADER + " обязателен");
        }
        log.info("Выполнение запроса на создание реквеста {}", request);
        return service.createRequest(userId, request);
    }

    @GetMapping
    public List<ItemRequestDto> userRequest(@RequestHeader(value = USER_HEADER, required = false) Long userId) {
        if (userId == null) {
            throw new MissingUserHeaderException("Заголовок " + USER_HEADER + " обязателен");
        }
        log.info("Выполнение запроса на получение всех реквестов пользователя {}", userId);
        return service.getUserRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> allRequests(@RequestHeader(value = USER_HEADER, required = false) Long userId,
                                            @RequestParam(defaultValue = "0") int from,
                                            @RequestParam(defaultValue = "10") int size) {
        if (userId == null) {
            throw new MissingUserHeaderException("Заголовок " + USER_HEADER + " обязателен");
        }
        log.info("Выполнение запроса на получение всех реквестов от юзера {}", userId);
        return service.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader(value = USER_HEADER, required = false) Long userId,
                                  @PathVariable Long requestId) {
        if (userId == null) {
            throw new MissingUserHeaderException("Заголовок " + USER_HEADER + " обязателен");
        }
        log.info("Выполнение запроса на получение реквеста {}", requestId);
        return service.getById(userId, requestId);
    }

}
