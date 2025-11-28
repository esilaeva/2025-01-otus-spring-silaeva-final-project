// File: src/test/java/ru/otus/hw/service/MockDicServiceImpl.java

package ru.otus.hw.service;

import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ru.otus.hw.proto.DicServiceGrpc;
import ru.otus.hw.proto.WordPair;

import java.util.Map;

/**
 * Mock implementation of DicService for testing purposes.
 * This service simulates the behavior of the real gRPC server.
 */

public class DicGrpcServerMock extends DicServiceGrpc.DicServiceImplBase {

    private boolean simulateError = false;
    private Status errorStatus = Status.UNAVAILABLE;

    /**
     * Mock implementation of getAllTopicsNumber RPC.
     * Unary send 10 as the number of topics.
     */
    @Override
    public void getAllTopicsNumber(Empty request, StreamObserver<Int64Value> responseObserver) {
        responseObserver.onNext(Int64Value.newBuilder().setValue(10).build());
        responseObserver.onCompleted();
    }

    /**
     * Mock implementation of getTopics RPC.
     * Streams predefined topics to the client.
     */
    @Override
    public void getTopics(Empty request, StreamObserver<StringValue> responseObserver) {

        if (simulateError) {
            responseObserver.onError(errorStatus.asRuntimeException());
            return;
        }

        try {
            // Simulate streaming multiple topics
            responseObserver.onNext(StringValue.of("Animals"));
            responseObserver.onNext(StringValue.of("Colors"));
            responseObserver.onNext(StringValue.of("Food"));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error streaming topics")
                    .asRuntimeException());
        }
    }

    /**
     * Mock implementation of getWordPairs RPC.
     * Streams word pairs based on the topic name.
     */
    @Override
    public void getWordPairs(StringValue request, StreamObserver<WordPair> responseObserver) {

        if (simulateError) {
            responseObserver.onError(errorStatus.asRuntimeException());
            return;
        }

        final Map<String, String> animalsMap = Map.of(
                "Dog", "Собака",
                "Cat", "Кошка"
        );
        final Map<String, String> colorsMap = Map.of(
                "Red", "Красный",
                "Blue", "Синий",
                "Black", "Черный"
        );

        String topicName = request.getValue();

        try {
            // Return different word pairs based on topic
            switch (topicName) {
                case "Animals" -> animalsMap.forEach(
                        (key, value) ->
                                responseObserver.onNext(WordPair.newBuilder().putWordPair(key, value).build()
                                ));

                case "Colors" -> colorsMap.forEach(
                        (key, value) ->
                                responseObserver.onNext(WordPair.newBuilder().putWordPair(key, value).build()
                                ));

                case "InvalidTopic" -> {
                    responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription("Topic not found: " + topicName)
                            .asRuntimeException());
                    return;
                }
                default -> responseObserver.onNext(WordPair.newBuilder()
                        .putAllWordPair(Map.of("Default", "Значение по умолчанию"))
                        .build());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error streaming word pairs")
                    .asRuntimeException());
        }
    }

    /**
     * Configure the mock to simulate error conditions for testing.
     */
    public void setSimulateError(boolean simulateError, Status errorStatus) {
        this.simulateError = simulateError;
        this.errorStatus = errorStatus;
    }

    /**
     * Reset the mock to normal operation.
     */
    public void reset() {
        this.simulateError = false;
        this.errorStatus = Status.UNAVAILABLE;
    }
}
