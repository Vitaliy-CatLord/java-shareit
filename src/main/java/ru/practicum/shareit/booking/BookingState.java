package ru.practicum.shareit.booking;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED,
    UNKNOWN;

    /**
     * Метод from(String bookingState) выполняет преобразование строки состояния бронирования в объект BookingState.
     *
     * @param bookingState Строка состояния бронирования
     * @return Возвращает объект BookingState, соответствующий переданной строке состояния бронирования.
     * Если соответствующего значения не найдено, возвращает null
     */
    public static BookingState from(String bookingState) {
        try {
            return BookingState.valueOf(bookingState.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
