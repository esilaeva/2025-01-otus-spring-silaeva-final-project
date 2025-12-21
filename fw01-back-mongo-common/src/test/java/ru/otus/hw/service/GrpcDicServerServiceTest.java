package ru.otus.hw.service;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dto.TopicDto;
import ru.otus.hw.proto.WordPair;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Grpc Dictionary service: ")
class GrpcDicServerServiceTest {

    private GrpcDicServerService server;

    @Mock
    private StreamObserver<StringValue> stringValueResponseObserver;

    @Mock
    private StreamObserver<WordPair> wordPairStreamObserver;

    @Mock
    private TopicService topicService;

    @Mock
    private WordService wordService;

    @Captor
    private ArgumentCaptor<StringValue> stringValueArgumentCaptor;

    @Captor
    private ArgumentCaptor<WordPair> wordPairArgumentCaptor;


    @BeforeEach
    void setUp() {
        server = new GrpcDicServerService(topicService, wordService);
    }

    @Test
    @DisplayName("should return all topics")
    void getTopics() {
        when(topicService.getAllTopics()).thenReturn(
                Stream.of(new TopicDto("Topic 1", "Description for topic 1")));

        server.getTopics(Empty.getDefaultInstance(), stringValueResponseObserver);

        verify(stringValueResponseObserver).onNext(stringValueArgumentCaptor.capture());
        verify(stringValueResponseObserver).onCompleted();
        verify(stringValueResponseObserver, never()).onError(any());

        assertAll("Let's check the answer to the following criteria",
                () -> assertThat("Answer has one topic",
                        stringValueArgumentCaptor.getAllValues(), hasSize(1)),
                () -> assertThat("And this topic name is 'Topic 1'",
                        stringValueArgumentCaptor.getAllValues().getFirst().getValue(), is("Topic 1")
                ));
    }

    @Test
    @DisplayName("should return pair as he:ru words for given topic")
    void getWordPairs() {
        when(wordService.getHeRuWordPairs("Topic 1"))
                .thenReturn(Map.of("HeWord 1", "RuWord 1", "HeWord_2", "RuWord_2"));

        server.getWordPairs(StringValue.newBuilder().setValue("Topic 1").build(), wordPairStreamObserver);

        verify(wordPairStreamObserver, times(2)).onNext(wordPairArgumentCaptor.capture());
        verify(wordPairStreamObserver).onCompleted();
        verify(wordPairStreamObserver, never()).onError(any());

        assertAll("Let's check the answer to the following criteria",
                () -> assertThat("Retuned result has two pairs",
                        wordPairArgumentCaptor.getAllValues(), hasSize(2)),
                () -> assertThat("And those word pair contains 'HeWord 1' and 'HeWord_2'",
                        wordPairArgumentCaptor.getAllValues()
                                .stream()
                                .allMatch(wp -> wp.containsWordPair("HeWord 1")
                                        || wp.containsWordPair("HeWord_2")))
        );
    }
}