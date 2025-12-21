package ru.otus.hw.batch.writer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.model.Concept;

@Slf4j
@Configuration
public class MongoWriterConfiguration {

    /**
     * Create MongoItemWriter bean for writing Concepts to MongoDB.
     * <p>
     * MongoItemWriter features:
     * - Batch writes for performance
     * - Automatic retry on failure
     * - Transaction support (if configured)
     * - Duplicate key handling
     *
     * @return Configured MongoItemWriter
     */
    @Bean
    public MongoItemWriter<Concept> conceptMongoWriter(MongoTemplate mongoTemplate) {

        log.info("✅ MongoItemWriter configured for 'concepts' collection");

        return new MongoItemWriterBuilder<Concept>()
                .template(mongoTemplate)
                .collection("concepts")
                .mode(MongoItemWriter.Mode.UPSERT)
                .build();
    }
}
