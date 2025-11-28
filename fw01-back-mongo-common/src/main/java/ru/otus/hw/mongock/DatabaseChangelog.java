package ru.otus.hw.mongock;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ObjectUtils;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.model.Concept;
import ru.otus.hw.model.Topic;
import ru.otus.hw.model.Word;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@ChangeLog(order = "001")
public class DatabaseChangelog {

    private static final String FIRST_TOPIC = "Поликлиника";
    private static final String FIRST_TOPIC_DESCRIPTION = "Поликлиника - место для первичной диагностики заболеваний.";

    private static final String SECOND_TOPIC = "Глаголы";
    private static final String SECOND_TOPIC_DESCRIPTION = "Часто используемые глаголы";

    @ChangeSet(id = "001-create-first-topic", author = "tester", order = "001")
    public void createFirstTopic(MongockTemplate mongockTemplate) {

        mongockTemplate.save(Topic.builder()
                .name(FIRST_TOPIC)
                .description(FIRST_TOPIC_DESCRIPTION)
                .build());
        log.info("✅ Topic: {} created successfully", FIRST_TOPIC);
    }

    @ChangeSet(id = "002-create-second-topic", author = "tester", order = "002")
    public void createSecondTopic(MongockTemplate mongockTemplate) {

        mongockTemplate.save(Topic.builder()
                .name(SECOND_TOPIC)
                .description(SECOND_TOPIC_DESCRIPTION)
                .build());
        log.info("✅ Topic: {} created successfully", SECOND_TOPIC);
    }

    @ChangeSet(id = "003-create-concepts-to-first-topic", author = "tester", order = "003")
    public void createFirstTopicConcept(MongockTemplate mongockTemplate) {

        var insertedConcepts = mongockTemplate.insert(
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
                        .map(wordPair ->
                                DatabaseChangelog.createConceptForTopic(mongockTemplate, FIRST_TOPIC, wordPair))
                        .toList(), Concept.class);

        log.info("✅ Concepts for topic: {} saved successfully, passed: {} concepts", FIRST_TOPIC, insertedConcepts.size());
    }

    @ChangeSet(id = "004-create-concepts-to-second-topic", author = "tester", order = "004")
    public void createSecondTopicConcept(MongockTemplate mongockTemplate) {

        var insertedConcepts = mongockTemplate.insert(
                Stream.of(
                                new WordPair("Брать/Покупать", "לָקַחַת", "verb"),
                                new WordPair("Сидеть", "לָשֶׁבֶת", "verb"),
                                new WordPair("Стоять", "לַעֲמֹד", "verb"),
                                new WordPair("Подниматься", "לַעֲלוֹת", "verb"),
                                new WordPair("Пробовать", "לְנַסוֹת", "verb")
                        )
                        .map(wordPair ->
                                DatabaseChangelog.createConceptForTopic(mongockTemplate, SECOND_TOPIC, wordPair))
                        .toList(), Concept.class);

        log.info("✅ Concepts for topic: {} saved successfully, passed: {} concepts", SECOND_TOPIC, insertedConcepts.size());
    }

    /**
     * Helper method to create a concept with Russian and Hebrew words
     */
    private static Concept createConceptForTopic(MongockTemplate mongockTemplate,
                                                 String topicName,
                                                 WordPair wordPair) {

        Topic topic = findTopic(mongockTemplate, topicName);

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
                .topics(List.of(topic))
                .words(List.of(ruWord, heWord))
                .comment("")
                .build();
    }

    /**
     * Helper method to find the given topic and validate its existence
     */
    private static Topic findTopic(MongockTemplate mongockTemplate, String topicName) {
        Topic topic = mongockTemplate.findOne(
                Query.query(Criteria.where("name").is(topicName)),
                Topic.class);

        if (ObjectUtils.isEmpty(topic)) {
            throw new EntityNotFoundException("❌ Topic: %s not found!".formatted(topicName));
        }
        return topic;
    }

    // Helper record: RU, HE, partOfSpeech
    private record WordPair(String ruWord, String heWord, String partOfSpeech) {
    }
}
