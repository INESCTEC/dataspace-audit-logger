# AGENTS.md

## Project overview

This repository contains INESC TEC's `dataspace-audit-logger`, an implementation
of the International Data Spaces (IDS) Clearing House. It provides a trusted
audit service for recording and querying data-exchange activity between
dataspace participants.

The service:

- Registers processes associated with contract/agreement IDs.
- Stores IDS messages, finalized contracts, and data-usage notifications.
- Returns cryptographically signed receipts for accepted audit records.
- Lets agreement participants query their audit trail.
- Uses IDS Dynamic Attribute Tokens (DATs) to identify participants and enforce
  agreement-level access.
- Tracks provided and consumed EDI artifacts and their lifecycle states.

The project is maintained by INESC TEC and licensed under Apache License 2.0.
The current application version is defined in `pom.xml` (1.1.4 at the time this
file was written).

## Technology stack

- Java 21
- Spring Boot 2.7
- Maven and Maven Wrapper
- IDS Messaging Services and IDS information-model classes
- MongoDB for the IDS Clearing House audit trail
- PostgreSQL with Spring Data JPA for EDI artifact logs
- Spring Security with HTTP Basic authentication
- OpenAPI/Swagger UI
- Docker for application packaging and local MongoDB services

## Repository structure

- `src/main/java/pt/inesctec/ids/clearinghouse/`
  - `controller/`: Clearing House, logging, querying, and health endpoints.
  - `service/`: logging, query, MongoDB, and signing logic.
  - `model/`: MongoDB persistence models.
  - `repository/`: Spring Data MongoDB repositories.
  - `dto/`, `builder/`, `converter/`, `util/`: message construction,
    conversion, validation, and IDS/DAPS helpers.
  - `messaging/`: IDS message handlers.
  - `app4edi/`: EDI artifact REST API, services, JPA entities, and PostgreSQL
    repositories.
- `src/main/resources/application.properties`: default application, database,
  IDS, DAPS, security, and server configuration.
- `src/main/resources/conf/configmodel.json`: IDS connector self-description
  configuration.
- `src/test/java/`: unit and integration tests.
- `docker/mongo/docker-compose.yml`: local MongoDB and mongo-express services.
- `Dockerfile`: Java 21 runtime image for the packaged application.
- `CHANGELOG`: release history.

## Main application flow

### Logging

Clients submit multipart IDS messages to
`POST /messages/log/{agreementId}`. The request contains:

- `header`: an IDS `LogMessage`.
- `payload`: the message, contract, or usage information being audited.

`LoggingService` classifies the payload as a process, IDS message, finalized
contract, or usage log. The corresponding record is stored in MongoDB. For
audited messages, `SignService` signs a transaction containing the stored
record and requester identity, and the service returns the signed receipt.

If an agreement does not yet have a process record, the logging flow creates
one using the identity from the requester's DAT.

### Querying

Clients submit an IDS `QueryMessage` to
`POST /messages/query/{agreementId}`. `QueryService` extracts the requester
identity from its DAT and grants access only when that identity is listed as an
owner of the agreement's process. Successful queries return the stored audit
payloads as IDS log-message DTOs; unauthorized queries return an IDS rejection
message.

### EDI artifact logging

The `app4edi` package maintains separate PostgreSQL-backed logs for artifacts:

- Provided artifacts move through states such as `created`, `rejected`,
  `published`, `consumed`, and `consumed&archived`.
- Consumed artifacts move through `consumed` and `archived`.

These records include participant IDs, document metadata, file paths, and
lifecycle timestamps.

## REST endpoints

### Public and IDS endpoints

- `GET /`: public IDS connector self-description.
- `GET /health`: health check.
- `POST /process/{agreementId}`: register a process and its owners.
- `POST /messages/log/{agreementId}`: record an audit event and return a signed
  receipt.
- `POST /messages/query/{agreementId}`: retrieve an agreement's audit records.

