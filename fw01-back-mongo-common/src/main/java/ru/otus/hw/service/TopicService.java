package ru.otus.hw.service;

import ru.otus.hw.dto.TopicDto;

import java.util.stream.Stream;

public interface TopicService {

    /**
     * Topics sorted by name.
     *
     * @return Stream of all sorted topics
     */
    Stream<TopicDto> getAllTopics();

    long getAllTopicsNumber();
}
