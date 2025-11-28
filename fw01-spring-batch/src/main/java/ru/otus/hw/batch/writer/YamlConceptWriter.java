package ru.otus.hw.batch.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import ru.otus.hw.dto.ConceptDto;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class YamlConceptWriter implements ItemWriter<ConceptDto> {

    private final List<ConceptDto> allConcepts = new ArrayList<>();

    private final String outputFilePath;


    @Override
    public void write(Chunk<? extends ConceptDto> chunk) throws Exception {
        // Accumulate items from each chunk
        allConcepts.addAll(chunk.getItems());
        log.info("✅ Accumulated: {} concepts so far", allConcepts.size());
    }

    /**
     * Called at the end of the step to write all accumulated data.
     * This method needs to be invoked by a StepExecutionListener
     */
    public void writeAllToYaml() throws IOException {

        log.info("✅ Writing: {} concepts to YAML file: {}", allConcepts.size(), outputFilePath);

        // Configure YAML output format
        YAMLFactory yamlFactory = new YAMLFactory()
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);

        // Create ObjectMapper with YAML factory
        ObjectMapper mapper = new ObjectMapper(yamlFactory);

        // Wrap concepts in a map against the "concepts" key
        Map<String, List<ConceptDto>> data = Map.of("concepts", allConcepts);

        // Write data to YAML file
        try (FileWriter fileWriter = new FileWriter(outputFilePath)) {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(fileWriter, data);
        }

        log.info("✅ Successfully exported: {} concepts to: {}", allConcepts.size(), outputFilePath);
    }

    /**
     * Clear accumulated data (useful for testing or multiple runs)
     */
    public void clear() {
        allConcepts.clear();
    }
}