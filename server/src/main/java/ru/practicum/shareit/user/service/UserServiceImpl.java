package ru.practicum.shareit.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.models.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
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
        User old = userRepository.findUserByEmail(dto.getEmail());
        if (old != null) {
            throw new ConflictException("Почта " + dto.getEmail() + " занята");
        }

        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(dto)));
    }

    @Override
    public UserDto update(Long userId, UserDto dto) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с " + userId + " не существует"));
        if (dto.getEmail() != null) {
            validateEmailRequired(dto.getEmail());
            if (!dto.getEmail().equalsIgnoreCase(u.getEmail())) {
                validateEmailUnique(dto.getEmail(), userId);
                u.setEmail(dto.getEmail());
            }
        }
        if (dto.getName() != null) {
            u.setName(dto.getName());
        }
        return UserMapper.toUserDto(userRepository.save(u));

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
        if(!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь для удаления не найден");
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    private void validateEmailRequired(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Почта не должна быть нулевой или пустой");
        }
        if (!email.contains("@")) {
            throw new ValidationException("Почта должна содержать '@'");
        }
    }

    private void validateEmailUnique(String email, Long selfId) {
        User old = userRepository.findUserByEmail(email);
            if (old != null && !Objects.equals(old.getId(), selfId)) {
                throw new ConflictException("Почта " + email + " занята");
            }
    }
}
