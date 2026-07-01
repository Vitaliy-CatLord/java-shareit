package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerExtraTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean
    ItemService items;

    @Test
    @DisplayName("GET /items?owner — список владельца")
    void ownerItems_ok() throws Exception {
        when(items.getOwnerItems(7L)).thenReturn(List.of());

        mvc.perform(get("/items").header(HEADER, 7))
                .andExpect(status().isOk());

        verify(items).getOwnerItems(7L);
    }

    @Test
    @DisplayName("GET /items/search?text=drill — прокидывает text")
    void search_ok() throws Exception {
        when(items.searchItems("drill")).thenReturn(List.of());

        mvc.perform(get("/items/search")
                        .header(HEADER, 1)
                        .param("text", "drill"))
                .andExpect(status().isOk());

        verify(items).searchItems("drill");
    }

    @Test
    @DisplayName("POST /items/{id}/comment — addComment")
    void addComment_ok() throws Exception {
        CommentDto resp = CommentDto.builder().id(1L).text("ok").build();
        when(items.createComment(eq(3L), any(CommentCreateDto.class), eq(10L))).thenReturn(resp);

        CommentCreateDto body = CommentCreateDto.builder().text("ok").build();

        mvc.perform(post("/items/{itemId}/comment", 10)
                        .header(HEADER, 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(items).createComment(eq(3L), any(CommentCreateDto.class), eq(10L));
    }
}