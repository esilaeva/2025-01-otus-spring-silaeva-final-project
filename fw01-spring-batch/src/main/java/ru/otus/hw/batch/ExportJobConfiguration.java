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
import org.springframework.batch.item.data.MongoCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.batch.listener.JobStatsListener;
import ru.otus.hw.batch.listener.YamlWriterListener;
import ru.otus.hw.batch.processor.ConceptExportProcessor;
import ru.otus.hw.batch.writer.YamlConceptWriter;
import ru.otus.hw.dto.ConceptDto;
import ru.otus.hw.model.Concept;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExportJobConfiguration {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    @Value("${batch.output.yaml.path:exported-concepts.yaml}")
    private String outputFilePath;

    @Bean
    @StepScope
    public YamlConceptWriter yamlConceptWriter(@Value("#{jobParameters['exportFile']}")
                                                   String outFilePath) {
        outFilePath = StringUtils.isEmpty(outFilePath) ? outputFilePath : outFilePath;
        return new YamlConceptWriter(outFilePath);
    }

    @Bean
    public Step exportConceptsStep(MongoCursorItemReader<Concept> conceptMongoReader,
                                   YamlConceptWriter yamlConceptWriter,
                                   ConceptExportProcessor conceptExportProcessor,
                                   YamlWriterListener yamlWriterListener) {

        return new StepBuilder("exportConceptsStep", jobRepository)
                .<Concept, ConceptDto>chunk(5, transactionManager)
                .reader(conceptMongoReader)  //Read from MongoDB
                .processor(conceptExportProcessor) //Transform to DTO
                .writer(yamlConceptWriter)   //Write to YAML file
                .listener(yamlWriterListener)
                .build();
    }

    @Bean
    public Job exportConceptsJob(Step exportConceptsStep, JobStatsListener jobStatsListener) {
        return new JobBuilder("exportConceptsJob", jobRepository)
                // RunIdIncrementer adds unique run.id parameter
                // Allows same job to run multiple times
                .incrementer(new RunIdIncrementer())
                .start(exportConceptsStep)
                .listener(jobStatsListener)
                .build();
    }
}
