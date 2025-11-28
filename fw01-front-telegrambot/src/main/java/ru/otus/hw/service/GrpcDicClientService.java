package ru.otus.hw.service;

import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.otus.hw.exception.OrderServiceException;
import ru.otus.hw.proto.DicServiceGrpc;
import ru.otus.hw.proto.WordPair;
import ru.otus.hw.util.CommonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class GrpcDicClientService {

    private final DicServiceGrpc.DicServiceStub stub;

    /**
     * Provide a topics number
     *
     * @return long as number of all topics
     */
    public long getAllTopicsNumber() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicLong responseContainer = new AtomicLong();
            AtomicReference<StatusRuntimeException> errorContainer = new AtomicReference<>();
            StreamObserver<Int64Value> responseObserver = createStreamObserver(
                    value -> {
                        log.info("✅ Received topics number: {}", value.getValue());
                        responseContainer.set(value.getValue());
                    }, errorContainer, latch, () -> "topics number");
            stub.getAllTopicsNumber(Empty.newBuilder().build(), responseObserver);
            latch.await();
            throwIfError(errorContainer);
            return responseContainer.get();
        } catch (StatusRuntimeException e) {
            log.error("❌ Failed to process topics: status={}, description={}",
                    e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw handleGrpcException("process topics number", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Thread interrupted while processing orders", e);
            throw new OrderServiceException("Processing operation interrupted", e);
        }
    }

    /**
     * Provide a topics list
     *
     * @return List of all topics
     */
    public List<String> getAllTopics() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            CopyOnWriteArrayList<String> collectedTopicsName = new CopyOnWriteArrayList<>();
            AtomicReference<StatusRuntimeException> errorContainer = new AtomicReference<>();
            StreamObserver<StringValue> responseObserver = createStreamObserver(
                    value -> {
                        log.info("✅ Received topic: {}", value.getValue());
                        collectedTopicsName.add(value.getValue());
                    }, errorContainer, latch, () -> String.format("received: %d topics", collectedTopicsName.size())
            );
            stub.getTopics(Empty.newBuilder().build(), responseObserver);
            latch.await();
            throwIfError(errorContainer);
            return new ArrayList<>(collectedTopicsName);
        } catch (StatusRuntimeException e) {
            log.error("❌ Failed to process topics: status={}, description={}",
                    e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw handleGrpcException("process topics", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Thread interrupted while processing orders", e);
            throw new OrderServiceException("Processing operation interrupted", e);
        }
    }

    /**
     * Provide a word pairs for quiz
     *
     * @return Map of word pairs: HeWord : RuWord
     */
    public Map<String, String> getWordPairsByTopicName(String topicName) {
        CommonUtils.checkStringIsPresent(topicName, "Topic");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            ConcurrentHashMap<String, String> collectedWordPairs = new ConcurrentHashMap<>();
            AtomicReference<StatusRuntimeException> errorContainer = new AtomicReference<>();
            StreamObserver<WordPair> responseObserver = createStreamObserver(
                    wordPair -> {
                        log.info("✅ Received word pair: {}", wordPair.getWordPairMap());
                        collectedWordPairs.putAll(wordPair.getWordPairMap());
                    },
                    errorContainer, latch,
                    () -> String.format("word pairs, received: %d word pairs", collectedWordPairs.size()));
            stub.getWordPairs(StringValue.of(topicName), responseObserver);
            latch.await();
            throwIfError(errorContainer);
            return new HashMap<>(collectedWordPairs);
        } catch (StatusRuntimeException e) {
            throw handleGrpcException("process word pairs", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrderServiceException("Processing operation interrupted", e);
        }
    }


    /**
     * Creates a generic StreamObserver with common error handling and completion logic.
     *
     * @param onNextHandler             Consumer to handle each received value
     * @param errorContainer            Container to store any StatusRuntimeException
     * @param latch                     CountDownLatch to signal completion
     * @param completionMessageSupplier Supplier for the completion log message context
     * @param <T>                       Type of the stream response
     * @return StreamObserver configured with the provided handlers
     */
    private static <T> StreamObserver<T> createStreamObserver(
            Consumer<T> onNextHandler,
            AtomicReference<StatusRuntimeException> errorContainer,
            CountDownLatch latch,
            Supplier<String> completionMessageSupplier) {
        return new StreamObserver<>() {

            @Override
            public void onNext(T value) {
                onNextHandler.accept(value);
            }

            @Override
            public void onError(Throwable t) {
                if (t instanceof StatusRuntimeException) {
                    errorContainer.set((StatusRuntimeException) t);
                }
                log.error("❌ Failed to process: {}", t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("✅ Receiving {} completed", completionMessageSupplier.get());
                latch.countDown();
            }
        };
    }

    /**
     * Throws the stored exception if present.
     *
     * @param errorContainer Container that may hold a StatusRuntimeException
     */
    private static void throwIfError(AtomicReference<StatusRuntimeException> errorContainer) {
        StatusRuntimeException error = errorContainer.get();
        if (error != null) {
            throw error;
        }
    }

    private static OrderServiceException handleGrpcException(String operation, StatusRuntimeException e) {
        String message = String.format("Failed to %s: %s", operation, e.getStatus().getDescription());

        return switch (e.getStatus().getCode()) {
            case UNAVAILABLE -> new OrderServiceException.ServiceUnavailableException(message, e);
            case DEADLINE_EXCEEDED -> new OrderServiceException.TimeoutException(message, e);
            case INVALID_ARGUMENT -> new OrderServiceException.InvalidArgumentException(message, e);
            case PERMISSION_DENIED -> new OrderServiceException.PermissionDeniedException(message, e);
            case UNAUTHENTICATED -> new OrderServiceException.AuthenticationException(message, e);
            default -> new OrderServiceException(message, e);
        };
    }
}
