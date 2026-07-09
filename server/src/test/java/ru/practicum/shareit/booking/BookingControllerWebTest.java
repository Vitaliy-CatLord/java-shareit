package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.models.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerWebTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Test
    @DisplayName("POST /bookings — create прокидывает userId и body в сервис")
    void create_ok() throws Exception {
        BookingCreateDto req = BookingCreateDto.builder()
                .itemId(10L)
                .build();

        BookingDto resp = BookingDto.builder().id(1L).build();
        when(bookingService.createBooking(eq(5L), any(BookingCreateDto.class))).thenReturn(resp);

        mvc.perform(post("/bookings")
                        .header(HEADER, 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookingService).createBooking(eq(5L), any(BookingCreateDto.class));
    }

    @Test
    @DisplayName("PATCH /bookings/{id}?approved=true — approve прокидывает флаг")
    void approve_ok() throws Exception {
        BookingDto resp = BookingDto.builder().id(99L).build();
        when(bookingService.aproveBooking(2L, 99L, true)).thenReturn(resp);

        mvc.perform(patch("/bookings/{id}", 99)
                        .header(HEADER, 2)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));

        verify(bookingService).aproveBooking(2L, 99L, true);
    }

    @Test
    @DisplayName("GET /bookings/{id} — getById прокидывает userId и bookingId")
    void getById_ok() throws Exception {
        BookingDto resp = BookingDto.builder().id(7L).build();
        when(bookingService.findBookingByUserId(3L, 7L)).thenReturn(resp);

        mvc.perform(get("/bookings/{id}", 7)
                        .header(HEADER, 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(bookingService).findBookingByUserId(3L, 7L);
    }

    @Test
    @DisplayName("GET /bookings?state=WAITING — парсинг state и вызов сервиса")
    void getByBooker_ok() throws Exception {
        when(bookingService.findAllByState(6L, BookingState.WAITING, 0, 10)).thenReturn(java.util.List.of());

        mvc.perform(get("/bookings")
                        .header(HEADER, 6)
                        .param("state", "WAITING"))
                .andExpect(status().isOk());

        verify(bookingService).findAllByState(6L, BookingState.WAITING, 0, 10);
    }

    @Test
    @DisplayName("GET /bookings/owner?state=CURRENT — парсинг state и вызов сервиса")
    void getByOwner_ok() throws Exception {
        when(bookingService.findAllOwnersBooking(4L, BookingState.CURRENT, 0, 10)).thenReturn(java.util.List.of());

        mvc.perform(get("/bookings/owner")
                        .header(HEADER, 4)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk());

        verify(bookingService).findAllOwnersBooking(4L, BookingState.CURRENT, 0, 10);
    }
}