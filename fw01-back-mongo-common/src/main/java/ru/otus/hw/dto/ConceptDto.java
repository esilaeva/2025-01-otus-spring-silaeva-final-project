package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import ru.otus.hw.model.Concept;

import java.util.List;

/**
 * DTO for {@link Concept}
 */
public record ConceptDto(@NotBlank List<TopicDto> topics,
                         @NotBlank List<WordDto> words,
                         String comment) {
}