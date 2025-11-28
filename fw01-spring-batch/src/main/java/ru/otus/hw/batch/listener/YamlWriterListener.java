package ru.otus.hw.batch.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;
import ru.otus.hw.batch.writer.YamlConceptWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class YamlWriterListener implements StepExecutionListener {

    private final YamlConceptWriter yamlConceptWriter;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("✅ Starting export step: {}", stepExecution.getStepName());
        yamlConceptWriter.clear(); // Clear any previous data
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        try {
            log.info("✅ Step completed. Writing accumulated data to YAML...");
            yamlConceptWriter.writeAllToYaml();
            log.info("✅ YAML export completed successfully");
            return ExitStatus.COMPLETED;
        } catch (Exception e) {
            log.error("❌ Failed to write accumulate data to YAML file", e);
            return ExitStatus.FAILED;
        }
    }
}
