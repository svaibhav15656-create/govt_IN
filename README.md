# Before You Go (India)

A website that tells a person exactly what to prepare before visiting a government office anywhere in India.

See [AGENT_BRIEF_BEFORE_YOU_GO.md](AGENT_BRIEF_BEFORE_YOU_GO.md) for the full product specification.

## Quick Start

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven (or use the included wrapper: `./mvnw`)

### Run locally

```bash
# Start PostgreSQL with PostGIS
docker compose up -d db

# Run the application
./mvnw spring-boot:run
```

Or run everything in Docker:

```bash
docker compose up --build
```

### Verify

```bash
curl http://localhost:8080/api/health
# → {"status":"UP","timestamp":"..."}
```

### Run tests

```bash
./mvnw test
```

## Project Structure

```
before-you-go/
├── src/
│   ├── main/
│   │   ├── java/in/beforeyougo/       # Application code
│   │   └── resources/
│   │       ├── application.yml         # Configuration
│   │       └── db/migration/           # Flyway migrations
│   └── test/java/in/beforeyougo/      # Tests
├── rules/                              # YAML rule files (future phases)
├── docker-compose.yml                  # App + PostgreSQL/PostGIS
├── Dockerfile                          # Multi-stage build
├── pom.xml                             # Maven build
└── .env.example                        # Environment variable template
```

## Environment Variables

Copy `.env.example` to `.env` and fill in values. See the file for documentation.

## License

TBD
