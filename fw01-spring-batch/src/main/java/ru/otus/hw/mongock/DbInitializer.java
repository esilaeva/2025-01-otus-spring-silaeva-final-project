package ru.otus.hw.mongock;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import ru.otus.hw.model.Concept;
import ru.otus.hw.model.Topic;
import ru.otus.hw.model.Word;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@ChangeUnit(id = "test-database-initializer", order = "001", author = "tester")
public class DbInitializer {

    @Execution
    public void execution(MongoTemplate mongoTemplate) {

        var savedConcepts = mongoTemplate.insert(
                Stream.of(
                                new WordPair("Регистратура", "מזכירות", "noun"),
                                new WordPair("Головная боль", "כאב ראש", "noun"),
                                new WordPair("Записаться на прием", "לקבוע תור", "verb"),
                                new WordPair("Прием у врача", "ביקור אצל הרופא", "noun"),
                                new WordPair("Симптомы", "תסמינים", "noun"),
                                new WordPair("Температура", "חום", "noun"),
                                new WordPair("Кашель", "שיעול", "noun"),
                                new WordPair("Насморк", "נזלת", "noun"),
                                new WordPair("Рецепт", "מרשם", "noun"),
                                new WordPair("Направление", "הפניה", "noun"),
                                new WordPair("Больничный лист", "אישור מחלה", "noun")
                        )
                        .map(DbInitializer::createConcept)
                        .toList(), Concept.class);

        log.info("✅ Concepts saved successfully, passed: {} concepts", savedConcepts.size());
    }

    @RollbackExecution
    public void rollbackExecution(MongoTemplate mongoTemplate) {
        mongoTemplate.remove(new Query(), Concept.class);
    }

    /**
     * Helper method to create a concept with Russian and Hebrew words
     */
    private static Concept createConcept(WordPair wordPair) {
        Word ruWord = Word.builder()
                .language("ru")
                .text(wordPair.ruWord)
                .partOfSpeech(wordPair.partOfSpeech)
                .audioUrl("")
                .comment("")
                .build();
        Word heWord = Word.builder()
                .language("he")
                .text(wordPair.heWord)
                .partOfSpeech(wordPair.partOfSpeech)
                .audioUrl("")
                .comment("")
                .build();
        return Concept.builder()
                .topics(List.of(Topic.builder()
                        .name("Поликлиника")
                        .description("Поликлиника - место для первичной диагностики заболеваний.")
                        .build()))
                .words(List.of(ruWord, heWord))
                .comment("")
                .build();
    }

    // Helper record: RU, HE, partOfSpeech
    private record WordPair(String ruWord, String heWord, String partOfSpeech) {
    }
}

