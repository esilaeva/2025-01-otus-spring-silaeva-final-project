package ru.otus.hw.dto;

import ru.otus.hw.model.Word;

/**
 * DTO for {@link Word}
 */
public record WordDto(String language,
                      String text,
                      String partOfSpeech,
                      String comment,
                      String audioUrl) {
}