package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {
    UserService userService;

    @PostMapping
    public UserDto postUser(@Valid @RequestBody UserDto newUser) {
        log.info("Выполнение запроса на создание пользователя {}", newUser);
        return userService.createUser(newUser);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("Выполнение запроса на получение всех пользователей");
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        log.info("Выполнение запроса на получение пользователя с ID {}", id);
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public UserDto putUser(@PathVariable Long id, @Valid @RequestBody UserDto updateUser) {
        log.info("Выполнение запроса на изменение пользователя с ID {}", updateUser);
        return userService.updateUser(id, updateUser);
    }

    @DeleteMapping("/{id}")
    public void removeFriend(@PathVariable Long id) {
        log.info("Выполнение запроса на удаление пользователя c ID {}", id);
        userService.removeUser(id);
    }
}
