package ru.otus.hw.batch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.otus.hw.dto.ConceptDto;
import ru.otus.hw.mapper.ConceptMapper;
import ru.otus.hw.model.Concept;

@Component
@RequiredArgsConstructor
public class ConceptExportProcessor implements ItemProcessor<Concept, ConceptDto> {

    private final ConceptMapper conceptMapper;

    @Override
    public ConceptDto process(Concept concept) {
        // Validate concept here
        if (CollectionUtils.isEmpty(concept.getWords()) || CollectionUtils.isEmpty(concept.getTopics())) {
            // Skip concepts without words or topics
            return null;
        }
        // Clean up data
        if (concept.getComment() == null) {
            concept.setComment("");
        }
        return conceptMapper.toDto(concept);
    }
}
