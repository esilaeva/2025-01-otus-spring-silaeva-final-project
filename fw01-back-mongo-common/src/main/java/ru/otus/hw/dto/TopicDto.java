package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import ru.otus.hw.model.Topic;

/**
 * DTO for {@link Topic}
 */
public record TopicDto(@NotBlank String name, String description) {
}