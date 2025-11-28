package ru.otus.hw.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.otus.hw.model.Concept;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@SpringBatchTest
@DisplayName("Our batch jobs: ")
class JobExecutionsTest {

    private static final long EXPECTED_DB_CONCEPTS_COUNT = 2;
    private static final long COUNT_CONCEPTS_FROM_YAML = 5;
    private static final String EXPECTED_YAML_FILE = "expectedExportFile.yaml";
    private static final String RESULT_YAML_FILE = "test-exported-concepts.yaml";
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());


    private static Path exportTempFile;

    @TempDir
    private static Path tempDir;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private Job exportConceptsJob;

    @Autowired
    private Job importConceptsJob;


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        exportTempFile = tempDir.resolve(RESULT_YAML_FILE);
        registry.add("batch.output.yaml.path", exportTempFile::toString);
    }

    @BeforeEach
    void cleanUp() {
        // Clean up batch early execution info
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    @DisplayName("should export DB data to yaml file")
    void testExportJob() throws Exception {

        long expectedTestConceptsCount = mongoTemplate.count(new Query(), Concept.class);
        assertThat(expectedTestConceptsCount)
                .as("Initial test records are present")
                .isEqualTo(EXPECTED_DB_CONCEPTS_COUNT);

        // when: Launch the job
        var params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.getJobLauncher().run(exportConceptsJob, params);

        assertThat(jobExecution.getExitStatus().getExitCode())
                .as("Verify the job exit code is COMPLETED (job competed successful)")
                .isEqualTo("COMPLETED");

        assertThat(jobExecution.getStepExecutions())
                .as("Verify that all expected steps were executed")
                .hasSize(1)
                .extracting("stepName")
                .containsExactlyInAnyOrder("exportConceptsStep");

        //Select the step we need and verify it
        StepExecution exportConceptsStep = jobExecution.getStepExecutions().stream()
                .filter(stepExecution -> stepExecution.getStepName().equals("exportConceptsStep"))
                .findFirst()
                .orElseThrow();
        assertThat(exportConceptsStep)
                .as("Verify particular step was executed successfully and made expected read/write operations")
                .satisfies(
                        stepExecution -> {
                            assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
                            assertThat(stepExecution.getReadCount()).isEqualTo(expectedTestConceptsCount);
                            assertThat(stepExecution.getWriteCount()).isEqualTo(expectedTestConceptsCount);
                        });

        assertThat(Files.exists(exportTempFile))
                .as("Is our exported yaml file exists")
                .isTrue();

        Map<String, Object> actualContent = parseYamlContent(exportTempFile.toString(), Map.class);

        Map<String, Object> expectedContent;

        try (InputStream expectedStream = new ClassPathResource(EXPECTED_YAML_FILE).getInputStream()) {
            expectedContent = parseYamlContent(expectedStream, Map.class);
        }
        assertThat(actualContent)
                .as("Exported YAML structure should match expected format using semantic validation")
                .isEqualTo(expectedContent);

    }

    @Test
    @DirtiesContext
    @DisplayName("should import DB data from yaml file")
    void testImportJob() throws Exception {

        long expectedTestConceptsCount = mongoTemplate.count(new Query(), Concept.class);
        assertThat(expectedTestConceptsCount)
                .as("Initial test records are present only")
                .isEqualTo(EXPECTED_DB_CONCEPTS_COUNT);

        // when: Launch the job
        var params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.getJobLauncher().run(importConceptsJob, params);

        assertThat(jobExecution.getExitStatus().getExitCode())
                .as("Verify the job exit code is COMPLETED 'job competed successful'")
                .isEqualTo("COMPLETED");

        assertThat(jobExecution.getStepExecutions())
                .as("Verify that all expected steps were executed")
                .hasSize(1)
                .extracting("stepName")
                .containsExactlyInAnyOrder("importConceptsStep");

        //Select the step we need and verify it
        StepExecution importConceptsStep = jobExecution.getStepExecutions().stream()
                .filter(stepExecution -> stepExecution.getStepName().equals("importConceptsStep"))
                .findFirst()
                .orElseThrow();
        assertThat(importConceptsStep)
                .as("Verify particular step was executed successfully and made expected read/write operations")
                .satisfies(
                        stepExecution -> {
                            assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
                            assertThat(stepExecution.getReadCount()).isEqualTo(COUNT_CONCEPTS_FROM_YAML);
                            assertThat(stepExecution.getWriteCount()).isEqualTo(COUNT_CONCEPTS_FROM_YAML);
                        });

        List<Concept> allConceptList = mongoTemplate.findAll(Concept.class);
        assertThat(allConceptList)
                .as("Verify that all expected concepts were imported successfully")
                .as("Now we have have 2 old and 5 new, total 7 concepts in MongoDB")
                .isNotEmpty()
                .hasSize(7);

        Criteria allVerbsCriteria = Criteria.where("topics.name").is("Глаголы");
        List<Concept> conceptVerbsList = mongoTemplate.find(new Query(allVerbsCriteria), Concept.class);
        assertThat(conceptVerbsList)
                .as("Check that all concepts with topic 'Глаголы' are migrated successfully")
                .isNotEmpty()
                .hasSize(5);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("topics.name").is("Глаголы")),
                Aggregation.unwind("words"),
                Aggregation.match(Criteria.where("words.language").is("ru")),
                Aggregation.project().and("words.text").as("ruText").andExclude("_id"));

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "concepts",
                Document.class);

        assertThat(results.getMappedResults())
                .as("Verify that all russian words for concepts are migrated successfully")
                .isNotEmpty()
                .hasSize(5)
                .extracting("ruText")
                .containsExactlyInAnyOrder("Брать/Покупать", "Сидеть", "Стоять", "Подниматься", "Пробовать");
    }

    /**
     * Parses YAML content into the specified type for semantic validation.
     * Supports file paths or input streams for flexibility in test scenarios.
     *
     * @param filePath path to YAML file
     * @param clazz    target type (e.g., Map.class for unstructured data)
     * @param <T>      the target type
     * @return parsed YAML content
     * @throws IOException              if file cannot be read or YAML is invalid
     * @throws IllegalArgumentException if filePath is null or empty
     */
    private <T> T parseYamlContent(String filePath, Class<T> clazz) throws IOException {
        Objects.requireNonNull(filePath, "File path cannot be null");
        if (filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            return YAML_MAPPER.readValue(fileInputStream, clazz);
        }
    }

    /**
     * Overload for parsing from an InputStream (e.g., classpath resources).
     *
     * @param inputStream the YAML input stream
     * @param clazz       target type (e.g., Map.class)
     * @param <T>         the target type
     * @return parsed YAML content
     * @throws IOException if stream reading or YAML parsing fails
     */
    private <T> T parseYamlContent(InputStream inputStream, Class<T> clazz) throws IOException {
        Objects.requireNonNull(inputStream, "Input stream cannot be null");
        Objects.requireNonNull(clazz, "Target class cannot be null");
        return YAML_MAPPER.readValue(inputStream, clazz);
    }
}