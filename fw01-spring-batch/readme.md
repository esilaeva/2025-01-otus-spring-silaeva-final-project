# Spring Batch YAML-MongoDB Import/Export Application
![Services-Diagram](Communication-Schema.svg "Services Diagram")
## Technology Stack

- **Java**: Version 21
- **Spring Boot**: 3.5.7
- **Spring Batch**: For batch processing operations
- **Spring Shell**: 3.4.1 (CLI interface)
- **Spring Data MongoDB**: Data access layer
- **MongoDB**: Document database (embedded for testing with external MongoDB support)
- **H2 Database**: In-memory database for Spring Batch job repository
- **Jackson YAML**: YAML serialization/deserialization
- **MapStruct**: Object mapping framework
- **Lombok**: Boilerplate code reduction
- **Mongock**: MongoDB migration framework (v5.5.1)
- **Maven**: Build and dependency management
- **JaCoCo**: Code coverage analysis
- **Checkstyle**: Code quality enforcement

## Functional Description

This Spring Boot application provides a robust batch processing solution for importing and exporting concept data between YAML files and MongoDB. The application is designed as a command-line tool using Spring Shell, making it suitable for automated data migration tasks, scheduled operations, and integration into larger data processing pipelines.

### Core Features

**Import Operations (`import-concepts` / `ic`):**
- Reads concept data from YAML files
- Processes data in configurable chunks (default: 10 items per chunk)
- Stores concepts in MongoDB with automatic ID generation
- Supports custom file paths via command-line parameters
- Provides detailed execution statistics (read/write counts)
- Includes comprehensive error handling and logging

**Export Operations (`export-concepts` / `ec`):**
- Retrieves concept data from MongoDB using cursor-based reading
- Transforms MongoDB documents to DTO format
- Exports data to YAML files with structured formatting
- Processes data in configurable chunks (default: 5 items per chunk)
- Supports custom output file paths
- Provides execution status and performance metrics

### Data Model

The application manages three main entities:

1. **Concept**: The primary entity containing:
   - Unique identifier
   - List of topics (with name and description)
   - List of words (with language, text, part of speech, comments, and audio URLs)
   - Additional comments

2. **Topic**: Represents subject areas with:
   - Name (indexed for fast lookup)
   - Description

3. **Word**: Language learning entities with:
   - Language code (ISO 639-1, indexed)
   - Text content
   - Part of speech classification
   - Comments
   - Audio pronunciation URLs

### Architecture Highlights

- **Chunk-Based Processing**: Efficient memory usage for large datasets
- **Streaming YAML Reader**: Handles large YAML files without loading everything into memory
- **Job Listeners**: Comprehensive monitoring and statistics collection
- **Transaction Management**: Ensures data consistency during batch operations
- **Database Migration**: Automated MongoDB schema management via Mongock
- **Embedded Testing**: Complete test environment with in-memory databases
- **Code Quality**: Integrated checkstyle and JaCoCo coverage reporting

### Usage Examples

```bash
# Import from default YAML file
import-concepts

# Import from custom YAML file
import-concepts --file-path /path/to/custom-concepts.yaml

# Export to default YAML file
export-concepts

# Export to custom YAML file
export-concepts --file-path /path/to/output-concepts.yaml
```

This application is ideal for language learning platforms, educational content management systems, or any scenario requiring structured import/export of concept-based data between file storage and MongoDB databases.