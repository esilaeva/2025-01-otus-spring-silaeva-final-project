package ru.otus.hw.util;

import javassist.NotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.otus.hw.dto.WordDto;
import ru.otus.hw.exception.EntityNotFoundException;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WordUtils {

    public static final String HEBREW_LANGUAGE = "he";

    public static final String RUSSIAN_LANGUAGE = "ru";

    public static Map<String, String> createWordPair(List<WordDto> wordDtoList) {
        try {
            if (wordDtoList.size() != 2) {
                throw new EntityNotFoundException(
                        "Expected 2 words (Hebrew + Russian), got: " + wordDtoList.size());
            }
            return Map.of(
                    extractWordTextByLanguage(wordDtoList, HEBREW_LANGUAGE),
                    extractWordTextByLanguage(wordDtoList, RUSSIAN_LANGUAGE)
            );
        } catch (NotFoundException e) {
            throw new EntityNotFoundException(e.getMessage());
        }
    }

    public static String extractWordTextByLanguage(List<WordDto> wordDtoList, String language) throws NotFoundException {
        return wordDtoList.stream()
                .filter(word -> word.language().equals(language))
                .findFirst()
                .map(WordDto::text)
                .orElseThrow(() -> new NotFoundException("No word with language: %s".formatted(language)));
    }
}
