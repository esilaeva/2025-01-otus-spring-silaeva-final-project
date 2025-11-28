package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.TopicDto;
import ru.otus.hw.mapper.ModelToDtoMapper;
import ru.otus.hw.repository.TopicRepository;

import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository repository;

    private final ModelToDtoMapper mapper;

    /**
     * Topics sorted by name.
     *
     * @return Stream of all sorted topics
     */
    @Override
    public Stream<TopicDto> getAllTopics() {

        return repository.streamAllBy().map(mapper::topicToTopicDto);
    }

    /**
     * The number of topics stored in DB
     *
     * @return Long as number of topics
     */
    @Override
    public long getAllTopicsNumber() {
        return repository.count();
    }
}
