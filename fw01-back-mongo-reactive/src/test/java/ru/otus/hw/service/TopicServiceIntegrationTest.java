package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(TopicServiceImpl.class)
@ComponentScan("ru.otus.hw.mapper")
@DisplayName("Topic service: ")
class TopicServiceIntegrationTest {

    @Autowired
    private TopicService topicService;


    @Test
    @DisplayName("Should return all existing topics")
    void getAllTopics() {
        StepVerifier.create(topicService.getAllTopics())
                .recordWith(ArrayList::new)
                .thenConsumeWhile(topicDto -> true)
                .consumeRecordedWith(
                        topicDtos ->
                                assertThat(topicDtos)
                                        .isNotEmpty()
                                        .hasSize(2)
                                        .extracting("name")
                                        .containsExactlyInAnyOrder("Глаголы","Поликлиника"))
                .verifyComplete();
    }

    @Test
    @DisplayName("should return all topics count")
    void getAllTopicsCount() {

        StepVerifier.create(topicService.getAllTopicsCount()).expectNext(2L).verifyComplete();
    }
}