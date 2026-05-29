package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;

    public UserDto createUser (UserDto dto) {
        User user = userRepository.save(UserMapper.toUser(dto));
        return UserMapper.toUserDto(user);
    }

    public List<UserDto> getAll () {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public UserDto getUserById(long userId) {
        return UserMapper.toUserDto(userRepository.findById(userId));
    }

    public UserDto updateUser(long userId, UserDto dto) {
        User user = UserMapper.toUser(dto);
        userRepository.update(userId, user);
        return dto;

    }

    public void removeUser(long userId) {
        userRepository.deleteById(userId);
    }
}
