package ru.otus.hw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.dto.ConceptDto;
import ru.otus.hw.dto.TopicDto;
import ru.otus.hw.dto.WordDto;
import ru.otus.hw.model.Concept;
import ru.otus.hw.model.Topic;
import ru.otus.hw.model.Word;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = SPRING)
public interface ConceptMapper {

    @Mapping(target = "id", ignore = true)
    Concept toEntity(ConceptDto dto);

    ConceptDto toDto(Concept entity);

    /**
     * Convert TopicDto to Topic.
     */
    Topic toEntity(TopicDto dto);

    /**
     * Convert Topic to TopicDto.
     */
    TopicDto toDto(Topic entity);

    /**
     * Convert WordDto to Word.
     */
    Word toEntity(WordDto dto);

    /**
     * Convert Word to WordDto.
     */
    WordDto toDto(Word entity);
}
