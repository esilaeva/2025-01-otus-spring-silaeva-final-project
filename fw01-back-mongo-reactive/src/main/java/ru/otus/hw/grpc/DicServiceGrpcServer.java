package ru.otus.hw.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;
import reactor.core.scheduler.Schedulers;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.proto.DicServiceGrpc;
import ru.otus.hw.proto.WordPair;
import ru.otus.hw.service.TopicService;
import ru.otus.hw.service.WordService;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class DicServiceGrpcServer extends DicServiceGrpc.DicServiceImplBase {

    private final TopicService topicService;

    private final WordService wordService;

    /**
     * RPC Method 3: getAllTopicsNumber
     * <p>
     * Pattern: Unary RPC
     * Signature: rpc getAllTopicsNumber(google.protobuf.Empty) returns (google.protobuf.Int64Value);
     * <p>
     * Purpose:
     * Unary RPC that returns the total count of unique topics in the system.
     * This is different from streaming RPCs - client makes one request, receives one response.
     * <p>
     * Data Flow:
     * 1. Client sends Empty message
     * 2. TopicService.getAllTopicsCount() returns Mono<Long> with the count
     * 3. Count is wrapped in Int64Value protobuf message
     * 4. Response is sent to client via responseObserver.onNext() followed by onCompleted()
     * <p>
     * Unary RPC Pattern:
     * - Request-response model (single request, single response)
     * - Simpler than streaming: no need to handle multiple messages
     * - Blocking from client perspective: client waits for the complete response
     * <p>
     * Reactive Bridge Pattern:
     * Mono<Long> → map to Int64Value → send with doOnSuccess → complete with responseObserver.onCompleted()
     * <p>
     * Error Handling:
     * - INTERNAL (13): Database errors or unexpected exceptions
     * - UNAVAILABLE (14): Service temporarily unavailable
     *
     * @param request          Empty message (no input parameters)
     * @param responseObserver Used to send the count result to client
     */
    @Override
    public void getAllTopicsNumber(Empty request, StreamObserver<Int64Value> responseObserver) {
        topicService.getAllTopicsCount()
                // Transform Long count to gRPC Int64Value message
                .map(count -> Int64Value.newBuilder()
                        .setValue(count)
                        .build())
                // Send the single response to the client
                .doOnSuccess(int64Value -> {
                    log.info("Got: {} as all topics count", int64Value.getValue());
                    responseObserver.onNext(int64Value);
                    // Complete the unary call
                    responseObserver.onCompleted();
                })
                // Handle errors and convert to appropriate gRPC status
                .doOnError(error -> {
                    log.error("Error getting topics count", error);
                    handleError(responseObserver, error);
                })
                .subscribeOn(Schedulers.boundedElastic())
                // waits for that one element
                .block();
    }

    /**
     * RPC Method 1: getTopics
     * <p>
     * Signature: rpc getTopics(google.protobuf.Empty) returns (stream google.protobuf.StringValue);
     * <p>
     * Purpose:
     * Retrieves all available topics sorted alphabetically. This is a server-side streaming RPC
     * where the client makes one request and receives a stream of topic names until the stream completes.
     * <p>
     * Data Flow:
     * 1. Client sends Empty message
     * 2. Service retrieves all topics from database via TopicService.getAllTopicsSortByName()
     * 3. Each TopicDto is converted to a StringValue (protobuf wrapper type)
     * 4. Each StringValue is sent to the client via StreamObserver.onNext()
     * 5. Stream completes with onCompleted() or error with onError()
     * <p>
     * Error Handling:
     * - INTERNAL: Database connection failures (converted from service exceptions)
     * - UNAVAILABLE: Service dependencies unavailable
     *
     * @param request          Empty message (no input parameters)
     * @param responseObserver Used to send topic names to client as a stream
     */
    @Override
    public void getTopics(Empty request, StreamObserver<StringValue> responseObserver) {
        topicService.getAllTopics()
                // Transform TopicDto to gRPC StringValue message
                .map(topicDto -> StringValue.newBuilder()
                        .setValue(topicDto.name())
                        .build())
                // Stream each topic to the client
                .doOnNext(stringValue -> {
                    log.info("Streaming topic: {}", stringValue.getValue());
                    responseObserver.onNext(stringValue);
                })
                // Complete the stream when all topics are sent
                .doOnComplete(() -> {
                    log.info("Completed streaming all topics");
                    responseObserver.onCompleted();
                })
                .doOnError(error -> {
                    log.error("Error getting topics", error);
                    handleError(responseObserver, error);
                })
                .subscribeOn(Schedulers.boundedElastic())
                // waits for the LAST element and all preceding ones
                .blockLast();
    }

    /**
     * RPC Method 2: getWordPairs
     * <p>
     * Signature: rpc getWordPairs(google.protobuf.StringValue) returns (stream WordPair);
     * <p>
     * Purpose:
     * Retrieves Hebrew-Russian word pairs for a specific topic. This is the most complex endpoint,
     * implementing server-side streaming with reactive data processing.
     * <p>
     * Data Flow:
     * 1. Client sends StringValue containing the topic name
     * 2. WordService.getHeRuWordsPairs(topic) returns Flux<Map<String, String>>
     * 3. Each Map entry (Hebrew word -> Russian word) is converted to a WordPair protobuf message
     * 4. Each WordPair is streamed to the client
     * 5. Stream completes when all word pairs are sent
     * <p>
     * Data Transformation:
     * - Input: Flux<Map<String, String>> where keys are Hebrew words, values are Russian translations
     * - Output: stream of WordPair messages, each containing a map of one Hebrew-Russian pair
     * <p>
     * WordPair Proto Structure:
     * ```
     * message WordPair {
     * map<string, string> wordPair = 1;  // key: Hebrew word, value: Russian word
     * }
     * ```
     * <p>
     * Error Handling:
     * - NOT_FOUND: Topic doesn't exist (EntityNotFoundException from service)
     * - INTERNAL: Database errors or processing failures
     * <p>
     * Performance Considerations:
     * - Uses backpressure: client controls consumption rate
     * - Non-blocking: doesn't block gRPC event loop
     * - Buffering: Reactor manages internal buffering for efficiency
     *
     * @param request          StringValue containing the topic name to filter word pairs by
     * @param responseObserver Used to stream WordPair messages to client
     */
    @Override
    public void getWordPairs(StringValue request, StreamObserver<WordPair> responseObserver) {
        wordService.getHeRuWordsPairs(request.getValue())
                .map(hebrewRussianMap -> {
                    WordPair.Builder wordPairBuilder = WordPair.newBuilder();
                    hebrewRussianMap.forEach(wordPairBuilder::putWordPair);
                    return wordPairBuilder.build();
                })
                .doOnNext(wordPair -> { // Stream each word pair to the client
                    log.info("Streaming word pair: {}", wordPair);
                    responseObserver.onNext(wordPair);
                })
                .doOnComplete(() -> { // Complete stream after all pairs sent
                    log.info("Completed streaming all word pairs for topic: {}", request.getValue());
                    responseObserver.onCompleted();
                })
                .doOnError(error -> { // Handle errors
                    log.error("Error getting word pairs for topic: {}", request.getValue(), error);
                    handleError(responseObserver, error);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .blockLast(); // waits for the LAST element and all preceding ones
    }

    /**
     * Helper Method: Centralized Error Handler
     * <p>
     * Purpose:
     * Converts Java exceptions to appropriate gRPC Status codes and messages.
     * This provides consistent error handling across all RPC methods.
     * <p>
     * gRPC Status Codes Mapping:
     * - EntityNotFoundException → NOT_FOUND (5): Requested resource doesn't exist
     * - NotFoundException → NOT_FOUND (5): Legacy support for javassist.NotFoundException
     * - Other exceptions → INTERNAL (13): Unexpected server-side errors
     * <p>
     * Best Practices:
     * - Always log errors for debugging
     * - Provide descriptive error messages to clients
     * - Use specific status codes for different error types
     * - Never expose internal details (stack traces) to clients
     * <p>
     * Additional Status Codes (can be extended):
     * - INVALID_ARGUMENT (3): Invalid request parameters
     * - PERMISSION_DENIED (7): Authentication/authorization failures
     * - UNAVAILABLE (14): Service temporarily unavailable
     * - DEADLINE_EXCEEDED (4): Request timeout
     *
     * @param responseObserver The StreamObserver to send error to
     * @param error            The exception that occurred
     */
    private <T> void handleError(StreamObserver<T> responseObserver, Throwable error) {
        Status status;
        // Map exceptions to appropriate gRPC status codes
        if (error instanceof EntityNotFoundException) {
            // Custom application exception for entities not found
            status = Status.NOT_FOUND
                    .withDescription(error.getMessage())
                    .withCause(error);
        } else if (error instanceof javassist.NotFoundException) {
            // Legacy NotFoundException from service layer
            status = Status.NOT_FOUND
                    .withDescription(error.getMessage())
                    .withCause(error);
        } else {
            // Generic server error for unexpected exceptions
            status = Status.INTERNAL
                    .withDescription("Internal server error: " + error.getMessage())
                    .withCause(error);
        }
        // Log the error with full context
        log.error("Sending gRPC error: {} - {}", status.getCode(), status.getDescription());
        // Send the error to the client
        responseObserver.onError(status.asRuntimeException());
    }

}
