package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private static final String USER_ID = "/{id}";

    @PostMapping
    public UserDto createUser(@RequestBody UserDto newUser) {
        log.info("Выполнение запроса на создание пользователя {}", newUser);
        return userService.create(newUser);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Выполнение запроса на получение всех пользователей");
        return userService.findAll();
    }

    @GetMapping(USER_ID)
    public UserDto getUserById(@PathVariable Long id) {
        log.info("Выполнение запроса на получение пользователя с ID {}", id);
        return userService.findById(id);
    }

    @PatchMapping(USER_ID)
    public UserDto putUser(@PathVariable Long id, @RequestBody UserDto updateUser) {
        log.info("Выполнение запроса на изменение пользователя {} с ID {}", updateUser, id);
        return userService.update(id, updateUser);
    }

    @DeleteMapping(USER_ID)
    public void removeFriend(@PathVariable Long id) {
        log.info("Выполнение запроса на удаление пользователя c ID {}", id);
        userService.delete(id);
    }
}
