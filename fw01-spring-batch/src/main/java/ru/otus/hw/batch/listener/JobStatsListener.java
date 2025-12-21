package ru.otus.hw.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JobStatsListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("=".repeat(80));
        log.info("Starting job: {}", jobExecution.getJobInstance().getJobName());
        log.info("Job ID: {}", jobExecution.getJobId());
        log.info("Start time: {}", jobExecution.getStartTime());
        log.info("=".repeat(80));
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Duration duration = Duration.between(
                Objects.requireNonNull(jobExecution.getStartTime()).toInstant(ZoneOffset.of("+03:00")),
                Objects.requireNonNull(jobExecution.getEndTime()).toInstant(ZoneOffset.of("+03:00")));

        log.info("Import job completed: {}", jobExecution.getJobInstance().getJobName());
        log.info("Status: {}", jobExecution.getStatus());
        log.info("Duration: {} milliseconds", TimeUnit.NANOSECONDS.toMillis(duration.get(ChronoUnit.NANOS)));
        jobExecution.getStepExecutions().forEach(stepExecution -> {
            log.info("Step: {}", stepExecution.getStepName());
            log.info("  Read count: {}", stepExecution.getReadCount());
            log.info("  Write count: {}", stepExecution.getWriteCount());
            log.info("  Skip count: {}", stepExecution.getSkipCount());
            log.info("  Rollback count: {}", stepExecution.getRollbackCount());
        });
        if (!jobExecution.getAllFailureExceptions().isEmpty()) {
            log.error("Job failed with {} exceptions:",
                    jobExecution.getAllFailureExceptions().size());
            jobExecution.getAllFailureExceptions()
                    .forEach(e -> log.error("  - {}", e.getMessage()));
        }
        log.info("=".repeat(80));
    }
}
