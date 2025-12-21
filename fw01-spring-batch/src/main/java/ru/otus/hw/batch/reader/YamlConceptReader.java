package ru.otus.hw.batch.reader;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import ru.otus.hw.dto.ConceptDto;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Custom ItemReader for reading Concepts from a YAML file.
 * This reader loads the entire YAML file into memory and serves items one by one.
 * <p>
 * The YAML structure expected:<br>
 * concepts:
 * - topics: [...]
 *   words: [...]
 *   comment: "..."
 * - topics: [...]
 *   words: [...]
 *   comment: "..."
 */
@Slf4j
@RequiredArgsConstructor
public class YamlConceptReader implements ItemReader<ConceptDto> {

    private final String filePath;

    private List<ConceptDto> concepts;

    private int currentIndex;

    private boolean isInitialized = false;

    /**
     * Read the next ConceptDto from the YAML file.<br>
     * Called repeatedly by Spring Batch until null is returned.
     *
     * @return Next ConceptDto, or null when all items have been processed
     */
    @Override
    public ConceptDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        // Initialize on first read - load entire YAML file
        if (!isInitialized) {
            initializeReader();
            isInitialized = true;
        }

        // Return null when all items processed (signals end of data)
        if (currentIndex >= concepts.size()) {
            log.info("✅ All: {} concepts have been read from YAML file", concepts.size());
            return null;
        }

        // Return next concept and increment index
        ConceptDto concept = concepts.get(currentIndex);
        currentIndex++;

        log.info("✅ Reading concept: {}/{}", currentIndex, concepts.size());
        return concept;

    }

    /**
     * Initialize the reader by loading and parsing the YAML file.
     * This method runs once before the first read operation.
     *
     * @throws IOException if file cannot be read or parsed
     */
    private void initializeReader() throws IOException {
        log.info("✅ Initializing YAML reader from file: {}", filePath);

        // Create ObjectMapper with YAML support
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // Read YAML file
        File yamlFile = new File(filePath);
        if (!yamlFile.exists()) {
            throw new IOException("❌ YAML file not found: %s".formatted(filePath));
        }

        // Parse YAML into wrapper record to extract concepts list
        ConceptsEnvelope envelope = mapper.readValue(yamlFile, ConceptsEnvelope.class);
        this.concepts = Optional.ofNullable(envelope.concepts()).orElse(List.of());
        this.currentIndex = 0;
    }

    /**
     * Reset reader state. Useful for testing or re-running the same reader.
     */
    public void reset() {
        this.currentIndex = 0;
        this.isInitialized = false;
        this.concepts = null;
    }

    /**
     * Helper inner record to deserialize the top-level "concepts" key from YAML.
     * Works for both imported and exported concept files.
     */
    private record ConceptsEnvelope(@JsonProperty("concepts") List<ConceptDto> concepts) {}
}



