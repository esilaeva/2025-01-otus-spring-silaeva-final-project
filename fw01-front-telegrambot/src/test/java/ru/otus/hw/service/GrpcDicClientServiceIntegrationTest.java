package ru.otus.hw.service;

import io.grpc.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.otus.hw.configuration.GrpcClientTestConfiguration;
import ru.otus.hw.exception.OrderServiceException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for GrpcDicClientService.
 * Uses in-process gRPC server with mock implementation.
 */
@SpringBootTest
@SpringJUnitConfig(classes = GrpcClientTestConfiguration.class)
class GrpcDicClientServiceIntegrationTest {


    @Autowired
    private GrpcDicClientService clientService;

    @Autowired
    private DicGrpcServerMock serverMock;

    @BeforeEach
    void setUp() {
        // Reset mock before each test
        serverMock.reset();
    }

    // ==================== getAllTopicsNUmber() Tests ====================
    @Test
    @DisplayName("✅ Should return all topics number")
    void getAllTopicsNumber_Success() {

        assertThat(clientService.getAllTopicsNumber())
                .isNotZero()
                .isEqualTo(10L);
    }

    // ==================== getAllTopics() Tests ====================

    @Test
    @DisplayName("✅ Should successfully retrieve all topics")
    void getAllTopics_Success() {

        assertThat(clientService.getAllTopics())
                .isNotNull()
                .hasSize(3)
                .containsExactlyInAnyOrder("Animals", "Colors", "Food");

    }

    @Test
    @DisplayName("❌ Should throw ServiceUnavailableException when server is unavailable")
    @DirtiesContext
    void getAllTopics_ServerUnavailable() {
        // Given
        serverMock.setSimulateError(true, Status.UNAVAILABLE);

        // When/Then
        assertThatThrownBy(() -> clientService.getAllTopics())
                .isInstanceOf(OrderServiceException.ServiceUnavailableException.class)
                .hasMessageContaining("Failed to process topics");
    }

    @Test
    @DisplayName("❌ Should throw TimeoutException when deadline exceeded")
    @DirtiesContext
    void getAllTopics_DeadlineExceeded() {
        // Given
        serverMock.setSimulateError(true, Status.DEADLINE_EXCEEDED);

        // When/Then
        assertThatThrownBy(() -> clientService.getAllTopics())
                .isInstanceOf(OrderServiceException.TimeoutException.class)
                .hasMessageContaining("Failed to process topics");
    }

    @Test
    @DisplayName("❌ Should throw PermissionDeniedException when permission denied")
    @DirtiesContext
    void getAllTopics_PermissionDenied() {
        // Given
        serverMock.setSimulateError(true, Status.PERMISSION_DENIED);

        // When/Then
        assertThatThrownBy(() -> clientService.getAllTopics())
                .isInstanceOf(OrderServiceException.PermissionDeniedException.class)
                .hasMessageContaining("Failed to process topics");
    }

    // ==================== getWordPairsByTopicName() Tests ====================

    @Test
    @DisplayName("✅ Should successfully retrieve word pairs for Animals topic")
    void getWordPairsByTopicName_Animals_Success() {
        // When
        Map<String, String> wordPairs = clientService.getWordPairsByTopicName("Animals");

        // Then
        assertThat(wordPairs)
                .isNotNull()
                .hasSize(2)
                .containsEntry("Dog", "Собака")
                .containsEntry("Cat", "Кошка");
    }

    @Test
    @DisplayName("✅ Should successfully retrieve word pairs for Colors topic")
    void getWordPairsByTopicName_Colors_Success() {
        // When
        Map<String, String> wordPairs = clientService.getWordPairsByTopicName("Colors");

        // Then
        assertThat(wordPairs)
                .isNotNull()
                .hasSize(3)
                .containsEntry("Red", "Красный")
                .containsEntry("Blue", "Синий")
                .containsEntry("Black", "Черный");
    }


    @Test
    @DisplayName("❌ Should throw IllegalArgumentException for null topic name")
    void getWordPairsByTopicName_NullTopicName() {
        // When/Then
        assertThatThrownBy(() -> clientService.getWordPairsByTopicName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Topic name cannot be null or empty");
    }

    @Test
    @DisplayName("❌ Should throw IllegalArgumentException for empty topic name")
    void getWordPairsByTopicName_EmptyTopicName() {
        // When/Then
        assertThatThrownBy(() -> clientService.getWordPairsByTopicName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Topic name cannot be null or empty");
    }

    @Test
    @DisplayName("❌ Should throw InvalidArgumentException for invalid topic")
    @DirtiesContext
    void getWordPairsByTopicName_InvalidTopic() {
        // When/Then
        assertThatThrownBy(() -> clientService.getWordPairsByTopicName("InvalidTopic"))
                .isInstanceOf(OrderServiceException.InvalidArgumentException.class)
                .hasMessageContaining("Failed to process word pairs");
    }

    @Test
    @DisplayName("❌ Should throw ServiceUnavailableException when server is unavailable")
    @DirtiesContext
    void getWordPairsByTopicName_ServerUnavailable() {
        // Given
        serverMock.setSimulateError(true, Status.UNAVAILABLE);

        // When/Then
        assertThatThrownBy(() -> clientService.getWordPairsByTopicName("Animals"))
                .isInstanceOf(OrderServiceException.ServiceUnavailableException.class)
                .hasMessageContaining("Failed to process word pairs");
    }

    @Test
    @DisplayName("❌ Should throw AuthenticationException when unauthenticated")
    @DirtiesContext
    void getWordPairsByTopicName_Unauthenticated() {
        // Given
        serverMock.setSimulateError(true, Status.UNAUTHENTICATED);

        // When/Then
        assertThatThrownBy(() -> clientService.getWordPairsByTopicName("Animals"))
                .isInstanceOf(OrderServiceException.AuthenticationException.class)
                .hasMessageContaining("Failed to process word pairs");
    }

    @Test
    @DisplayName("✅ Should handle default topic gracefully")
    void getWordPairsByTopicName_DefaultTopic() {
        // When
        Map<String, String> wordPairs = clientService.getWordPairsByTopicName("UnknownTopic");

        // Then
        assertThat(wordPairs)
                .isNotNull()
                .hasSize(1)
                .containsEntry("Default", "Значение по умолчанию");
    }
}
