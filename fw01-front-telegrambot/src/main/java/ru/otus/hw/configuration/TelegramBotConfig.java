package ru.otus.hw.configuration;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TelegramBotProperties.class)
public class TelegramBotConfig {

    @Bean
    public TelegramBot telegramBot(TelegramBotProperties properties) {
        return new TelegramBot(properties.token());
    }
}
