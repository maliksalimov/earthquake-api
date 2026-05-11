# 🌍 Earthquake API

A RESTful API for querying and managing real earthquake data with JWT authentication, Redis caching, and full Swagger documentation.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-6DB33F?style=flat&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=flat&logo=redis&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat)

Serves 10,938 real seismic events sourced from the USGS Earthquake Hazards Program. The API supports full CRUD operations with role-based access control, paginated responses, and Redis-backed caching. Authentication is handled via stateless JWT tokens.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Authentication Flow](#authentication-flow)
- [Pagination & Sorting](#pagination--sorting)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Redis Cache](#redis-cache)
- [Project Structure](#project-structure)
- [Author](#author)
- [Acknowledgments](#acknowledgments)

---

## Features

### Data Management
- 10,938 real USGS earthquake records loaded automatically on first startup
- CSV parsed with Apache Commons CSV — handles quoted fields and edge cases correctly
- H2 file-based persistence — data survives application restarts
- Idempotent loader — skips CSV import if records already exist

### API Backend
- JWT authentication with BCrypt password hashing
- Full CRUD for earthquake records
- Role-based access control: `GET` endpoints are public, `POST` / `PUT` / `DELETE` require `ADMIN` role
- Pagination with a hard cap of 20 items per page
- Redis cache for list and single-record endpoints with automatic invalidation on writes
- Interactive Swagger UI and Postman collection

---

## Tech Stack

| Category | Technology |
|---|---|
| Framework | Spring Boot 3.4.1 |
| Language | Java 17 |
| Security | Spring Security + JWT (jjwt 0.12.3) |
| Database | H2 (file-based) |
| ORM | Spring Data JPA + Hibernate |
| Cache | Redis with Jackson JSR310 serialization |
| Documentation | Springdoc OpenAPI 2.7.0 |
| CSV Parsing | Apache Commons CSV 1.10.0 |
| Build | Gradle |

---

## Getting Started

### Prerequisites

- Java 17+
- Gradle 7+
- Redis *(optional — the application starts without it; caching is simply skipped)*

### Installation

```bash
# Clone the repository
git clone <repo-url>
cd earthquake-api

# Build
./gradlew clean build

# Run
./gradlew bootRun
```

The application starts on **http://localhost:8080**.

### First-Time Startup Behavior

On the very first run, the following happens automatically:

1. JPA creates the `EARTHQUAKES` and `USERS` tables via `ddl-auto: update`
2. H2 database file is created at `./data/earthquakedb`
3. `DataLoader` reads `earthquakes.csv` from the classpath and inserts all 10,938 records
4. Progress is logged every 100 records — expect a few seconds for the full load

On subsequent restarts the loader detects existing records and skips the import.

---

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user | None |
| `POST` | `/api/auth/login` | Login and receive a JWT | None |

### Earthquakes

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/earthquake` | Paginated list of all earthquakes | None |
| `GET` | `/api/earthquake/{id}` | Single earthquake by ID | None |
| `POST` | `/api/earthquake` | Create a new earthquake record | ADMIN |
| `PUT` | `/api/earthquake/{id}` | Update an existing record | ADMIN |
| `DELETE` | `/api/earthquake/{id}` | Delete a record | ADMIN |

---

## Authentication Flow

### 1. Register a user

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "test123"}'
```

### 2. Promote to ADMIN (via H2 Console)

Open http://localhost:8080/h2-console, connect with the credentials below, then run:

```sql
UPDATE USERS SET ROLE = 'ADMIN' WHERE USERNAME = 'testuser';
```

### 3. Login and get your token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "test123"}'
```

Response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 4. Use the token in protected requests

```bash
curl -X POST http://localhost:8080/api/earthquake \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "magnitude": 4.5,
    "location": "15 km NE of Sample City, CA",
    "latitude": 37.4,
    "longitude": -122.1,
    "depth": 10.0,
    "time": "2024-01-15T08:30:00",
    "tsunamiAlert": "No",
    "type": "earthquake"
  }'
```

---

## Pagination & Sorting

All list requests support the following query parameters:

| Parameter | Type | Default | Description |
|---|---|---|---|
| `page` | `int` | `0` | Page number (0-indexed) |
| `size` | `int` | `20` | Items per page (max 20 — larger values are clamped) |
| `sort` | `string` | `id,desc` | Field and direction, comma-separated |

**Example — most recent large earthquakes:**

```
GET /api/earthquake?page=0&size=10&sort=magnitude,desc
```

**Example response:**

```json
{
  "content": [
    {
      "id": 42,
      "magnitude": 6.8,
      "location": "150 km SW of Tonga",
      "latitude": -21.5,
      "longitude": -174.3,
      "depth": 35.0,
      "time": "2024-03-12T14:22:10",
      "tsunamiAlert": "Yes",
      "type": "earthquake"
    }
  ],
  "totalElements": 10938,
  "totalPages": 547,
  "size": 20,
  "number": 0
}
```

---

## API Documentation

### Swagger UI (Interactive)

```
http://localhost:8080/swagger-ui/index.html
```

All endpoints are documented with request/response schemas. Protected endpoints can be tested by clicking **Authorize** and entering `Bearer <token>`.

### Postman Collection

Full collection with pre-configured requests and environment variables:

https://documenter.getpostman.com/view/45700405/2sBXqNkdGs

### H2 Console

```
URL:      http://localhost:8080/h2-console
JDBC URL: jdbc:h2:file:./data/earthquakedb
Username: sa
Password: (leave blank)
```

---

## Database Schema

```sql
CREATE TABLE users (
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL
);

CREATE TABLE earthquakes (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    magnitude     DOUBLE       NOT NULL,
    location      VARCHAR(255) NOT NULL,
    latitude      DOUBLE       NOT NULL,
    longitude     DOUBLE       NOT NULL,
    depth         DOUBLE       NOT NULL,
    time          TIMESTAMP    NOT NULL,
    tsunami_alert VARCHAR(10),
    type          VARCHAR(50)
);
```

*Tables are created automatically by Hibernate on startup — no manual migration needed.*

---

## Redis Cache

Caching is applied at the service layer using Spring Cache annotations:

| Cache name | Key | Invalidated on |
|---|---|---|
| `earthquakes` | `{page}-{size}-{sort}` | Any `POST`, `PUT`, or `DELETE` |
| `earthquake` | `{id}` | `PUT /{id}` or `DELETE /{id}` |

- TTL: **1 hour**
- Serialization: Jackson with `JavaTimeModule` (handles `LocalDateTime` correctly)
- The application starts and runs normally if Redis is unavailable — caching is silently bypassed

---

## Project Structure

```
src/main/java/com/malik/earthquakeapi/
├── config/
│   ├── OpenApiConfig.java
│   ├── RedisConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   └── EarthquakeController.java
├── dto/
│   ├── AuthResponse.java
│   ├── CreateEarthquakeRequest.java
│   ├── EarthquakeResponse.java
│   ├── ErrorResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── UpdateEarthquakeRequest.java
├── entity/
│   ├── Earthquake.java
│   ├── Role.java
│   └── User.java
├── exception/
│   ├── DuplicateResourceException.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── loader/
│   └── DataLoader.java
├── repository/
│   ├── EarthquakeRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
└── service/
    ├── AuthService.java
    ├── EarthquakeService.java
    └── JwtService.java
```

---

## Author

**Malik Salimov**  
Java Backend Developer  
Baku, Azerbaijan  
Qwasar/DoCode Season 03 — Fullstack Java Program

---

## Acknowledgments

- [USGS Earthquake Hazards Program](https://earthquake.usgs.gov/) — earthquake data source
- [jjwt](https://github.com/jwtk/jjwt) — Java JWT library
- [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/) — CSV parsing
