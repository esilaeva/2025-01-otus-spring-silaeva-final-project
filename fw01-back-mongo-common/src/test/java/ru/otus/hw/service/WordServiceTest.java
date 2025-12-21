package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.otus.hw.exception.EntityNotFoundException;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataMongoTest
@Import(WordServiceImpl.class)
@ComponentScan("ru.otus.hw.mapper")
@DisplayName("Word service: ")
class WordServiceTest {

    @Autowired
    private WordService wordService;

    @ParameterizedTest
    @MethodSource("topicWordPairs")
    @DisplayName("should return word pairs for given topic")
    void getHeRuWordPairsParameterized(String topicName, int expectedSize, String sampleHe, String sampleRu) {

        Map<String, String> wordPairs = wordService.getHeRuWordPairs(topicName);

        assertAll(
                () -> assertThat("Result map is not empty",
                        wordPairs.entrySet(), is(not(empty()))),
                () -> assertThat("We have %d of testing word pairs".formatted(expectedSize),
                        wordPairs, aMapWithSize(expectedSize)),
                () -> assertThat("Word pairs map include pair: '%s':'%s'".formatted(sampleHe, sampleRu),
                        wordPairs, hasEntry(sampleHe, sampleRu))
        );
    }

    static Stream<Arguments> topicWordPairs() {
        return Stream.of(
                Arguments.of("Поликлиника", 11, "ביקור אצל הרופא", "Прием у врача"),
                Arguments.of("Глаголы", 5, "לְנַסוֹת", "Пробовать")
        );
    }

    @Test
    @DisplayName("should throw exception if topic not found")
    void getHeRuWordPairs_ExceptionNotTopicFound() {
        assertThatThrownBy(() -> wordService.getHeRuWordPairs("Больница"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Topic not found: %s".formatted("Больница"));
    }
}
