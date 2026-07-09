package ru.practicum.shareit.item.mappers;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.models.Comment;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) return null;
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

//    public static CommentDtoOut toCommentDtoOut(Comment comment) {
//        CommentDtoOut out = new CommentDtoOut();
//        out.setId(comment.getId());
//        out.setText(comment.getText());
//        out.setAuthorName(comment.getAuthor().getName());
//        out.setCreated(comment.getCreated());
//        out.setItemId(comment.getItem().getId());
//        return out;
//    }
//
//    public static Comment toComment(CommentDto dto, Item item, User user) {
//        Comment comment = new Comment();
//        comment.setText(dto.getText());
//        comment.setItem(item);
//        comment.setAuthor(user);
//        return comment;
//    }
}
