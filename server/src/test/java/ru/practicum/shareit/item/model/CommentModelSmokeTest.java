package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.models.Comment;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.user.models.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommentModelSmokeTest {

    @Test
    void touch_Comment_entity_buildersAndGetters() {
        User u = new User();
        u.setId(1L);

        Item it = new Item();
        it.setId(2L);

        LocalDateTime now = LocalDateTime.now();

        Comment c = Comment.builder()
                .id(10L)
                .text("ok")
                .created(LocalDateTime.of(2025, 1, 2, 3, 4, 5))
                .author(u)
                .item(it)
                .build();

        assertEquals(10L, c.getId());
        assertEquals("ok", c.getText());
        assertEquals(LocalDateTime.of(2025, 1, 2, 3, 4, 5), c.getCreated());
        assertEquals(1L, c.getAuthor().getId());
        assertEquals(2L, c.getItem().getId());
    }
}