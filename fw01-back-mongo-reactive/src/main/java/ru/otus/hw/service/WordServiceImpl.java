package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.otus.hw.enums.NotFoundMessage;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.mapper.ModelToDtoMapper;
import ru.otus.hw.repository.ConceptRepository;
import ru.otus.hw.util.WordUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WordServiceImpl implements WordService {

    private final ConceptRepository conceptRepository;

    private final ModelToDtoMapper modelToDtoMapper;

    /**
     * Return pairs of Hebrew and Russian words for a given topic.
     *
     * @param topicName topic name to filter words by
     * @return Flux of Map (He x Ru) of all concepts
     */
    @Override
    public Flux<Map<String, String>> getHeRuWordsPairs(String topicName) {

        return conceptRepository.findConceptsByTopicsNameIgnoreCase(topicName)
                .switchIfEmpty(Flux.error(
                        new EntityNotFoundException(
                                NotFoundMessage.CONCEPT.getMessage().formatted(topicName))))
                .flatMap(concept -> Flux.fromIterable(concept.getWords())
                        .map(modelToDtoMapper::wordToWordDto)
                        .buffer(2)
                        .map(WordUtils::createWordPair));
    }
}
