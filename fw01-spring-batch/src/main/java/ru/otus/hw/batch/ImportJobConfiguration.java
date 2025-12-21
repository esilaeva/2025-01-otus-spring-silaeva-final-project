package ru.otus.hw.batch;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.batch.listener.JobStatsListener;
import ru.otus.hw.batch.processor.ConceptImportProcessor;
import ru.otus.hw.batch.reader.StreamingYamlConceptReader;
import ru.otus.hw.batch.reader.YamlConceptReader;
import ru.otus.hw.dto.ConceptDto;
import ru.otus.hw.model.Concept;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ImportJobConfiguration {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    @Value("${batch.input.yaml.path:imported-concepts.yaml}")
    private String inputFilePath;

    @Bean
    @StepScope
    public YamlConceptReader yamlConceptReader(@Value("#{jobParameters['importFile']}")
                                                   String inFilePath) {
        inFilePath = StringUtils.isEmpty(inFilePath) ? inputFilePath : inFilePath;
        log.info("✅ Configuring YAML reader for file: {}", inFilePath);
        return new YamlConceptReader(inFilePath);
    }

    @Bean
    @StepScope
    public StreamingYamlConceptReader streamingYamlConceptReader(@Value("#{jobParameters['importFile']}")
                                                                     String inFilePath) {
        inFilePath = StringUtils.isEmpty(inFilePath) ? inputFilePath : inFilePath;
        log.info("✅ Configuring streaming YAML reader for file: {}", inFilePath);
        return new StreamingYamlConceptReader(inFilePath);
    }

    @Bean
    public Step importConceptsStep(StreamingYamlConceptReader yamlReader,
                                   ConceptImportProcessor conceptImportProcessor,
                                   MongoItemWriter<Concept> conceptMongoWriter) {

        return new StepBuilder("importConceptsStep", jobRepository)
                .<ConceptDto, Concept>chunk(10, transactionManager)// Process 10 items per chunk
                .reader(yamlReader)
                .processor(conceptImportProcessor)
                .writer(conceptMongoWriter)
                .build();
    }

    @Bean
    public Job importConceptsJob(Step importConceptsStep,
                                 JobStatsListener jobStatsListener) {

        return new JobBuilder("importConceptsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(importConceptsStep)
                .listener(jobStatsListener)
                .build();
    }
}
