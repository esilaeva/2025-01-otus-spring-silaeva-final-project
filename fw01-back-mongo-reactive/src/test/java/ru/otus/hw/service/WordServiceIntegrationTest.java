package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataMongoTest
@Import(WordServiceImpl.class)
@ComponentScan("ru.otus.hw.mapper")
@DisplayName("Word service: ")
class WordServiceIntegrationTest {

    @Autowired
    private WordService wordService;

    // Encapsulates topic-specific expectations
    private record TopicExpectation(String topicName,
                                    int expectedSize,
                                    List<Map<String, String>> expectedPairs) {
    }

    static Stream<Arguments> topicsData() {
        return Stream.of(
                Arguments.of(new TopicExpectation("Поликлиника", 11,
                        List.of(
                                Map.of("ביקור אצל הרופא", "Прием у врача"),
                                Map.of("אישור מחלה", "Больничный лист")))),

                Arguments.of(new TopicExpectation("Глаголы", 5,
                        List.of(
                                Map.of("לָקַחַת", "Брать/Покупать"),
                                Map.of("לְנַסוֹת", "Пробовать"))))
        );
    }


    @ParameterizedTest(name = "should return word pairs for topic {0}")
    @MethodSource("topicsData")
    void getHeRuWordsPairsByGivenTopic(TopicExpectation expectation) {

        StepVerifier.create(wordService.getHeRuWordsPairs(expectation.topicName))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(wordPair -> true)
                .consumeRecordedWith(listWordsPairs ->
                        assertAll(
                                () -> assertThat("Result map is not empty",
                                        listWordsPairs, is(not(empty()))),

                                () -> assertThat("Word pair count matches the seeded concepts",
                                        listWordsPairs, hasSize(expectation.expectedSize)),

                                () -> expectation.expectedPairs.forEach(pair ->
                                        assertThat(
                                                "Representative word pair is present for topic " + expectation.topicName,
                                                listWordsPairs,
                                                hasItem(pair)))
                        ))
                .verifyComplete();
    }
}
