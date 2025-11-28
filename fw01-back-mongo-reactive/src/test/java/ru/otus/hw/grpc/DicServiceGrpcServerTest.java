package ru.otus.hw.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.TopicDto;
import ru.otus.hw.enums.NotFoundMessage;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.proto.WordPair;
import ru.otus.hw.service.TopicService;
import ru.otus.hw.service.WordService;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Grpc Dictionary service: ")
@Slf4j
class DicServiceGrpcServerTest {

    private DicServiceGrpcServer grpcServer;

    @Mock
    private TopicService topicService;

    @Mock
    private WordService wordService;

    @Mock
    private StreamObserver<StringValue> stringValueResponseObserver;

    @Mock
    private StreamObserver<WordPair> wordPairStreamObserver;

    @Mock
    private StreamObserver<Int64Value> int64ValueStreamObserver;

    @Captor
    private ArgumentCaptor<StringValue> stringValueArgumentCaptor;

    @Captor
    private ArgumentCaptor<WordPair> wordPairArgumentCaptor;

    @Captor
    private ArgumentCaptor<Int64Value> int64ValueArgumentCaptor;

    @Captor
    private ArgumentCaptor<Throwable> errorCaptor;


    @BeforeEach
    void setUp() {
        grpcServer = new DicServiceGrpcServer(topicService, wordService);
    }

    @Test
    @DisplayName("should send all existing topics number")
    void getAllTopicsNumber() {

        when(topicService.getAllTopicsCount()).thenReturn(Mono.just(1L));

        grpcServer.getAllTopicsNumber(Empty.newBuilder().build(), int64ValueStreamObserver);

        verify(int64ValueStreamObserver).onNext(int64ValueArgumentCaptor.capture());
        verify(int64ValueStreamObserver).onCompleted();
        verify(int64ValueStreamObserver, never()).onError(any());

        assertThat(int64ValueArgumentCaptor.getValue().getValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should send all existing topics sorted by name")
    void getTopics() {
        when(topicService.getAllTopics()).thenReturn(
                Flux.fromStream(
                        Stream.of(
                                new TopicDto("BTopic", "BTopic description"),
                                new TopicDto("ATopic", "ATopic description")
                        )));

        grpcServer.getTopics(Empty.newBuilder().build(), stringValueResponseObserver);

        verify(stringValueResponseObserver, times(2)).onNext(stringValueArgumentCaptor.capture());
        verify(stringValueResponseObserver).onCompleted();
        verify(stringValueResponseObserver, never()).onError(any());

        assertThat(stringValueArgumentCaptor.getAllValues())
                .isNotEmpty()
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        StringValue.newBuilder().setValue("BTopic").build(),
                        StringValue.newBuilder().setValue("ATopic").build());
    }

    @Test
    @DisplayName("should throws error if no topics found (no concept found in DB)")
    void getErrorIfNoTopicsFound() {

        var notFoundException = new NotFoundException(
                NotFoundMessage.MODEL.getMessage().formatted("concepts"));

        when(topicService.getAllTopics()).thenReturn(Flux.error(notFoundException));

        try {
            grpcServer.getTopics(Empty.newBuilder().build(), stringValueResponseObserver);
        } catch (Exception e) {
            log.error("Caught expected exception from blockLast(): {}", e.getClass().getSimpleName());
        }

        verify(stringValueResponseObserver, never()).onNext(any());
        verify(stringValueResponseObserver, never()).onCompleted();
        verify(stringValueResponseObserver, times(1))
                .onError(errorCaptor.capture());

        assertThat(errorCaptor.getValue())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No concepts found, seems like database is empty")
                .hasCauseInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("should return pair as he:ru words for given topic")
    void getWordPairs() {

        final Map<String, String> wordPair1 = Map.of("He_1", "Ru_1");
        final Map<String, String> wordPair2 = Map.of("He_2", "Ru_2");

        when(wordService.getHeRuWordsPairs("ATopic")).thenReturn(Flux.just(wordPair1, wordPair2));

        grpcServer.getWordPairs(StringValue.newBuilder().setValue("ATopic").build(), wordPairStreamObserver);

        verify(wordPairStreamObserver, times(2)).onNext(wordPairArgumentCaptor.capture());
        verify(wordPairStreamObserver).onCompleted();
        verify(wordPairStreamObserver, never()).onError(any());

        assertThat(wordPairArgumentCaptor.getAllValues())
                .isNotEmpty()
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        WordPair.newBuilder().putAllWordPair(wordPair1).build(),
                        WordPair.newBuilder().putAllWordPair(wordPair2).build()
                );
    }

    @Test
    void gwtErrorIfNoConceptFoundForTopicName() {

        when(wordService.getHeRuWordsPairs("NotExistingTopic"))
                .thenReturn(Flux.error(
                        new EntityNotFoundException(
                                NotFoundMessage.CONCEPT.getMessage().formatted("NotExistingTopic"))));

        try {
            grpcServer.getWordPairs(
                    StringValue.newBuilder().setValue("NotExistingTopic").build(), wordPairStreamObserver);
        } catch (Exception e) {
            log.error("Caught expected exception from blockLast(): {}", e.getClass().getSimpleName());
        }

        verify(wordPairStreamObserver, never()).onNext(any());
        verify(wordPairStreamObserver, never()).onCompleted();
        verify(wordPairStreamObserver, times(1)).onError(errorCaptor.capture());

        assertThat(errorCaptor.getValue())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(NotFoundMessage.CONCEPT.getMessage().formatted("NotExistingTopic"))
                .hasCauseInstanceOf(EntityNotFoundException.class);
    }
}