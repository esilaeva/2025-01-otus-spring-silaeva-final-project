package ru.otus.hw.service;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.TopicDto;
import ru.otus.hw.enums.NotFoundMessage;
import ru.otus.hw.mapper.ModelToDtoMapper;
import ru.otus.hw.repository.ConceptRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final ConceptRepository conceptRepository;

    private final ModelToDtoMapper modelToDtoMapper;

    /**
     * Return of all Topics sorted by name.
     *
     * @return Flux of all sorted concepts
     */
    @Override
    public Flux<TopicDto> getAllTopics() {

        return conceptRepository.findAll()
                .flatMap(concept -> Flux.fromIterable(
                        ObjectUtils.isEmpty(concept.getTopics()) ? List.of() : concept.getTopics()))
                .distinct()
                .map(modelToDtoMapper::topicToTopicDto)
                .switchIfEmpty(Flux.error(
                        new NotFoundException(
                                NotFoundMessage.MODEL.getMessage().formatted("concepts"))));
    }

    /**
     * Return the number of topics were loaded.
     *
     * @return Mono of counts of loaded topics
     */
    @Override
    public Mono<Long> getAllTopicsCount() {

        return conceptRepository.findAll()
                .flatMap(concept -> Flux.fromIterable(concept.getTopics()))
                .distinct()
                .count();
    }
}
