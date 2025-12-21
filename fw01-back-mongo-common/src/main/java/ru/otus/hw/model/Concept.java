package ru.otus.hw.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "concepts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Concept {

    @Id
    private String id;

    @DBRef
    private List<Topic> topics;

    private List<Word> words;

    private String comment;
}
