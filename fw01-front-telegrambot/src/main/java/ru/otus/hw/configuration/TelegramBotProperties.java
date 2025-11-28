package ru.otus.hw.configuration;

import jakarta.validation.constraints.NotBlank;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "telegram.bot")
@Validated
public record TelegramBotProperties(

        @NotBlank(message = "Telegram token can't be empty")
        String token,

        @NotBlank(message = "Telegram username can't be empty")
        String username,

        String webhookUrl
) {
    // Custom toString to mask sensitive data
    @NotNull
    @Override
    public String toString() {
        return "TelegramBotProperties{" +
                "token='" + maskToken(token) + '\'' +
                ", username='" + username + '\'' +
                ", webhookUrl='" + webhookUrl + '\'' +
                '}';
    }

    private static String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 5) + "***" + token.substring(token.length() - 5);
    }
}