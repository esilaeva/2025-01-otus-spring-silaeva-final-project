package ru.otus.hw.service;

import reactor.core.publisher.Flux;

import java.util.Map;

public interface WordService {

    /**
     * Return pairs of Hebrew and Russian words for a given topic.
     *
     * @param topic topic name to filter words by
     * @return Flux of Map (He x Ru) of all concepts
     */
    Flux<Map<String, String>> getHeRuWordsPairs(String topic);
}
