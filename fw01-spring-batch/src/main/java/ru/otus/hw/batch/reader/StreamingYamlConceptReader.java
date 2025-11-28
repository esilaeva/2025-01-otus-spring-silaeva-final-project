package ru.otus.hw.batch.reader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import ru.otus.hw.dto.ConceptDto;
import ru.otus.hw.dto.TopicDto;
import ru.otus.hw.dto.WordDto;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A streaming ItemReader implementation for reading ConceptDto objects from large YAML files
 * in a memory-efficient manner. This reader processes YAML files incrementally rather than
 * loading the entire file content into memory, making it suitable for handling large datasets
 * in Spring Batch processing.
 *
 * <p>The reader expects YAML files with a specific structure containing a 'concepts' array
 * where each element is a ConceptDto. It uses Jackson's streaming API to parse the YAML
 * incrementally, ensuring minimal memory footprint during batch processing operations.
 *
 * @see org.springframework.batch.item.ItemReader
 * @see ConceptDto
 */
@Slf4j
@RequiredArgsConstructor
public class StreamingYamlConceptReader implements ItemReader<ConceptDto> {

    private static final String DELIMITER = ", ";

    /**
     * Path to the YAML file containing the concepts data.
     * Must be accessible as a file system resource.
     */
    private final String filePath;

    /**
     * Jackson JsonParser instance for streaming YAML parsing.
     * Initialized on first read() call and managed throughout the reading process.
     */
    private YAMLParser yamlParser;

    /**
     * Iterator for deserializing ConceptDto objects from the YAML stream.
     * Created after locating the concepts array in the YAML file.
     */
    private MappingIterator<ConceptDto> conceptIterator;

    /**
     * Flag indicating whether the reader has been initialized.
     * Prevents multiple initializations during a single reading session.
     */
    private boolean isInitialized;

