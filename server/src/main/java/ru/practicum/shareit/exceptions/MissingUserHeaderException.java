package ru.practicum.shareit.exceptions;

public class MissingUserHeaderException extends RuntimeException {
    public MissingUserHeaderException(String message) {
        super(message);
    }
}
