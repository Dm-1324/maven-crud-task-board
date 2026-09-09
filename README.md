# Maven CRUD Task Board

A small full-stack Kanban task board built with Spring Boot, Spring Data JPA, H2, Maven, and a vanilla HTML/CSS/JavaScript frontend.

## Stack

- Java 17
- Spring Boot 3.2.5
- Spring Web
- Spring Data JPA
- H2 in-memory database
- Maven
- Vanilla JavaScript frontend
- JUnit 5 + Mockito
- JaCoCo coverage
- Docker
- GitHub Actions CI

## Run locally

Prerequisites: Java 17+ and Maven 3.9+.

```bash
mvn clean verify
mvn spring-boot:run
```

Open:

`http://localhost:8090/`

The REST API is available at `/api/tasks`.

Health check:

`http://localhost:8090/actuator/health`

H2 console:

`http://localhost:8090/h2-console`

JDBC URL: `jdbc:h2:mem:tasksdb`

User: `sa`

Password: leave blank

## API

### Get tasks

```http
GET /api/tasks
```

### Create a task

```http
POST /api/tasks
Content-Type: application/json

{
  "title": "Build CI pipeline",
  "description": "Add Jenkins after the application is stable",
  "status": "TODO"
}
```

### Change status

```http
PATCH /api/tasks/1/status
Content-Type: application/json

{
  "status": "IN_PROGRESS"
}
```

Valid statuses: `TODO`, `IN_PROGRESS`, `DONE`.

### Delete

```http
DELETE /api/tasks/1
```

## Docker

Build and run:

```bash
docker build -t maven-crud-task-board .
docker run --rm -p 8090:8090 maven-crud-task-board
```

Then open `http://localhost:8090/`.

## CI

Every push to `main` and every pull request runs `mvn clean verify` on Java 17 through GitHub Actions. The JaCoCo report is uploaded as a workflow artifact.

## Jenkins-ready

The project intentionally keeps the build Maven-based and container-friendly. A later Jenkins pipeline can use the same commands:

```bash
mvn clean verify
mvn clean package
```

and optionally:

```bash
docker build -t maven-crud-task-board .
```

## Database note

The default H2 database is in-memory, so data resets when the application stops. This keeps the project zero-configuration for development and CI. A persistent database can be introduced later through Spring profiles without changing the frontend API.
