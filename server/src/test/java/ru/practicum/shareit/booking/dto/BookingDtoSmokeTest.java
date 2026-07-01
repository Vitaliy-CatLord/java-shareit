package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingDtoSmokeTest {

    @Test
    void touch_BookingCreateDto_getters() {
        LocalDateTime s = LocalDateTime.now().plusHours(1);
        LocalDateTime e = s.plusHours(2);

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(10L)
                .start(s)
                .end(e)
                .build();

        assertEquals(10L, dto.getItemId());
        assertEquals(s, dto.getStart());
        assertEquals(e, dto.getEnd());
    }

    @Test
    void touch_BookingShortDto_getters() {
        BookingShortDto shortDto = BookingShortDto.builder()
                .id(1L)
                .bookerId(7L)
                .build();

        assertEquals(1L, shortDto.getId());
        assertEquals(7L, shortDto.getBookerId());
    }

    @Test
    void touch_BookingDto_minimalGetters() {
        BookingDto dto = BookingDto.builder()
                .id(5L)
                .build();

        assertEquals(5L, dto.getId());
    }
}