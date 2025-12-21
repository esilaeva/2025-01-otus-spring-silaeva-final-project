package ru.otus.hw.dto;

import java.util.List;

/**
 * DTO for {@link ru.otus.hw.model.Concept}
 */
public record ConceptDto(List<TopicDto> topics,
                         List<WordDto> words,
                         String comment) {
}