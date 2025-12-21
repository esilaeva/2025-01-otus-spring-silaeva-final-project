package ru.otus.hw.service;

import java.util.Map;

public interface WordService {

    /**
     * Pairs of Hebrew and Russian words for a given topic.
     *
     * @param topicName topic name to filter words by
     * @return Stream of Map (He x Ru) of all concepts for given topic
     */
    Map<String, String> getHeRuWordPairs(String topicName);
}
