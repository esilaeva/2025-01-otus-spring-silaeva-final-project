package ru.otus.hw.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ChannelBuilderOptions;
import org.springframework.grpc.client.GrpcChannelFactory;
import ru.otus.hw.interceptor.LoggingClientInterceptor;
import ru.otus.hw.proto.DicServiceGrpc;

import java.util.List;

@Configuration
@Slf4j
public class GrpcDicClientConfig {

    @Bean
    DicServiceGrpc.DicServiceStub stub(GrpcChannelFactory channelFactory) {
        log.info("✅ Creating DicService non-blocking stub with logging interceptor");
        ChannelBuilderOptions options = ChannelBuilderOptions.defaults()
                .withInterceptors(List.of(new LoggingClientInterceptor()));

        return DicServiceGrpc.newStub(channelFactory.createChannel("dic-channel", options));
    }

}


