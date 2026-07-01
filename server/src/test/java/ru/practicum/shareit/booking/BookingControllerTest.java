package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.models.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;

    @MockBean
    BookingService bookings;

    @Test
    @DisplayName("POST /bookings — создаёт и возвращает DTO")
    void create_returnsDto() throws Exception {
        BookingDto resp = BookingDto.builder().id(1L).build();
        when(bookings.createBooking(eq(5L), any(BookingCreateDto.class))).thenReturn(resp);

        String body = "{\"iRjhtemId\":10,\"start\":\"2030-01-01T10:00:00\",\"end\":\"2030-01-01T12:00:00\"}";

        mvc.perform(post("/bookings")
                        .header(HEADER, 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookings).createBooking(eq(5L), any(BookingCreateDto.class));
    }

    @Test
    @DisplayName("PATCH /bookings/{id}?approved=... — прокидывает флаг approved")
    void approve_ok() throws Exception {
        when(bookings.aproveBooking(2L, 99L, true)).thenReturn(BookingDto.builder().id(99L).build());

        mvc.perform(patch("/bookings/{id}", 99)
                        .header(HEADER, 2)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));

        verify(bookings).aproveBooking(2L, 99L, true);
    }

    @Test
    @DisplayName("GET /bookings — state парсится в BookingState.from(...)")
    void myBookings_parsesState() throws Exception {
        when(bookings.findAllByState(7L, BookingState.CURRENT, 0, 10)).thenReturn(List.of());

        mvc.perform(get("/bookings")
                        .header(HEADER, 7)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bookings).findAllByState(7L, BookingState.CURRENT, 0, 10);
    }

    @Test
    @DisplayName("GET /bookings/owner — state парсится в BookingState.from(...)")
    void ownerBookings_parsesState() throws Exception {
        when(bookings.findAllOwnersBooking(3L, BookingState.WAITING, 0, 10)).thenReturn(List.of());

        mvc.perform(get("/bookings/owner")
                        .header(HEADER, 3)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bookings).findAllOwnersBooking(3L, BookingState.WAITING, 0, 10);
    }
}