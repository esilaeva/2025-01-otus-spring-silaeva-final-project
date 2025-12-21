package ru.otus.hw.service;

import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.proto.DicServiceGrpc;
import ru.otus.hw.proto.WordPair;

@Service
@Slf4j
@RequiredArgsConstructor
public class GrpcDicServerService extends DicServiceGrpc.DicServiceImplBase {

    private final TopicService topicService;

    private final WordService wordService;

    // Unary
    @Override
    public void getAllTopicsNumber(Empty request, StreamObserver<Int64Value> responseObserver) {
        long allTopicsNumber = topicService.getAllTopicsNumber();

        if (allTopicsNumber <= 0L) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("❌ No topics found, seems db is empty!")
                    .asException());
        }
        responseObserver.onNext(Int64Value.newBuilder().setValue(allTopicsNumber).build());
        responseObserver.onCompleted();
    }

    // Server stream
    @Override
    public void getTopics(Empty request, StreamObserver<StringValue> responseObserver) {

        topicService.getAllTopics()
                .peek(topic -> log.info("✅ Found a topic: '{}'", topic.name()))
                .forEach(topic -> responseObserver.onNext(StringValue.newBuilder().setValue(topic.name()).build()));
        responseObserver.onCompleted();
    }

    // Server stream
    @Override
    public void getWordPairs(StringValue request, StreamObserver<WordPair> responseObserver) {

        if (request == null || request.getValue().trim().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("❌ Search term cannot be null or empty")
                    .asException());
            return;
        }

        String searchTerm = request.getValue().trim();

        wordService.getHeRuWordPairs(searchTerm).entrySet().stream()
                .peek(wordEntry ->
                        log.info("✅ Found a word pair: {} - {}", wordEntry.getKey(), wordEntry.getValue()))
                .forEach(wordEntry ->
                        responseObserver.onNext(
                                WordPair.newBuilder().putWordPair(wordEntry.getKey(), wordEntry.getValue()).build()));

        responseObserver.onCompleted();
    }
}
