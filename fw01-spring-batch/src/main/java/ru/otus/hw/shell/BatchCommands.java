package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.util.Collection;

/**
 * Spring Shell commands for batch job operations.
 * Uses Spring Batch to manage import/export operations.
 */
@Slf4j
@Command(group = "Batch Operations:")
@RequiredArgsConstructor
public class BatchCommands {

    private final JobLauncher jobLauncher;

    private final Job importConceptsJob;

    private final Job exportConceptsJob;

    /**
     * Import concepts from YAML file to MongoDB.
     *
     * @param filePath path to the YAML input file (optional)
     * @return execution summary with job status and read/write counts
     */
    @Command(command = {"import-concepts"}, alias = {"ic"}, description = "Import concepts from YAML to MongoDB")
    public String importConcepts(@Option(longNames = {"file-path"}, shortNames = {'f'}, description = "YAML input file path")
                                 String filePath) {
        try {
            log.info("Starting concept import job");
            JobParametersBuilder paramsBuilder = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis());
            if (!StringUtils.isEmpty(filePath)) {
                paramsBuilder.addString("importFile", filePath);
                log.warn("Using custom input file: {}", filePath);
            } else {
                log.warn("Using default input file from application configuration");
            }
            JobParameters jobParameters = paramsBuilder.toJobParameters();

            JobExecution execution = jobLauncher.run(importConceptsJob, jobParameters);

            String status = execution.getStatus().toString();
            long readCount = 0;
            long writeCount = 0;

            Collection<StepExecution> stepExecutions = execution.getStepExecutions();
            if (!stepExecutions.isEmpty()) {
                StepExecution stepExecution = stepExecutions.iterator().next();
                readCount = stepExecution.getReadCount();
                writeCount = stepExecution.getWriteCount();
            }
            log.info("Import job completed with status: {}", status);

            return "Import job finished: %s%nRead: %d, Written: %d".formatted(status, readCount, writeCount);

        } catch (JobExecutionException e) {
            log.error("Job execution error during import", e);
            return "Import job failed: " + e.getMessage();
        } catch (Exception e) {
            log.error("Unexpected error running import job", e);
            return "Import job failed with unexpected error: " + e.getMessage();
        }
    }

    /**
     * Export concepts from MongoDB to YAML file.
     *
     * @param filePath path to the YAML output file (optional)
     * @return execution summary with job status
     */
    @Command(command = {"export-concepts"}, alias = {"ec"}, description = "Export concepts from MongoDB to YAML")
    public String exportConcepts(
            @Option(longNames = {"file-path"}, shortNames = {'f'}, description = "YAML output file path") String filePath) {
        try {
            log.info("Starting concept export job");
            JobParametersBuilder paramsBuilder = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis());
            if (!StringUtils.isEmpty(filePath)) {
                paramsBuilder.addString("exportFile", filePath);
                log.warn("Using custom output file: {}", filePath);
            } else {
                log.warn("Using default output file from application configuration");
            }
            JobParameters params = paramsBuilder.toJobParameters();
            JobExecution execution = jobLauncher.run(exportConceptsJob, params);
            String status = execution.getStatus().toString();
            log.info("Export job completed with status: {}", status);
            Collection<StepExecution> stepExecutions = execution.getStepExecutions();
            if (!stepExecutions.isEmpty()) {
                StepExecution stepExecution = stepExecutions.iterator().next();
                return "Export job finished: %s%nRead: %d, Written: %d".formatted(status, stepExecution.getReadCount(),
                        stepExecution.getWriteCount());
            }
            return String.format("Export job finished: %s", status);
        } catch (JobExecutionException e) {
            return "Export job failed: " + e.getMessage();
        } catch (Exception e) {
            return "Export job failed with unexpected error: " + e.getMessage();
        }
    }
}