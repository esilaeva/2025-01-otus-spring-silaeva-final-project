package ru.otus.hw.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Word {
    /**
     * ISO 639-1 language code (2-letter code) for the word's language.
     * Using standardized language codes ensures consistency and
     * enables proper internationalization support.
     * <p>
     * Examples:
     * - "en" for English
     * - "he" for Hebrew
     * - "ru" for Russian
     * - "es" for Spanish
     * - "fr" for French
     */
    @Indexed
    private String language;

    private String text;

    /**
     * Part of speech classification for the word.
     * Helps users understand grammatical usage and context.
     * <p>
     * Common values:
     * - "noun" - person, place, thing, or idea
     * - "verb" - action or state of being
     * - "adjective" - describes or modifies nouns
     * - "adverb" - modifies verbs, adjectives, or other adverbs
     * - "preposition" - shows relationships between words
     * - "conjunction" - connects words or phrases
     * - "interjection" - expresses emotion
     */
    private String partOfSpeech;

    private String comment;

    /**
     * URL to an audio file containing the pronunciation of the word.
     * Enables users to hear correct pronunciation, which is especially
     * valuable for language learning and unfamiliar scripts.
     * <p>
     * Example: "https://audio.example.com/pronunciations/house_en.mp3"
     */
    private String audioUrl;
}
