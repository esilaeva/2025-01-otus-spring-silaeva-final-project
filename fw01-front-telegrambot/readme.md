# fw01-front-telegrambot
![Services-Diagram](Communication-Schema.svg "Services Diagram")
## Technology Stack
- Java 21
- Spring Boot 3.5.6
- Maven
- gRPC 1.74.0
- Protocol Buffers 4.31.1
- Telegram Bot API 9.2.0
- Lombok
- Spring gRPC Client

## Functional Description
Microservice frontend for Telegram bot (hw24-dic-bot). A Spring Boot application that acts as a Telegram bot frontend, communicating with a dictionary gRPC service over secure TLS mTLS channel. Fetches word pairs for interactive language quizzes, manages user quiz sessions, and handles bot commands via webhook.