package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.Exceptions.ConflictExeption;
import ru.practicum.shareit.Exceptions.DuplicatedDataException;
import ru.practicum.shareit.Exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;

    public User createUser(UserDto dto) {
        if (dto.getName().isEmpty() || dto.getName().isBlank()) {
            throw new ValidationException("Имя нового пользователя не может быть пустым");
        }
        if (dto.getEmail().isEmpty() || dto.getEmail().isBlank()) {
            throw new ValidationException("Имейл нового пользователя не может быть пустым");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicatedDataException("Пользователь с этим имейлом уже существует");
        }
        return userRepository.save(UserMapper.toUser(dto));
    }

    public List<User> getAll() {
        return userRepository.findAll().stream()
                .toList();
    }

    public User getUserById(long userId) {
        return userRepository.findById(userId);
    }

    public User updateUser(long userId, UserDto dto) {
        User user = UserMapper.toUser(dto);
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictExeption("Нельзя изменить почту существующему пользователю");
        }
        return userRepository.update(userId, user);
    }

    public void removeUser(long userId) {
        userRepository.deleteById(userId);
    }
}
