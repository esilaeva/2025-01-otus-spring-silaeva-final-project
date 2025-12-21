// File: src/test/java/ru/otus/hw/configuration/GrpcClientTestConfiguration.java

package ru.otus.hw.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.otus.hw.service.DicGrpcServerMock;

/**
 * Test configuration for gRPC integration tests.
 * Sets up the in-process gRPC server with mock service implementation.
 */
@TestConfiguration
public class GrpcClientTestConfiguration {

    @Bean
    public DicGrpcServerMock mockDicServerService() {
        return new DicGrpcServerMock();
    }
}
