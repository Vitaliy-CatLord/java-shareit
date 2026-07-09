package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.models.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private UserRepository repo;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(UserRepository.class);
        service = new UserServiceImpl(repo);
    }

    private static User user(long id, String name, String email) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        return u;
    }

    @Test
    void create_ok() {
        when(repo.findUserByEmail("a@ex.com")).thenReturn(null);
        when(repo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserDto out = service.create(UserDto.builder().name("Ann").email("a@ex.com").build());
        assertEquals(1L, out.getId());
        assertEquals("Ann", out.getName());
    }

    @Test
    void create_blankEmail_throwsVE() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.create(UserDto.builder().name("Ann").email(" ").build()));
        assertTrue(ex.getMessage().contains("Имейл нового пользователя не может быть пустым"));
    }


    @Test
    void create_duplicateEmail_throwsConflict() {
        when(repo.findUserByEmail("a@ex.com")).thenReturn(user(6L, "J", "a@ex.com"));

        assertThrows(ConflictException.class,
                () -> service.create(UserDto.builder().name("Ann").email("a@ex.com").build()));
    }

    @Test
    void update_changesOnlyProvidedFields_andChecksUniqueEmail() {
        when(repo.findById(1L)).thenReturn(Optional.of(user(1L, "Old", "old@ex.com")));
        when(repo.findUserByEmail("new@ex.com")).thenReturn(user(6L, "J", "new@ex.com"));

        // email занят другим — Conflict
        assertThrows(ConflictException.class,
                () -> service.update(1L, UserDto.builder().email("new@ex.com").build()));

        // а вот изменение имени без email — ок
        when(repo.findById(1L)).thenReturn(Optional.of(user(1L, "Old", "old@ex.com")));
        when(repo.save(any(User.class))).thenReturn(user(1L, "New", "old@ex.com"));
        UserDto out = service.update(1L, UserDto.builder().name("New").build());
        assertEquals("New", out.getName());
        assertEquals("old@ex.com", out.getEmail());
    }

    @Test
    void getById_ok_and_notFound() {
        when(repo.findById(5L)).thenReturn(Optional.of(user(5L, "U5", "u5@ex.com")));
        assertEquals(5L, service.findById(5L).getId());

        when(repo.findById(6L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.findById(6L));
    }

    @Test
    void getAll_maps() {
        when(repo.findAll()).thenReturn(List.of(user(1L, "A", "a@ex.com"), user(2L, "B", "b@ex.com")));
        assertEquals(2, service.findAll().size());
    }

    @Test
    void delete_checksExists() {
        when(repo.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repo).deleteById(1L);

        when(repo.existsById(2L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.delete(2L));
    }
}