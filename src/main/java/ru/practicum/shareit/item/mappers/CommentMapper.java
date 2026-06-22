package ru.practicum.shareit.item.mappers;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoOut;
import ru.practicum.shareit.item.models.Comment;
import ru.practicum.shareit.item.models.Item;
import ru.practicum.shareit.user.models.User;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setText(comment.getText());
        return dto;
    }

    public static CommentDtoOut toCommentDtoOut(Comment comment) {
        CommentDtoOut out = new CommentDtoOut();
        out.setId(comment.getId());
        out.setText(comment.getText());
        out.setAuthorName(comment.getAuthor().getName());
        out.setCreated(comment.getCreated());
        out.setItemId(comment.getItem().getId());
        return out;
    }

    public static Comment toComment(CommentDto dto, Item item, User user) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        return comment;
    }
}
