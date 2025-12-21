package ru.otus.hw.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.model.Topic;

import java.util.Optional;
import java.util.stream.Stream;

public interface TopicRepository extends MongoRepository<Topic, String> {
    Stream<Topic> streamAllBy();

    Optional<Topic> findByNameIgnoreCase(String name);
}
