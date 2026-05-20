# README Mappd

Mappd is a visualized learning tool for university courses. Lecturers structure a course into a **skill map**: a visual graph of skills and their dependencies. Students join maps to orient themselves and track personal progress. A live **Collaboration Mode** turns the map into a real-time feedback channel: students submit understanding ratings and anonymous questions, while the lecturer watches a live dashboard overlaid on the skill map.

This repository contains the Spring Boot backend. It exposes a REST API consumed by the Next.js frontend and a WebSocket endpoint for real-time collaboration features.

---

## Technologies Used

- Java 21
- Spring Boot 3
- Spring Data JPA / Hibernate
- PostgreSQL
- WebSockets (STOMP over SockJS)
- Gradle
- DiceBear API (external, for avatar generation)

---

## High-Level Components

### 1. REST Controllers
**[`src/main/java/.../controller/`](src/main/java/ch/uzh/ifi/hase/soprafs24/controller)**

One controller per domain area (users, skill maps, skills, dependencies, sessions, questions, quizzes, quiz attempts). Each controller maps HTTP requests to service calls and returns DTOs. All protected endpoints require a valid token; the `UserController` handles registration and login without authentication.

### 2. WebSocket Handler
**[`src/main/java/.../websocket/`](src/main/java/ch/uzh/ifi/hase/soprafs24/websocket)**

Manages STOMP subscriptions and broadcasts for Collaboration Mode. When a student submits an understanding rating, posts a question, or upvotes, the handler aggregates the change and pushes an update to all subscribers on the relevant session topic. This is what makes the lecturer dashboard update live without polling.

### 3. Service Layer
**[`src/main/java/.../service/`](src/main/java/ch/uzh/ifi/hase/soprafs24/service)**

Contains all business logic: session lifecycle management, skill-locking rules based on quiz pass status and dependency chains, understanding aggregation across participants, quiz attempt scoring, and export/import of skill maps. Controllers and the WebSocket handler both delegate to services.

### 4. JPA Repositories and Domain Entities
**[`src/main/java/.../repository/`](src/main/java/ch/uzh/ifi/hase/soprafs24/repository)** | **[`src/main/java/.../entity/`](src/main/java/ch/uzh/ifi/hase/soprafs24/entity)**

JPA entities: `User`, `SkillMap`, `SkillMapMembership`, `Skill`, `Dependency`, `StudentProgress`, `CollaborationSession`, `SessionParticipant`, `UnderstandingRating`, `LiveQuestion`, `UpvoteRecord`, `Quiz`, `QuizQuestion`, `QuizAnswer`, `QuizAttempt`. Repositories extend `JpaRepository` and are the only layer that touches the database.

### 5. DTOs and Mappers
**[`src/main/java/.../dto/`](src/main/java/ch/uzh/ifi/hase/soprafs24/dto)**

Request and response DTOs decouple the API contract from the domain model. Mappers convert between entities and DTOs so that internal fields (e.g. hashed passwords) are never accidentally exposed.

---

## Launch & Deployment

### Prerequisites

- Java 21+
- PostgreSQL running locally (or a connection string to a remote instance)
- Gradle (wrapper included)

### Environment / Configuration

Create or edit `src/main/resources/application.properties` (or use environment variables):

```
spring.datasource.url=jdbc:postgresql://localhost:5432/mappd
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

For production, override these via environment variables on your deployment platform.

### Run Locally

```bash
./gradlew bootRun
```

The server starts on `http://localhost:8080` by default.

### Run Tests

```bash
./gradlew test
```

Test reports are written to `build/reports/tests/test/index.html`.

### Build for Production

```bash
./gradlew build
```

This produces a runnable JAR at `build/libs/mappd-backend.jar`. Deploy it to your platform of choice (Railway, Render, etc.) and set the database environment variables there.

### Releases

Tag the commit and push:

```bash
git tag v1.x.x
git push origin v1.x.x
```

Then deploy the new JAR to your hosting platform.

---

## Roadmap

The following features would be good next contributions for new developers:

1. **Interact with user profiles:** Currently, user profiles display a username and 
   avatar but are not publicly viewable by others. A new contributor could add a 
   public profile page showing a user's joined maps and progress statistics, and 
   allow map members to view each other's profiles within a shared skill map.

2. **Duplication of maps:** Lecturers who want to reuse a skill map across semesters 
   or create a variant of an existing map currently have to rebuild it from scratch 
   (or use export/import). A duplicate feature would let any map owner clone an 
   entire map — including its skills, dependencies, and quiz questions — into a new 
   editable copy.

3. **Nested maps:** Currently, a skill node holds a description and resources but 
   cannot expand further. A new contributor could allow a skill node to reference 
   an entire child skill map, so that clicking into a node reveals its own graph 
   of sub-skills with their own dependencies and levels. This would enable 
   hierarchically structured courses — for example, a top-level map of a 
   degree programme whose nodes each unfold into individual course maps.

---

## Authors and Acknowledgment

| Name | UZH Email | GitHub |
|---|---|---|
| Chiara Wooldridge | chiara.wooldridge@uzh.ch | [@chiawld](https://github.com/chiawld) |
| Anna Gubler | anna.gubler@uzh.ch | [@anna-gubler](https://github.com/anna-gubler) |
| Elias Iskander | eliasmithanios.iskander@uzh.ch | [@elsithewizzard](https://github.com/elsithewizzard) |
| Sebastian Huber | sebastian.huber2@uzh.ch | [@sebdahub](https://github.com/sebdahub) |
| Hadia Aslam | hadia.aslam@uzh.ch | [@haslam](https://github.com/haslam) |

Built as part of the Software Engineering Lab (SoPra FS26), Department of Informatics, University of Zurich.

Thanks to the [DiceBear API](https://www.dicebear.com/) for avatar generation, and to the SoPra teaching team for the project template.

---

## License

[MIT License](LICENSE)