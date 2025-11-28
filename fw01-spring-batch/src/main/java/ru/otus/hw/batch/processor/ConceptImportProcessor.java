package ru.otus.hw.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.ConceptDto;
import ru.otus.hw.mapper.ConceptMapper;
import ru.otus.hw.model.Concept;


@Slf4j
@Component
@RequiredArgsConstructor
public class ConceptImportProcessor implements ItemProcessor<ConceptDto, Concept> {

    private final ConceptMapper conceptMapper;

    /**
     * Process a single ConceptDto and convert it to a Concept entity.
     *
     * @param conceptDto The ConceptDto read from YAML file
     * @return Concept entity ready for MongoDB persistence, or null to skip this item
     * @throws Exception if processing fails
     */
    @Override
    public Concept process(ConceptDto conceptDto) throws Exception {

        log.debug("Processing concept with {} topics and {} words",
                conceptDto.topics() != null ? conceptDto.topics().size() : 0,
                conceptDto.words() != null ? conceptDto.words().size() : 0);

        // Validation: Skip invalid concepts
        if (!isValid(conceptDto)) {
            log.warn("Skipping invalid concept: {}", conceptDto);
            return null;  // Returning null skips this item
        }
        // Transform DTO to Entity using MapStruct mapper
        Concept concept = conceptMapper.toEntity(conceptDto);
        // Additional processing can be done here:
        // Example: Ensure empty strings are null
        if (concept.getComment() != null && concept.getComment().trim().isEmpty()) {
            concept.setComment(null);
        }
        log.debug("Successfully processed concept");
        return concept;
    }

    /**
     * Validate ConceptDto before processing.
     *
     * @param dto The DTO to validate
     * @return true if valid, false otherwise
     */
    private boolean isValid(ConceptDto dto) {
        // Check for null or empty collections
        if (dto == null) {
            log.warn("Concept DTO is null");
            return false;
        }
        if (dto.topics() == null || dto.topics().isEmpty()) {
            log.warn("Concept has no topics");
            return false;
        }
        if (dto.words() == null || dto.words().isEmpty()) {
            log.warn("Concept has no words");
            return false;
        }
        // Validate that each word has required fields
        boolean allWordsValid = dto.words().stream()
                .allMatch(word -> word.language() != null && !word.language().isEmpty()
                        && word.text() != null && !word.text().isEmpty());
        if (!allWordsValid) {
            log.warn("Some words are missing required fields (language or text)");
            return false;
        }
        return true;
    }
}
