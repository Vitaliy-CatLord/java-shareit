package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.models.Comment;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.user.models.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommentEntityTouchTest {

    @Test
    void touch_Comment_entity() {
        User author = new User();
        author.setId(1L);

        Item item = new Item();
        item.setId(2L);

        LocalDateTime now = LocalDateTime.now();

        Comment c = new Comment();
        c.setId(10L);
        c.setText("ok");
        c.setCreated(LocalDateTime.of(2025, 1, 2, 3, 4, 5));
        c.setAuthor(author);
        c.setItem(item);

        assertEquals(10L, c.getId());
        assertEquals("ok", c.getText());
        assertEquals(LocalDateTime.of(2025, 1, 2, 3, 4, 5), c.getCreated());
        assertEquals(1L, c.getAuthor().getId());
        assertEquals(2L, c.getItem().getId());
    }
}