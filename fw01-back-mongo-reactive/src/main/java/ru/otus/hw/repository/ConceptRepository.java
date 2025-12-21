package ru.otus.hw.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.model.Concept;

public interface ConceptRepository extends ReactiveMongoRepository<Concept, String> {
    Flux<Concept> findConceptsByTopicsNameIgnoreCase(String topicName);
}