package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.models.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.ErrorHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerMoreWebTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Test
    void create_validDates_ok() throws Exception {
        BookingCreateDto body = BookingCreateDto.builder().itemId(1L).build();
        when(bookingService.createBooking(eq(2L), any())).thenReturn(BookingDto.builder().id(10L).build());

        mvc.perform(post("/bookings")
                        .header(HEADER, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(bookingService).createBooking(eq(2L), any());
    }

    @Test
    void getBooker_unknownState_400() throws Exception {
        mvc.perform(get("/bookings")
                        .header(HEADER, 1)
                        .param("state", "WTF:)"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwner_parsesState_ok() throws Exception {
        when(bookingService.findAllOwnersBooking(3L, BookingState.PAST, 0, 10)).thenReturn(java.util.List.of());

        mvc.perform(get("/bookings/owner")
                        .header(HEADER, 3)
                        .param("state", "past"))
                .andExpect(status().isOk());

        verify(bookingService).findAllOwnersBooking(3L, BookingState.PAST, 0, 10);
    }
}