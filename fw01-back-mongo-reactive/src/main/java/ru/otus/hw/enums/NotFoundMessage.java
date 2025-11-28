package ru.otus.hw.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotFoundMessage {
    MODEL("No %s found, seems like database is empty"),
    CONCEPT("No concepts found for topic: %s"),
    WORD("No words found for topic: %s");

    private final String message;
}
