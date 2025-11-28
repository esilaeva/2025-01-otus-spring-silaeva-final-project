# fw01-back-mongo-reactive

![Services-Diagram](Communication-Schema.svg "Services Diagram")

## Technology Stack
- Java 21
- Spring Boot 3.5.6 (Reactive)
- Spring Data MongoDB Reactive
- Project Reactor
- gRPC (Spring gRPC 0.11.0 with mTLS)
- MongoDB (with embedded for tests)
- Mongock 4.3.8 (DB migrations)
- MapStruct 1.6.3 (model to DTO mapping)
- Lombok
- Maven
- JaCoCo (coverage)
- Checkstyle

## Functional Description
Reactive backend microservice for a Hebrew-Russian dictionary service. Exposes gRPC API on port 9090 (mTLS secured) via DicService:

- `getTopics(Empty)` → stream `StringValue` (all topics sorted)
- `getWordPairs(StringValue topic)` → stream `WordPair` (Hebrew→Russian maps)
- `getAllTopicsNumber(Empty)` → `Int64Value` (topics count)

Data managed reactively with MongoDB repositories, services (TopicService, WordService), and Mongock changelog for seeding.