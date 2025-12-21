package ru.otus.hw.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.TopicDto;

public interface TopicService {

    /**
     * Return of all Topics sorted by name.
     *
     * @return Flux of all sorted concepts
     */
    Flux<TopicDto> getAllTopics();

    /**
     * Return the number of topics were loaded.
     *
     * @return Mono of counts of loaded topics
     */
    Mono<Long> getAllTopicsCount();
}
