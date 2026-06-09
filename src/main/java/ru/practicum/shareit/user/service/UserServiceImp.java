package ru.practicum.shareit.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.Exceptions.ConflictException;
import ru.practicum.shareit.Exceptions.NotFoundException;
import ru.practicum.shareit.Exceptions.ValidationException;
import ru.practicum.shareit.user.models.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImp implements UserService {
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        if (dto.getName().isEmpty() || dto.getName().isBlank()) {
            throw new ValidationException("Имя нового пользователя не может быть пустым");
        }
        if (dto.getEmail().isEmpty() || dto.getEmail().isBlank()) {
            throw new ValidationException("Имейл нового пользователя не может быть пустым");
        }

        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(dto)));
    }

    @Override
    public UserDto update(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователя с " + id + " не существует"));
        String name = dto.getName();
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        String email = dto.getEmail();
        User old = userRepository.findUserByEmail(email);
        if (old != null) {
            throw new ConflictException("Почта " + email + " занята");
        }
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователя с " + id + " не существует")
                );
        return UserMapper.toUserDto(user);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

}