### EDI artifact endpoints

- `POST /api/provided-edi-artifacts/update`
- `GET /api/provided-edi-artifacts/all`
- `GET /api/provided-edi-artifacts/from-participant/{participantId}`
- `DELETE /api/provided-edi-artifacts/from-participant/{participantId}`
- `POST /api/consumed-edi-artifacts/update`
- `GET /api/consumed-edi-artifacts/all`
- `GET /api/consumed-edi-artifacts/to-participant/{participantId}`
- `DELETE /api/consumed-edi-artifacts/to-participant/{participantId}`

Swagger UI is configured at `/api/docs`.

## Storage

### MongoDB

MongoDB stores process records and Clearing House messages. The default local
configuration uses:

- Host: `localhost`
- Port: `27019`
- Database: `clearinghouse`

Use `docker/mongo/docker-compose.yml` to start the supplied local MongoDB
services. It also exposes mongo-express for development inspection.

### PostgreSQL

PostgreSQL stores provided and consumed EDI artifact records. The default
configuration expects:

- URL: `jdbc:postgresql://localhost:5432/ch-app4edi`
- User: `postgres`

PostgreSQL is not included in the MongoDB Compose file and must be provided
separately. Hibernate schema handling defaults to `update`.

Do not treat credentials in `application.properties` as production-ready.
Override database credentials, security users, key locations, DAPS endpoints,
and TLS settings for deployed environments.

## Security

- The root, health, IDS message, and process endpoints are permitted by the
  HTTP security filter; IDS query authorization is performed from the DAT and
  process-owner list.
- EDI endpoints use HTTP Basic authentication.
- EDI read/update operations require `read` or `write` authority.
- EDI deletion requires `write` authority.
- The default Spring Security user has `read` authority.
- Additional users can be configured through sequential
  `Broker.userN.name`, `Broker.userN.password`, and
  `Broker.userN.authorities` properties.

Private keys, keystores, truststores, passwords, and DAPS settings are
security-sensitive. Never commit real production secrets or replace certificate
files without understanding the target environment.

## Build and run

Use the Maven Wrapper on Windows:

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

The application listens on port `8083` by default.

To run the packaged application:

```powershell
java -jar target\clearinghouse-*.jar
```

The Docker image expects a packaged `target/clearinghouse-*.jar`.

Some IDS configuration requires valid PKCS#12 keystore and truststore files.
The README notes that these files may need to be recreated after cloning:

- `src/main/resources/conf/vcese19.inesctec.pt.p12`
- `src/main/resources/conf/truststore.p12`

## Deploy

Use these two scrips to build and deploy the correspondent docker image:

```powershell
build-publish-snapshot-docker-image.sh
build-publish-versioned-docker-image.sh
```

## Testing

Start the required local databases before running integration tests. Some tests
also expect the application server to be running.

Run the test suite with:

```powershell
.\mvnw.cmd clean test
```

Tests cover controllers, services, MongoDB operations, repositories, signing,
query authorization, IDS message conversion, DTOs, and utilities.

When changing behavior:

1. Add or update focused tests near the affected package.
2. Run the smallest relevant test set during development.
3. Run the full suite when the required database services are available.
4. Do not weaken DAT authorization, receipt signing, or API authority checks to
   make tests pass.

## Implementation guidance

- Preserve the distinction between the MongoDB-backed IDS audit trail and the
  PostgreSQL-backed EDI artifact log.
- Treat the agreement ID as the correlation key for Clearing House processes
  and messages.
- Preserve IDS multipart response structure and semantic IDS message types.
- Keep timestamps and database/Jackson time handling in UTC.
- Validate changes to signing carefully; receipts cover stored content,
  timestamp, agreement ID, and client identity.
- Update OpenAPI annotations when adding or changing endpoints.
- Update `CHANGELOG` for release-facing behavior changes.
- Avoid modifying generated files under `target/`.
