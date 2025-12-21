package ru.otus.hw.mapper;

import org.mapstruct.Mapper;
import ru.otus.hw.dto.ConceptDto;
import ru.otus.hw.dto.TopicDto;
import ru.otus.hw.dto.WordDto;
import ru.otus.hw.model.Concept;
import ru.otus.hw.model.Topic;
import ru.otus.hw.model.Word;

@Mapper(componentModel = "spring")
public interface ModelToDtoMapper {

    TopicDto topicToTopicDto(Topic topic);

    ConceptDto conceptToConceptDto(Concept concept);

    WordDto wordToWordDto(Word word);
}
