package ru.otus.hw.batch.reader;

import org.springframework.batch.item.data.MongoCursorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.model.Concept;

import java.util.HashMap;

@Configuration
public class MongoReaderConfiguration {

    @Bean
    public MongoCursorItemReader<Concept> conceptMongoReader(MongoTemplate mongoTemplate) {

        MongoCursorItemReader<Concept> reader = new MongoCursorItemReader<>();
        reader.setName("conceptMongoReader");
        reader.setTemplate(mongoTemplate);
        reader.setCollection("concepts");
        reader.setQuery("{}");
        reader.setSort(new HashMap<>());
        reader.setTargetType(Concept.class);
        reader.setBatchSize(5);

        return reader;
    }
}