    /**
     * Reads the next available ConceptDto from the YAML stream.
     * This method implements the Spring Batch ItemReader contract and should be called
     * repeatedly until it returns null, indicating end of stream.
     *
     * <p>On the first call, this method automatically initializes the streaming parser
     * and locates the concepts array in the YAML file. Subsequent calls return the next
     * concept from the stream until all concepts have been processed.
     *
     * @return the next ConceptDto from the YAML stream, or null if end of stream reached
     * @throws Exception if any error occurs during reading or deserialization
     * @throws UnexpectedInputException if the YAML file format is invalid
     * @throws ParseException if deserialization of a concept fails
     * @throws NonTransientResourceException if the YAML file cannot be accessed
     * @throws IOException if file system errors occur during reading
     *
     * @see #initializeStream()
     * @see #closeStream()
     */
    @Override
    public ConceptDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!isInitialized) {
            initializeStream();
            isInitialized = true;
        }
        // Check if more concepts are available in the stream
        if (conceptIterator != null && conceptIterator.hasNext()) {
            try {
                ConceptDto concept = conceptIterator.next();  // Deserializes the next array element
                log.info("✅ Read concept from stream (topics: {}, words pairs: {})",
                        concept.topics() != null ? topicsName(concept.topics()) : "N/A",
                        concept.words() != null ? wordsName(concept.words()) : "N/A");
                return concept;
            } catch (Exception e) {
                log.error("❌ Failed to deserialize ConceptDto from YAML stream", e);
                throw new ParseException("Deserialization error in streaming reader", e);
            }
        }
        // End of stream reached
        log.info("✅ End of YAML stream reached - no more concepts to read");
        closeStream();  // Clean up resources at end
        return null;
    }

    /**
     * Initializes the YAML streaming parser and prepares for concept reading.
     * This method orchestrates the complete initialization process by delegating
     * to specialized helper methods.
     *
     * @throws IOException if file validation fails or parsing encounters structural errors
     * @throws IllegalStateException if the concepts array is not found or has invalid structure
     *
     */
    private void initializeStream() throws IOException {
        log.info("✅ Initializing streaming YAML reader from file: {}", filePath);
        
        validateYamlFile();
        createYamlParser();
        findConceptsArray();
        initializeConceptIterator();
    }

    /**
     * Validates that the YAML file exists and is not empty.
     *
     * @throws IOException if file validation fails
     */
    private void validateYamlFile() throws IOException {
        File yamlFile = new File(filePath);
        if (!yamlFile.exists()) {
            throw new IOException("YAML file not found: " + filePath);
        }
        if (yamlFile.length() == 0) {
            throw new IOException("YAML file is empty: " + filePath);
        }
    }

    /**
     * Creates YAML parser with auto-close enabled.
     *
     * @throws IOException if parser creation fails
     */
    private void createYamlParser() throws IOException {
        YAMLFactory yamlFactory = new YAMLFactory();
        this.yamlParser = yamlFactory.createParser(new File(filePath));
        yamlParser.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
    }

    /**
     * Finds the 'concepts' array in the YAML structure and positions parser at its start.
     *
     * @throws IOException if concepts array is not found or has invalid structure
     */
    private void findConceptsArray() throws IOException {
        boolean isConceptsFound = false;
        while (yamlParser.nextToken() != null) {
            if (yamlParser.currentToken() == JsonToken.FIELD_NAME && "concepts".equals(yamlParser.currentName())) {
                yamlParser.nextToken();
                if (yamlParser.currentToken() != JsonToken.START_ARRAY) {
                    throw new IOException("❌ Expected START_ARRAY after 'concepts' key, but found: " +
                            yamlParser.currentToken());
                }
                isConceptsFound = true;
                break;
            }
        }
        if (!isConceptsFound) {
            throw new IOException("❌ Could not find 'concepts' array in YAML file: " + filePath);
        }
    }

    /**
     * Initializes the concept iterator based on array content.
     * Handles both empty and populated concepts arrays.
     *
     * @throws IOException if iterator initialization fails
     */
    private void initializeConceptIterator() throws IOException {
        yamlParser.nextToken();
        
        if (yamlParser.currentToken() == JsonToken.START_OBJECT) {
            YAMLFactory yamlFactory = new YAMLFactory();
            ObjectMapper mapper = new ObjectMapper(yamlFactory);
            this.conceptIterator = mapper.readValues(yamlParser, ConceptDto.class);
            log.info("✅ Streaming YAML reader initialized successfully - ready to read concepts from array");
        } else if (yamlParser.currentToken() == JsonToken.END_ARRAY) {
            this.conceptIterator = null;
            log.info("✅ Concepts array is empty - iterator will return null immediately");
        } else {
            throw new IOException("❌ Expected START_OBJECT or END_ARRAY after START_ARRAY, but found: " +
                    yamlParser.currentToken());
        }
    }

    /**
     * Closes the JSON parser and underlying file stream to free resources.
     * Called automatically at end-of-read or explicitly.
     *
     * @throws IOException If close fails (rare).
     */
    private void closeStream() throws IOException {
        if (yamlParser != null) {
            try {
                yamlParser.close();  // Closes file handle and stream
                log.info("✅ YAML stream closed");
            } finally {
                yamlParser = null;
                conceptIterator = null;  // Allow GC
            }
        }
    }

    /**
     * Resets the reader to its initial state, allowing for reuse in scenarios such as
     * job restarts or testing. This method closes the current stream and clears all
     * state variables, preparing the reader for a fresh reading session.
     *
     * <p>After calling reset(), the next read() call will reinitialize the stream
     * and start reading from the beginning of the YAML file.
     *
     * @throws IOException if errors occur during stream closing
     *
     * @see #initializeStream()
     * @see #closeStream()
     */
    public void reset() throws IOException {
        log.info("✅ Resetting streaming YAML reader");
        closeStream();
        isInitialized = false;
        // File will be re-opened on next read()
    }

    /**
     * Provides explicit resource cleanup for integration with Spring Batch's resource
     * management lifecycle. This method delegates to closeStream() and may be called
     * by the Spring container after job completion.
     *
     * @throws IOException if errors occur during stream closing
     *
     * @see #closeStream()
     */
    @SuppressWarnings("unused")  // For potential ResourceAwareItemReader integration
    public void close() throws IOException {
        closeStream();
    }

    /**
     * Helper method to extract topic names for logging purposes.
     *
     * @param topics the list of TopicDto objects
     * @return comma-separated string of topic names, or empty string if null/empty
     */
    private static String topicsName(List<TopicDto> topics) {
        return topics.stream()
                .map(TopicDto::name)
                .collect(Collectors.joining(DELIMITER));
    }

    /**
     * Helper method to extract word texts for logging purposes.
     *
     * @param words the list of WordDto objects
     * @return comma-separated string of words, or empty string if null/empty
     */
    private static String wordsName(List<WordDto> words) {
        return words.stream()
                .map(WordDto::text)
                .collect(Collectors.joining(DELIMITER));
    }

}


