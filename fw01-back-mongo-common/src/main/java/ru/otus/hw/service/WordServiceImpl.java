package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.ConceptDto;
import ru.otus.hw.dto.WordDto;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.mapper.ModelToDtoMapper;
import ru.otus.hw.model.Concept;
import ru.otus.hw.model.Topic;
import ru.otus.hw.repository.ConceptRepository;
import ru.otus.hw.repository.TopicRepository;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class WordServiceImpl implements WordService {

    private final ConceptRepository conceptRepository;

    private final TopicRepository topicRepository;

    private final ModelToDtoMapper mapper;

    /**
     * Pairs of Hebrew and Russian words for a given topic.
     *
     * @param topicName topic name to filter words by
     * @return Stream of Map (He x Ru) of all concepts for given topic
     */
    @Override
    public Map<String, String> getHeRuWordPairs(String topicName) {

        // First: Find the topic by name (case-insensitive)
        Topic topic = topicRepository.findByNameIgnoreCase(topicName)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found: %s".formatted(topicName)));
        // Build a map pairing Hebrew words (keys) with their Russian translations (values)
        try (Stream<Concept> streamConceptsByTopicsId = conceptRepository.findConceptsByTopicsId(topic.getId())) {
            return streamConceptsByTopicsId
                    .map(mapper::conceptToConceptDto)
                    .map(ConceptDto::words)
                    .flatMap(Collection::stream)
                    .collect(Collectors.teeing(
                            Collectors.filtering(word -> "he".equals(word.language()),
                                    Collectors.mapping(WordDto::text, Collectors.toList())),
                            Collectors.filtering(word -> "ru".equals(word.language()),
                                    Collectors.mapping(WordDto::text, Collectors.toList())),
                            (heWord, ruWord) ->
                                    IntStream.range(0, Math.min(heWord.size(), ruWord.size()))
                                            .boxed()
                                            .collect(Collectors.toMap(heWord::get, ruWord::get))
                    ));
        }
    }
}
