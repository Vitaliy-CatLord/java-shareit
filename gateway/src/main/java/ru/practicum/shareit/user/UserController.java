package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserClient client;
    private static final String USER_ID = "/{userId}";

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto dto) {
        return client.create(dto);
    }

    @PatchMapping(USER_ID)
    public ResponseEntity<Object> update(@PathVariable long userId,
                                         @RequestBody UserDto dto) {
        return client.update(userId, dto);
    }

    @GetMapping(USER_ID)
    public ResponseEntity<Object> getById(@PathVariable long userId) {
        return client.getById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return client.getAll();
    }

    @DeleteMapping(USER_ID)
    public ResponseEntity<Object> delete(@PathVariable long userId) {
        return client.delete(userId);
    }
}
