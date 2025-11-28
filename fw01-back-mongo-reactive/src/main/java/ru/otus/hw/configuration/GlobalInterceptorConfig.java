package ru.otus.hw.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;
import ru.otus.hw.interceptor.LoggingServerInterceptor;

@Configuration
public class GlobalInterceptorConfig {

    @Bean
    @Order(100)
    @GlobalServerInterceptor
    public LoggingServerInterceptor loggingServerInterceptor() {
        return new LoggingServerInterceptor();
    }
}
