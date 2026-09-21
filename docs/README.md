# TodoApp

A RESTful Todo application built with Spring Boot and Spring Data JPA, backed by PostgreSQL. It provides full CRUD operations for managing todo items along with filtering by completion status.

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Configuration](#configuration)
  - [Build & Run](#build--run)
- [API Reference](#api-reference)
  - [Base URL](#base-url)
  - [Endpoints](#endpoints)
- [Data Model](#data-model)
- [Testing](#testing)
  - [Bruno Collection](#bruno-collection)
- [Error Handling](#error-handling)
- [Roadmap](#roadmap)

---

## Overview

TodoApp is a simple, well-structured example of a Spring Boot monolithic application. It follows a clean layered architecture (`Controller → Service → Repository → Entity`) with DTOs separating the public API contract from the JPA entity.

## Tech Stack

| Layer        | Technology                                              |
| ------------ | ------------------------------------------------------- |
| Language     | Java 17                                                  |
| Framework    | Spring Boot 4.1.1                                        |
| Web          | Spring Web MVC (`spring-boot-starter-webmvc`)           |
| Persistence  | Spring Data JPA (`spring-boot-starter-data-jpa`)        |
| Validation   | Bean Validation (`spring-boot-starter-validation`)      |
| Database     | PostgreSQL                                               |
| Build        | Maven (bundled `mvnw` wrapper)                           |
| API Testing  | Bruno (`.bru` collection files)                         |

## Project Structure

```
todoapp
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── docs/
│   └── README.md                        # This document
├── pipeline/
│   ├── bruno.json
│   ├── create-todo.bru                  # Bruno API requests
│   ├── delete-todo.bru
│   ├── get-all-todos.bru
│   ├── get-todo-by-id.bru
│   ├── get-todos-filtered.bru
│   ├── patch-todo.bru
│   └── update-todo.bru
├── src/
│   ├── main/
│   │   ├── java/com/darshan/todoapp/
│   │   │   ├── TodoappApplication.java
│   │   │   ├── controller/
│   │   │   │   └── TodoController.java
│   │   │   ├── dto/
│   │   │   │   ├── TodoRequest.java
│   │   │   │   └── TodoResponse.java
│   │   │   ├── entity/
│   │   │   │   └── Todo.java
│   │   │   ├── repository/
│   │   │   │   └── TodoRepository.java
│   │   │   └── service/
│   │   │       ├── TodoService.java
│   │   │       └── TodoNotFoundException.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       └── java/com/darshan/todoapp/
│           └── TodoappApplicationTests.java
├── .gitattributes
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── HELP.md
├── pom.xml
├── mvnw
└── mvnw.cmd
```

## Prerequisites

- **JDK 17** or later
- **Maven 3.9+** (or use the bundled `mvnw` wrapper)
- **PostgreSQL** running locally (default: `localhost:5432`)

## Getting Started

### Configuration

Database settings live in `../src/main/resources/application.properties`:

```properties
spring.application.name=todoapp

spring.datasource.url=jdbc:postgresql://localhost:5432/todo_db
spring.datasource.username=postgres
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Create the database before first run:

```sql
CREATE DATABASE todo_db;
```

### Build & Run

```bash
# Build (skip tests)
./mvnw clean compile

# Run tests
./mvnw test

# Start the application
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`.

---

## API Reference

### Base URL

```
http://localhost:8080/api/todos
```

### Endpoints

| Method | Endpoint               | Description                        | Request Body            | Response                 |
| ------ | ---------------------- | ---------------------------------- | ----------------------- | ------------------------ |
| GET    | `/api/todos`           | List all todos                     | —                       | `200` `TodoResponse[]`   |
| GET    | `/api/todos?completed=`| Filter by completion (`true`/`false`)| —                     | `200` `TodoResponse[]`   |
| GET    | `/api/todos/{id}`      | Get a single todo                  | —                       | `200` `TodoResponse`     |
| POST   | `/api/todos`           | Create a todo                      | `TodoRequest`           | `201` `TodoResponse`     |
| PUT    | `/api/todos/{id}`      | Full update (replace)              | `TodoRequest`           | `200` `TodoResponse`     |
| PATCH  | `/api/todos/{id}`      | Partial update                     | `TodoRequest` (partial) | `200` `TodoResponse`     |
| DELETE | `/api/todos/{id}`      | Delete a todo                      | —                       | `204 No Content`         |

### Request Body (`TodoRequest`)

| Field         | Type      | Required | Constraints                          | Description               |
| ------------- | --------- | -------- | ------------------------------------ | ------------------------- |
| `title`       | `string`  | Yes      | `@NotBlank`, max 255 chars           | Todo title                |
| `description` | `string`  | No       | —                                    | Optional details          |
| `completed`   | `boolean` | No       | —                                    | Completion flag (default `false`) |

Example:

```json
{
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "completed": false
}
```

### Response Body (`TodoResponse`)

| Field         | Type       | Description               |
| ------------- | ---------- | ------------------------- |
| `id`          | `number`   | Todo identifier           |
| `title`       | `string`   | Todo title                |
| `description` | `string`   | Optional details          |
| `completed`   | `boolean`  | Completion flag           |
| `createdAt`   | `datetime` | Creation timestamp (ISO)  |
| `updatedAt`   | `datetime` | Last update timestamp     |

Example:

```json
{
  "id": 1,
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "completed": false,
  "createdAt": "2026-09-18T12:00:00",
  "updatedAt": "2026-09-18T12:00:00"
}
```

### Example Requests

```bash
# Create a todo
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{"title":"Buy groceries","description":"Milk, eggs, bread"}'

# List all todos
curl http://localhost:8080/api/todos

# List completed todos only
curl "http://localhost:8080/api/todos?completed=true"

# Get a single todo
curl http://localhost:8080/api/todos/1

# Full update
curl -X PUT http://localhost:8080/api/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Buy groceries and snacks","description":"...","completed":true}'

# Partial update (mark completed)
curl -X PATCH http://localhost:8080/api/todos/1 \
  -H "Content-Type: application/json" \
  -d '{"completed":true}'

# Delete a todo
curl -X DELETE http://localhost:8080/api/todos/1
```

---

## Data Model

### `Todo` Entity

| Column        | Type            | Constraints                          |
| ------------- | --------------- | ------------------------------------ |
| `id`          | `bigint`        | Primary key, auto-increment (`IDENTITY`) |
| `title`       | `varchar(255)`  | Not null                             |
| `description` | `text`          | Nullable                             |
| `completed`   | `boolean`       | Not null                             |
| `created_at`  | `timestamp`     | Not null, set on persist (`@PrePersist`) |
| `updated_at`  | `timestamp`     | Not null, updated on change (`@PreUpdate`) |

`created_at` and `updated_at` are managed automatically via JPA lifecycle callbacks.

---

## Testing

### Bruno Collection

Bruno API request files (`.bru`) are stored in `../pipeline`.

| File                     | Request                          |
| ------------------------ | -------------------------------- |
| `get-all-todos.bru`      | `GET /api/todos`                 |
| `get-todos-filtered.bru` | `GET /api/todos?completed=true`  |
| `get-todo-by-id.bru`     | `GET /api/todos/1`               |
| `create-todo.bru`        | `POST /api/todos`                |
| `update-todo.bru`        | `PUT /api/todos/1`               |
| `patch-todo.bru`         | `PATCH /api/todos/1`             |
| `delete-todo.bru`        | `DELETE /api/todos/1`            |

To use: open the `pipeline` folder as a collection in Bruno and run the requests individually.

---

## Error Handling

| Status | Scenario                                    |
| ------ | ------------------------------------------- |
| `400`  | Validation failure (`@Valid` / Bean Validation) |
| `404`  | Todo not found (`TodoNotFoundException`)    |

`TodoNotFoundException` is annotated with `@ResponseStatus(HttpStatus.NOT_FOUND)` and automatically returns a 404 with a message like:

```
Todo not found with id: 1
```

---

## Roadmap

- [ ] Global exception handler (`@ControllerAdvice`) for consistent error responses
- [ ] Integration tests (`@WebMvcTest`, `@DataJpaTest`)
- [ ] Pagination & sorting for `GET /api/todos`
- [ ] API versioning (`/api/v1/todos`)
- [ ] Swagger/OpenAPI documentation
- [ ] Docker Compose for PostgreSQL + app
