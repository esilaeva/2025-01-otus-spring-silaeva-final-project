package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;

public record WordDto(@NotBlank String language,
                      @NotBlank String text,
                      String partOfSpeech,
                      String comment,
                      String audioUrl) {
}
