package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.TopicDto;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataMongoTest
@Import(TopicServiceImpl.class)
@ComponentScan("ru.otus.hw.mapper")
@DisplayName("Topic service: ")
class TopicServiceTest {

    @Autowired
    private TopicService topicService;

    @Test
    @DisplayName("should return all topics")
    void shouldReturnAllTopics() {

        List<TopicDto> topicDtoList = topicService.getAllTopics().toList();

        assertAll(
                () -> assertThat("Result is not empty", topicDtoList, is(not(empty()))),
                () -> assertThat("Contains one topic", topicDtoList.size(), is(2)),
                () -> assertThat("Contains 2 topics with name 'Поликлиника' and 'Глаголы'",
                        topicDtoList.stream()
                                .filter(topic -> topic.name().matches("Поликлиника|Глаголы"))
                                .count(), is(2L)

        ));
    }
}