package ru.otus.hw.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.model.Concept;

import java.util.stream.Stream;

public interface ConceptRepository extends MongoRepository<Concept, String> {

    Stream<Concept> findConceptsByTopicsId(String topicId);

}
