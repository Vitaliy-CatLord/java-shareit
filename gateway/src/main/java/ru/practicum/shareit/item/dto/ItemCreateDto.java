package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateDto {
    @NotBlank(message = "name must be not blank")
    private String name;

    @NotBlank(message = "description must be not blank")
    private String description;

    @NotNull(message = "available must be not null")
    private Boolean available;

    @Positive(message = "requestId must be positive")
    private Long requestId;
}
