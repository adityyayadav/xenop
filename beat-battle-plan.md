# Beat Battle — Full Project Plan
> From idea to deployed product. Spring Boot backend, React frontend, $0 deployment.
> Use this document as your master reference. Update it as you go.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Core Concepts to Understand](#2-core-concepts-to-understand)
3. [Full Tech Stack](#3-full-tech-stack)
4. [System Architecture](#4-system-architecture)
5. [Database Schema](#5-database-schema)
6. [API Design](#6-api-design)
7. [Room State Machine](#7-room-state-machine)
8. [WebSocket Design](#8-websocket-design)
9. [File Upload Pipeline](#9-file-upload-pipeline)
10. [Phase-by-Phase Build Plan](#10-phase-by-phase-build-plan)
11. [Deployment Guide](#11-deployment-guide)
12. [What to Put on Your Resume](#12-what-to-put-on-your-resume)
13. [Interview Prep](#13-interview-prep)

---

## 1. Project Overview

### What it is
A real-time creative battle platform. A host creates a "battle room" with a theme, a time limit, and a shared asset pack (audio samples, reference images, etc). Participants join, produce something within the time limit, upload their submission, then the community votes. Rankings update on a global leaderboard.

### Who it's for
- Beat producers (FL Studio, Ableton, GarageBand communities)
- Digital artists
- Writers
- Any creative community that runs manual battles on Reddit/Discord today

### Why it exists
Communities like r/WeAreTheMusicMakers and r/learnart run beat/art battles manually — sharing Google Drive links and running polls in comments. There is no dedicated open platform for this. You are building the infrastructure they are missing.

### MVP scope (what you ship first)
- User registration and login with JWT
- Create a battle room with theme, time limit, category
- Upload an asset pack to the room
- Room moves through states: WAITING → ACTIVE → VOTING → CLOSED automatically
- Participants submit files during ACTIVE phase
- Users vote on submissions during VOTING phase
- Results and leaderboard after CLOSED

### What you are NOT building in MVP
- Real-time WebSockets (V2)
- Comments (V2)
- Email notifications (V2)
- Bracket/tournament system (V3)
- Mobile app (never)
- Any payment system (never)

---

## 2. Core Concepts to Understand

Learn each of these before or during the phase that uses them. Do not skip this section — every item here is something you should be able to explain out loud.

### 2.1 Auth and Security
**Topics to learn:**
- How JWT (JSON Web Token) works — header, payload, signature
- Access token vs refresh token — why you need both
- How Spring Security filter chain works
- What bcrypt is and why you never store plain passwords
- What CORS is and why your frontend will fail without configuring it

**Resources:**
- JWT.io — read the introduction fully
- Spring Security docs — "Servlet Authentication Architecture"
- YouTube: "Spring Boot 3 JWT Authentication" by Amigoscode
- Read the jjwt library README on GitHub

**What you should be able to explain after:**
- Why JWTs are stateless and what that means for your server
- What happens if someone steals an access token
- Why refresh tokens exist and how rotation works

---

### 2.2 Database Design and JPA
**Topics to learn:**
- Entity relationships: @OneToMany, @ManyToOne, @ManyToMany
- What LAZY vs EAGER loading means and when each causes problems
- What the N+1 query problem is and how to fix it with @EntityGraph or JOIN FETCH
- What a database constraint is (UNIQUE, NOT NULL, CHECK, FOREIGN KEY)
- What a database index is and when you should add one
- What Flyway migration is and why you use it instead of hibernate ddl-auto=create

**Resources:**
- Vlad Mihalcea's blog — "The best way to map a @OneToMany" (read this carefully)
- Baeldung — "Hibernate One To Many Annotation Tutorial"
- Flyway docs — Getting Started
- YouTube: "JPA / Hibernate Tutorial" by Thorben Janssen

**What you should be able to explain after:**
- Why you use LAZY loading by default
- What happens when you access a LAZY collection outside a transaction
- Why you add an index on foreign keys and frequently queried columns

---

### 2.3 File Storage with Cloudflare R2
**Topics to learn:**
- What object storage is (S3 model — bucket, key, object)
- What a presigned URL is and how it works
- The difference between server-side upload and client-side direct upload
- What content-type validation means and why you enforce it server-side
- What the AWS SDK is and why R2 is compatible with it

**Resources:**
- Cloudflare R2 docs — "Getting Started"
- AWS S3 docs — "Presigned URLs" (same concept, same SDK)
- Baeldung — "AWS S3 with Java"

**What you should be able to explain after:**
- Why you use presigned URLs instead of routing files through your backend
- What stops a user from uploading a .exe disguised as an .mp3
- How you would clean up files when a battle room is deleted

---

### 2.4 Room State Machine
**Topics to learn:**
- What a finite state machine (FSM) is — states, transitions, guards
- What Spring @Scheduled is and how to use it for time-based transitions
- What optimistic locking is and why you need it for concurrent state transitions
- What @Version does in JPA

**Resources:**
- Wikipedia — "Finite-state machine" (read the intro section)
- Spring docs — "Task Execution and Scheduling"
- Baeldung — "Optimistic Locking in JPA"
- Optional: Spring State Machine project (good to know it exists, don't use it for MVP)

**What you should be able to explain after:**
- Why you can't just check system time on the client to control state
- What happens if two requests try to transition the same room simultaneously
- How @Scheduled works and what happens if the server restarts mid-battle

---

### 2.5 WebSockets and STOMP (V2)
**Topics to learn:**
- What WebSocket is and how it differs from HTTP (persistent vs request-response)
- What STOMP is — a messaging protocol on top of WebSocket
- How Spring's SimpMessagingTemplate broadcasts to topics
- What a message broker is in this context
- How the frontend connects with SockJS + @stomp/stompjs

**Resources:**
- Spring docs — "WebSocket Support"
- Baeldung — "Intro to WebSockets with Spring"
- YouTube: "Spring Boot WebSocket + STOMP Tutorial" by Amigoscode
- SockJS GitHub README

**What you should be able to explain after:**
- Why you use STOMP on top of raw WebSocket
- How you authenticate a WebSocket connection
- What happens when a user disconnects mid-battle

---

### 2.6 Redis with Spring
**Topics to learn:**
- What Redis is — in-memory key-value store
- What Spring Data Redis is and how to use RedisTemplate
- What cache-aside pattern is
- What TTL (time-to-live) is on a Redis key
- How to use Redis for rate limiting (token bucket / sliding window)

**Resources:**
- Upstash docs — Getting Started with Redis
- Baeldung — "Spring Data Redis Tutorial"
- Baeldung — "Rate Limiting with Spring and Redis"

**What you should be able to explain after:**
- Why leaderboard data is a good candidate for caching
- What happens when Redis goes down — does your app crash?
- Why you set a TTL on cached data

---

### 2.7 Voting and Ranking Systems
**Topics to learn:**
- How to prevent double voting with database constraints
- What a materialized view is in PostgreSQL
- How to calculate a leaderboard with aggregated stats
- What Elo rating is (good to know, don't implement in MVP)

**Resources:**
- PostgreSQL docs — "Materialized Views"
- Baeldung — "Spring Data JPA Aggregations"

**What you should be able to explain after:**
- How you prevent a user from voting on their own submission
- How you prevent a user from voting twice on the same submission
- How you calculate win rate efficiently without scanning all votes every time

---

## 3. Full Tech Stack

### Backend
| Technology | Purpose | Version |
|---|---|---|
| Java | Language | 21 (LTS) |
| Spring Boot | Framework | 3.3.x |
| Spring Security | Auth + JWT filter | included |
| Spring Data JPA | ORM / database access | included |
| Spring WebSocket | Real-time rooms (V2) | included |
| Spring Data Redis | Cache + rate limiting | included |
| Spring Scheduling | Room state transitions | included |
| Flyway | Database migrations | 10.x |
| jjwt | JWT library | 0.12.x |
| AWS SDK v2 | Cloudflare R2 (S3-compatible) | 2.x |
| MapStruct | DTO mapping | 1.5.x |
| Lombok | Boilerplate reduction | 1.18.x |
| Testcontainers | Integration testing | 1.19.x |

### Database and Storage
| Service | Purpose | Free Tier |
|---|---|---|
| PostgreSQL 16 | Primary database | Via Neon — 0.5GB free |
| Cloudflare R2 | File storage | 10GB + 1M requests/month free |
| Upstash Redis | Cache + rate limiting | 10,000 commands/day free |

### Frontend
| Technology | Purpose |
|---|---|
| React 18 | UI framework |
| Vite | Build tool |
| Tailwind CSS | Styling |
| React Query (TanStack Query) | Server state, API calls |
| React Router v6 | Client-side routing |
| @stomp/stompjs + SockJS | WebSocket client (V2) |
| React Hook Form | Form handling |

### Deployment
| Service | What runs there | Cost |
|---|---|---|
| Render | Spring Boot backend | $0 (free tier) |
| Neon | PostgreSQL | $0 (free tier) |
| Upstash | Redis | $0 (free tier) |
| Cloudflare R2 | Audio/image files | $0 (free tier) |
| Vercel | React frontend | $0 (free tier) |

---

## 4. System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     React Frontend                       │
│              (Vercel — vercel.app domain)                │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTPS REST + WS
┌──────────────────────▼──────────────────────────────────┐
│                  Spring Boot Backend                     │
│               (Render — onrender.com)                    │
│                                                          │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │ Auth Filter │  │ REST Controllers│  │ WS Handler(V2)│  │
│  └─────────────┘  └──────────────┘  └────────────────┘  │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │  Services   │  │  Schedulers  │  │  R2 Client     │  │
│  └─────────────┘  └──────────────┘  └────────────────┘  │
└──────┬──────────────────┬──────────────────┬────────────┘
       │                  │                  │
┌──────▼──────┐  ┌────────▼──────┐  ┌───────▼────────┐
│  PostgreSQL │  │  Redis Cache  │  │ Cloudflare R2  │
│   (Neon)    │  │  (Upstash)    │  │  (file store)  │
└─────────────┘  └───────────────┘  └────────────────┘
```

### Request flow for a typical action
1. React sends HTTP request with `Authorization: Bearer <token>` header
2. Spring Security JWT filter intercepts, validates token, sets SecurityContext
3. Controller receives request, calls Service layer
4. Service applies business logic, calls Repository (JPA) or R2 client
5. Repository queries PostgreSQL via JPA
6. Service returns DTO to Controller
7. Controller sends JSON response to React

### File upload flow
1. React requests a presigned upload URL from backend
2. Backend generates presigned URL pointing to R2, returns it to React
3. React uploads file directly to R2 using presigned URL (bypasses backend)
4. React notifies backend with the file key after upload succeeds
5. Backend stores file key in database linked to the submission

---

## 5. Database Schema

### Users table
```sql
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,  -- bcrypt hash
    bio         TEXT,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
```

### Rooms table
```sql
CREATE TABLE rooms (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    host_id         UUID NOT NULL REFERENCES users(id),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    theme           VARCHAR(255),
    category        VARCHAR(50) NOT NULL DEFAULT 'MUSIC',  -- MUSIC, ART, WRITING, OTHER
    status          VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    -- WAITING, ACTIVE, VOTING, CLOSED
    max_participants INT DEFAULT 50,
    submission_limit INT DEFAULT 1,  -- submissions per user
    active_duration_minutes   INT NOT NULL,  -- how long submission phase lasts
    voting_duration_minutes   INT NOT NULL,  -- how long voting phase lasts
    battle_config   JSONB DEFAULT '{}', -- config for extensibility
    active_starts_at  TIMESTAMP WITH TIME ZONE,  -- set when host starts battle
    voting_starts_at  TIMESTAMP WITH TIME ZONE,  -- computed from active_starts_at
    closes_at         TIMESTAMP WITH TIME ZONE,  -- computed from voting_starts_at
    version           INT DEFAULT 0,  -- for optimistic locking
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_rooms_status ON rooms(status);
CREATE INDEX idx_rooms_host_id ON rooms(host_id);
CREATE INDEX idx_rooms_closes_at ON rooms(closes_at);
```

### Sample Library table
```sql
CREATE TABLE sample_library (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,          -- "808 Kick", "Lo-fi Piano Loop"
    file_key    VARCHAR(500) NOT NULL,          -- R2 object key
    file_type   VARCHAR(100) NOT NULL,          -- MIME type
    file_size   BIGINT NOT NULL,
    category    VARCHAR(50) NOT NULL,           -- DRUMS, BASS, MELODY, VOCALS, FX, ONE_SHOT
    tags        TEXT[],                          -- PostgreSQL array: {'trap', 'dark', 'heavy'}
    bpm         INT,                            -- nullable, only relevant for loops
    key_signature VARCHAR(10),                  -- nullable, "Cm", "F#m"
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_sample_library_category ON sample_library(category);
CREATE INDEX idx_sample_library_tags ON sample_library USING GIN(tags);
```

### Asset packs table
```sql
CREATE TABLE asset_packs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id     UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    file_name   VARCHAR(255) NOT NULL,
    file_key    VARCHAR(500) NOT NULL,  -- R2 object key
    file_type   VARCHAR(100) NOT NULL,  -- MIME type
    file_size   BIGINT NOT NULL,        -- bytes
    source      VARCHAR(20) NOT NULL DEFAULT 'HOST_UPLOAD',
    -- 'HOST_UPLOAD' = host uploaded this
    -- 'SYSTEM_RANDOM' = pulled from sample_library
    source_sample_id UUID REFERENCES sample_library(id),
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_asset_packs_room_id ON asset_packs(room_id);
```

### Participants table
```sql
CREATE TABLE participants (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id     UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id),
    joined_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(room_id, user_id)  -- user can only join a room once
);

CREATE INDEX idx_participants_room_id ON participants(room_id);
CREATE INDEX idx_participants_user_id ON participants(user_id);
```

### Submissions table
```sql
CREATE TABLE submissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id         UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id),
    title           VARCHAR(255),
    description     TEXT,
    file_key        VARCHAR(500) NOT NULL,  -- R2 object key
    file_name       VARCHAR(255) NOT NULL,
    file_type       VARCHAR(100) NOT NULL,
    file_size       BIGINT NOT NULL,
    submitted_at    TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(room_id, user_id)  -- one submission per user per room
);

CREATE INDEX idx_submissions_room_id ON submissions(room_id);
CREATE INDEX idx_submissions_user_id ON submissions(user_id);
```

### Votes table
```sql
CREATE TABLE votes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    submission_id   UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    voter_id        UUID NOT NULL REFERENCES users(id),
    score           INT NOT NULL CHECK (score BETWEEN 1 AND 5),
    voted_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(submission_id, voter_id)  -- one vote per submission per voter
);

CREATE INDEX idx_votes_submission_id ON votes(submission_id);
CREATE INDEX idx_votes_voter_id ON votes(voter_id);
```

### Leaderboard view
```sql
CREATE MATERIALIZED VIEW leaderboard AS
SELECT
    u.id AS user_id,
    u.username,
    COUNT(DISTINCT p.room_id) AS battles_entered,
    COUNT(DISTINCT CASE WHEN ranked.rank = 1 THEN ranked.room_id END) AS wins,
    ROUND(
        COUNT(DISTINCT CASE WHEN ranked.rank = 1 THEN ranked.room_id END)::numeric /
        NULLIF(COUNT(DISTINCT p.room_id), 0) * 100, 1
    ) AS win_rate,
    COALESCE(SUM(ranked.avg_score), 0) AS total_score
FROM users u
LEFT JOIN participants p ON p.user_id = u.id
LEFT JOIN (
    SELECT
        s.user_id,
        s.room_id,
        AVG(v.score) AS avg_score,
        RANK() OVER (PARTITION BY s.room_id ORDER BY AVG(v.score) DESC) AS rank
    FROM submissions s
    JOIN votes v ON v.submission_id = s.id
    GROUP BY s.user_id, s.room_id
) ranked ON ranked.user_id = u.id
GROUP BY u.id, u.username
ORDER BY wins DESC, win_rate DESC;

CREATE UNIQUE INDEX idx_leaderboard_user_id ON leaderboard(user_id);
```

Refresh this view after each battle closes:
```sql
REFRESH MATERIALIZED VIEW CONCURRENTLY leaderboard;
```

---

## 6. API Design

### Auth endpoints
```
POST   /api/auth/register       Register new user
POST   /api/auth/login          Login, returns access + refresh token
POST   /api/auth/refresh        Get new access token using refresh token
POST   /api/auth/logout         Invalidate refresh token
```

### User endpoints
```
GET    /api/users/me            Get current user profile
PUT    /api/users/me            Update profile (bio, username)
GET    /api/users/{username}    Get public profile
GET    /api/users/{id}/stats    Get battle stats for a user
```

### Room endpoints
```
GET    /api/rooms               List rooms (filter by status, category, page)
POST   /api/rooms               Create a new room [AUTH]
GET    /api/rooms/{id}          Get room details
DELETE /api/rooms/{id}          Delete room (host only) [AUTH]
POST   /api/rooms/{id}/start    Start the battle — move to ACTIVE [AUTH, host only]
POST   /api/rooms/{id}/join     Join a room as participant [AUTH]
GET    /api/rooms/{id}/participants   List participants
```

### Asset pack endpoints
```
POST   /api/rooms/{id}/assets/upload-url   Get presigned upload URL [AUTH, host]
POST   /api/rooms/{id}/assets/confirm      Confirm upload, save to DB [AUTH, host]
POST   /api/rooms/{id}/assets/random       Pull random samples from library [AUTH, host]
GET    /api/rooms/{id}/assets              List assets with download URLs
DELETE /api/rooms/{id}/assets/{assetId}    Delete an asset [AUTH, host]
```

### Sample Library endpoints
```
GET    /api/sample-library                 Browse available library samples
GET    /api/sample-library/categories      List available categories
```

### Submission endpoints
```
POST   /api/rooms/{id}/submissions/upload-url   Get presigned upload URL [AUTH]
POST   /api/rooms/{id}/submissions/confirm      Confirm upload, save to DB [AUTH]
GET    /api/rooms/{id}/submissions              List submissions for a room
GET    /api/rooms/{id}/submissions/{subId}      Get single submission
DELETE /api/rooms/{id}/submissions/{subId}      Delete own submission [AUTH]
```

### Voting endpoints
```
POST   /api/submissions/{id}/vote       Cast a vote [AUTH]
PUT    /api/submissions/{id}/vote       Update vote [AUTH]
DELETE /api/submissions/{id}/vote       Remove vote [AUTH]
GET    /api/rooms/{id}/results          Get final results after CLOSED
```

### Leaderboard endpoints
```
GET    /api/leaderboard                 Global leaderboard (paginated)
GET    /api/leaderboard?category=MUSIC  Filtered by category
```

### WebSocket topics (V2)
```
/topic/rooms/{id}/events    Room-level events (participant joined, submission uploaded)
/topic/rooms/{id}/timer     Timer tick events
/app/rooms/{id}/ping        Client heartbeat
```

---

## 7. Room State Machine

This is the most important part of the backend. Understand it deeply.

### States
```
WAITING  → Room created, not started yet. Host can add assets, participants can join.
ACTIVE   → Battle is live. Submissions accepted. Timer running.
VOTING   → Submission phase closed. Voting open. No new submissions.
CLOSED   → Voting closed. Results final. Leaderboard updated.
```

### Transitions
```
WAITING  --[host triggers /start]--> ACTIVE
ACTIVE   --[active_duration elapsed]--> VOTING   (automatic, server-side)
VOTING   --[voting_duration elapsed]--> CLOSED   (automatic, server-side)
```

### Implementation with Spring @Scheduled

```java
// Runs every 30 seconds, finds rooms that need transitioning
@Scheduled(fixedDelay = 30000)
@Transactional
public void processRoomTransitions() {
    // ACTIVE → VOTING
    List<Room> expiredActive = roomRepository
        .findByStatusAndVotingStartsAtBefore(RoomStatus.ACTIVE, Instant.now());
    expiredActive.forEach(this::transitionToVoting);

    // VOTING → CLOSED
    List<Room> expiredVoting = roomRepository
        .findByStatusAndClosesAtBefore(RoomStatus.VOTING, Instant.now());
    expiredVoting.forEach(this::transitionToClosed);
}

private void transitionToVoting(Room room) {
    room.setStatus(RoomStatus.VOTING);
    roomRepository.save(room);
    // V2: broadcast WS event to room topic
}

private void transitionToClosed(Room room) {
    room.setStatus(RoomStatus.CLOSED);
    roomRepository.save(room);
    refreshLeaderboard();
    // V2: broadcast WS event to room topic
}
```

### Optimistic locking
The `version` column on the Room entity prevents two scheduler runs from transitioning the same room simultaneously:

```java
@Entity
public class Room {
    @Version
    private Integer version;
    // ...
}
```

If two transactions try to update the same room, the second one will get an `OptimisticLockException` and roll back. This is safe — the room was already transitioned.

---

## 8. WebSocket Design (V2 — build after MVP)

### Architecture
```
Client                    Server
  |                         |
  |-- WS CONNECT ---------->|
  |<- CONNECTED ------------|
  |                         |
  |-- SUBSCRIBE             |
  |   /topic/rooms/{id} --->|
  |                         |
  |   (someone joins room)  |
  |<-- MESSAGE              |
  |    {type: PARTICIPANT_JOINED, count: 5}
  |                         |
  |   (submission uploaded) |
  |<-- MESSAGE              |
  |    {type: SUBMISSION_UPLOADED, count: 3}
  |                         |
  |   (timer tick)          |
  |<-- MESSAGE              |
  |    {type: TIMER, secondsLeft: 300}
```

### Event types
```java
public enum RoomEventType {
    PARTICIPANT_JOINED,
    PARTICIPANT_LEFT,
    SUBMISSION_UPLOADED,
    VOTING_OPENED,
    BATTLE_CLOSED,
    TIMER_UPDATE
}
```

### Connection Manager
```java
@Component
public class RoomConnectionManager {
    // roomId -> set of session IDs
    private final Map<UUID, Set<String>> roomSessions = new ConcurrentHashMap<>();

    public void addSession(UUID roomId, String sessionId) { ... }
    public void removeSession(UUID roomId, String sessionId) { ... }
    public int getSessionCount(UUID roomId) { ... }
}
```

### Auth for WebSocket
Pass JWT as a query param on connect: `ws://server/ws?token=<jwt>`
Server validates in a `ChannelInterceptor` before allowing subscription.

---

## 9. File Upload Pipeline

### Why presigned URLs
If you route file uploads through your backend:
- Large files block your server threads
- Render free tier has bandwidth limits
- Slower for the user (file travels backend → R2 instead of directly)

With presigned URLs, the file goes directly from the user's browser to R2. Your backend only handles metadata.

### Upload flow in detail

**Step 1 — Frontend requests upload URL**
```
POST /api/rooms/{id}/assets/upload-url
Body: { "fileName": "drums.wav", "fileType": "audio/wav", "fileSize": 4500000 }
```

**Step 2 — Backend validates and generates URL**
```java
// Validate file type
Set<String> ALLOWED_TYPES = Set.of("audio/mpeg", "audio/wav", "audio/flac", "image/jpeg", "image/png");
if (!ALLOWED_TYPES.contains(fileType)) throw new InvalidFileTypeException();

// Validate file size (50MB max)
if (fileSize > 50 * 1024 * 1024) throw new FileTooLargeException();

// Generate unique key
String fileKey = "rooms/" + roomId + "/assets/" + UUID.randomUUID() + "/" + fileName;

// Generate presigned URL (valid for 5 minutes)
PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(r ->
    r.putObjectRequest(p -> p.bucket(bucket).key(fileKey).contentType(fileType))
     .signatureDuration(Duration.ofMinutes(5)));

return new PresignedUrlResponse(fileKey, presigned.url().toString());
```

**Step 3 — Frontend uploads directly to R2**
```javascript
await fetch(presignedUrl, {
  method: 'PUT',
  body: file,
  headers: { 'Content-Type': file.type }
});
```

**Step 4 — Frontend confirms to backend**
```
POST /api/rooms/{id}/assets/confirm
Body: { "fileKey": "rooms/abc/assets/xyz/drums.wav", "fileName": "drums.wav", ... }
```

**Step 5 — Backend saves metadata to database**

### R2 configuration in Spring Boot
```java
@Bean
public S3Client r2Client() {
    return S3Client.builder()
        .endpointOverride(URI.create("https://<accountid>.r2.cloudflarestorage.com"))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(r2AccessKey, r2SecretKey)))
        .region(Region.of("auto"))
        .build();
}
```

---

## 10. Phase-by-Phase Build Plan

### Before you write any code — project setup (2 days)

**Learn first:**
- Read Spring Initializr docs — understand what each dependency does before adding it
- Read Neon quickstart — set up your free Postgres database
- Read Flyway getting started — understand migration files

**Do:**
1. Create GitHub repo — `beat-battle` (public)
2. Create `docs/` folder — put this plan file there
3. Create `decisions/` folder — you'll log architecture decisions here
4. Go to start.spring.io and generate project with:
   - Spring Web
   - Spring Security
   - Spring Data JPA
   - Spring Data Redis
   - Flyway Migration
   - Lombok
   - Validation
   - PostgreSQL Driver
5. Set up Neon — create database, copy connection string
6. Set up Upstash — create Redis database, copy connection string
7. Configure `application.properties` with all connection strings
8. Write your first Flyway migration — V1__init.sql with the users table only
9. Run the app, verify it starts without errors

**Project structure:**
```
src/main/java/com/beatbattle/
├── auth/
│   ├── controller/
│   ├── service/
│   ├── dto/
│   └── filter/
├── user/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
├── room/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── scheduler/
├── battletype/
│   ├── BattleConfig.java
│   ├── BattleConfigFactory.java
│   └── validation/
│       └── SubmissionValidator.java
├── samplelibrary/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   ├── controller/
│   └── dto/
├── submission/
├── vote/
├── leaderboard/
├── storage/
│   └── R2Service.java
└── config/
    ├── SecurityConfig.java
    ├── RedisConfig.java
    └── R2Config.java
```

---

### Phase 1 — Auth system (1.5 weeks)

**Learn first (before coding):**
- How Spring Security filter chain works (read the docs section on Servlet Authentication)
- JWT structure — header.payload.signature — go to jwt.io, decode a token manually
- What bcrypt is — read the Wikipedia article intro
- Watch Amigoscode "Spring Boot 3 JWT Authentication from Scratch" (YouTube)

**What you're building:**
- User registration with email + password
- Login returning access token (15 min expiry) + refresh token (7 day expiry)
- JWT filter that validates every request
- Refresh token endpoint
- Logout endpoint

**Build order:**
1. User entity + Flyway migration V1
2. UserRepository with Spring Data JPA
3. Password hashing with BCryptPasswordEncoder
4. JwtService — generate, validate, extract claims
5. JwtAuthFilter — intercepts requests, validates token, sets SecurityContext
6. SecurityConfig — configure filter chain, public vs protected routes
7. AuthController — /register and /login endpoints
8. Test everything with Postman before moving on

**What to log in decisions/:**
- Why 15 minutes for access token expiry
- Why you're not using OAuth for MVP
- What your refresh token rotation strategy is

**Done when:**
- Can register a user via Postman
- Can login and get a token
- Protected endpoints return 401 without a token
- Protected endpoints work with a valid token

---

### Phase 2 — Room creation and management (1 week)

**Learn first:**
- JPA relationships — @ManyToOne, @OneToMany — read Vlad Mihalcea's post
- What LAZY loading is — understand why it's the default
- What @Transactional does and when you need it

**What you're building:**
- Room entity with all fields
- Create room endpoint (host only)
- Get room + list rooms with filtering and pagination
- Join room endpoint
- Room status validation (can't join a CLOSED room, etc.)

**Build order:**
1. Flyway migration V2 — rooms table + participants table
2. Room entity + RoomRepository
3. Participant entity + ParticipantRepository
4. RoomService — createRoom, getRoom, listRooms, joinRoom
5. RoomController — all room endpoints
6. RoomDTO — what you return to the client (never expose entity directly)
7. Add pagination to list endpoint using Spring Data Pageable
8. Test all endpoints with Postman

**Done when:**
- Can create a room as an authenticated user
- Can list rooms with status filter
- Can join a room
- Can't join a room you're already in
- Pagination works on room listing

---

### Phase 3 — File uploads and asset packs (1 week)

**Learn first:**
- What object storage is — read Cloudflare R2 docs intro
- What presigned URLs are — read the AWS S3 presigned URL docs (same concept)
- Set up Cloudflare account — create R2 bucket, get API keys
- Add AWS SDK to pom.xml and configure R2Client bean

**What you're building:**
- Presigned URL generation for asset uploads
- Asset pack metadata storage
- Asset listing with download URLs
- File type and size validation

**Build order:**
1. R2Config.java — configure S3Client pointing to R2
2. R2Service.java — generateUploadUrl, generateDownloadUrl, deleteObject
3. Flyway migration V3 — sample_library table + seed data
4. Flyway migration V4 — asset_packs table (with source + source_sample_id)
5. AssetPack entity + repository
6. SampleLibrary entity + repository + service + controller
7. AssetController — upload-url, confirm, list, delete, random
8. Test by uploading a real audio file from Postman and pulling random samples

**Important validations to implement:**
- File type must be in allowed list (audio/mpeg, audio/wav, image/jpeg, image/png)
- File size must be under 50MB
- Only room host can upload assets
- Room must be in WAITING or ACTIVE status

**Done when:**
- Can get a presigned URL and upload a file directly to R2
- Confirm endpoint saves metadata to DB
- Asset list returns files with working download URLs

---

### Phase 4 — Room state machine (1 week)

**Learn first:**
- Read about finite state machines — Wikipedia intro is enough
- Read Spring @Scheduled docs — understand fixedDelay vs fixedRate vs cron
- Read about optimistic locking — Baeldung article

**What you're building:**
- Start battle endpoint (host manually starts room)
- @Scheduled job that transitions ACTIVE → VOTING → CLOSED automatically
- State validation on all endpoints (can't submit during VOTING phase, etc.)
- Optimistic locking on Room entity with @Version

**Build order:**
1. Add @Version field to Room entity
2. Flyway migration V4 — add version column to rooms
3. RoomScheduler.java with @Scheduled(fixedDelay = 30000)
4. Implement transition logic with proper error handling
5. Add state guards to all relevant endpoints
6. Add /start endpoint to RoomController

**Test scenarios to verify:**
- Create a room with 2-minute active phase and 2-minute voting phase
- Start the room
- Wait — verify it transitions to VOTING after 2 minutes
- Wait — verify it transitions to CLOSED after 2 more minutes
- Verify you can't submit after VOTING starts
- Verify you can't vote after CLOSED

**Done when:**
- Room transitions happen automatically at the correct times
- State guards prevent invalid actions
- Two scheduler runs don't double-transition the same room

---

### Phase 5 — Submissions (1 week)

**Learn first:**
- Review presigned URL pattern from Phase 3 — same thing for submissions
- Read about UNIQUE constraints in PostgreSQL

**What you're building:**
- Same presigned URL pattern as assets — but for submissions
- Submit file during ACTIVE phase only
- One submission per user per room (enforced by DB constraint)
- Delete own submission (only during ACTIVE phase)

**Build order:**
1. Flyway migration V5 — submissions table
2. Submission entity + repository
3. SubmissionService — with state validation
4. SubmissionController
5. Test all edge cases

**Edge cases to handle:**
- Submitting after deadline → 400 Bad Request
- Submitting twice → 409 Conflict
- Submitting to a room you haven't joined → 403 Forbidden
- Uploading oversized file → 400 with clear message

**Done when:**
- Can submit a file during ACTIVE phase
- Can't submit during VOTING or CLOSED
- Two submissions from same user to same room rejected by DB

---

### Phase 6 — Voting and results (1 week)

**Learn first:**
- Read about PostgreSQL aggregation functions — AVG, COUNT, RANK
- Read about materialized views in PostgreSQL
- Understand the business rules: can't vote on own submission, one vote per submission

**What you're building:**
- Cast, update, delete vote endpoints
- Vote validation (no self-voting, voting only during VOTING phase, one vote per submission)
- Results endpoint — ranked submissions after CLOSED
- Leaderboard — refresh after each battle closes

**Build order:**
1. Flyway migration V6 — votes table + leaderboard materialized view
2. Vote entity + repository
3. VoteService — with all business rule validation
4. VoteController
5. ResultsService — aggregate votes, rank submissions
6. LeaderboardService — cache results in Redis, refresh after room closes
7. LeaderboardController

**Important validations:**
- User cannot vote on their own submission — check submission.userId != voter.id
- User can only vote during VOTING phase — check room.status == VOTING
- One vote per user per submission — enforced by UNIQUE constraint + service check
- User must have joined the room to vote — check participants table

**Done when:**
- Can cast votes during VOTING phase
- Can't vote twice (DB constraint catches it)
- Can't vote on own submission
- Results endpoint returns ranked list after CLOSED
- Leaderboard updates after battle closes

---

### Phase 7 — Frontend (2 weeks)

**Learn first:**
- React fundamentals if not already solid — React docs "Learn React" tutorial
- React Query (TanStack Query) — read the Getting Started docs
- React Router v6 — read the tutorial

**Pages to build (in this order):**
1. Login / Register page
2. Room listing page (browse battles)
3. Room detail page (view info, join, download assets)
4. Battle room page (submit file, see submissions, vote)
5. Results page (ranked results after CLOSED)
6. Leaderboard page
7. User profile page

**Each page needs:**
- API call with React Query
- Loading state
- Error state
- Auth guard (redirect to login if not authenticated)

**Auth flow in React:**
- Store access token in memory (not localStorage — XSS risk)
- Store refresh token in httpOnly cookie
- React Query handles refetch and token refresh automatically

**Done when:**
- Can complete a full battle cycle in the browser
- All pages handle loading and error states
- Works on mobile (Tailwind responsive classes)

---

### Phase 8 — WebSockets (V2, 1 week)

**Learn first:**
- Watch Amigoscode Spring Boot WebSocket tutorial
- Read Spring STOMP docs
- Understand SockJS fallback

**What you're adding:**
- Spring WebSocket config with STOMP
- Room event broadcasting (participant joined, submission uploaded, state changed)
- Server-side timer broadcasting every 30 seconds
- Frontend: connect on room page, subscribe to room topic, update UI on events

**Build order:**
1. Add spring-boot-starter-websocket to pom.xml
2. WebSocketConfig.java — configure message broker, STOMP endpoints
3. RoomEventService.java — broadcast events to room topics
4. Add broadcast calls to existing services (joinRoom, submitFile, etc.)
5. RoomTimerScheduler.java — broadcast timer updates every 30s
6. Frontend: connect with @stomp/stompjs, subscribe, update participant/submission counts

**Done when:**
- Participant count updates live when someone joins
- Submission count updates live when someone submits
- Timer syncs from server (can't be gamed)

---

### Phase 9 — Polish and deployment (1 week)

**Polish:**
- Add proper error handling — global @ControllerAdvice with consistent error responses
- Add request validation — @Valid on all request DTOs
- Add rate limiting on file uploads and vote endpoints
- Add Swagger/OpenAPI docs — springdoc-openapi
- Write at least 5 integration tests with Testcontainers

**Deployment — step by step:**

1. **Neon** — already set up. Get production connection string.

2. **Upstash** — already set up. Get production Redis URL.

3. **Cloudflare R2** — already set up. Note bucket name and endpoint.

4. **Render backend:**
   - Push code to GitHub
   - Go to render.com → New Web Service → connect GitHub repo
   - Build command: `./mvnw clean package -DskipTests`
   - Start command: `java -jar target/*.jar`
   - Add environment variables: DB_URL, REDIS_URL, R2_ACCESS_KEY, R2_SECRET_KEY, JWT_SECRET
   - Deploy

5. **Vercel frontend:**
   - Push React code to GitHub (separate repo or /frontend folder)
   - Go to vercel.com → New Project → connect repo
   - Framework: Vite
   - Add environment variable: VITE_API_URL=https://your-app.onrender.com
   - Deploy

6. **Cloudflare R2 CORS:**
   - Add CORS policy to R2 bucket allowing PUT from your Vercel domain
   - Without this, direct browser uploads will fail

7. **Test production:**
   - Register a user
   - Create a battle room
   - Upload an asset
   - Start the battle
   - Submit a file
   - Vote
   - Check results and leaderboard

---

## 11. Deployment Guide

### Environment variables needed

**Backend (Render):**
```
DATABASE_URL=postgresql://user:pass@host/beatbattle
REDIS_URL=redis://default:pass@host:port
R2_ACCOUNT_ID=your_cloudflare_account_id
R2_ACCESS_KEY=your_r2_access_key
R2_SECRET_KEY=your_r2_secret_key
R2_BUCKET_NAME=beat-battle-files
JWT_SECRET=your_256_bit_secret_key_here
JWT_EXPIRY_MS=900000
REFRESH_TOKEN_EXPIRY_DAYS=7
FRONTEND_URL=https://your-app.vercel.app
```

**Frontend (Vercel):**
```
VITE_API_URL=https://your-backend.onrender.com
VITE_WS_URL=wss://your-backend.onrender.com/ws
```

### application.properties structure
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true

spring.data.redis.url=${REDIS_URL}

app.jwt.secret=${JWT_SECRET}
app.jwt.expiry-ms=${JWT_EXPIRY_MS}
app.r2.account-id=${R2_ACCOUNT_ID}
app.r2.access-key=${R2_ACCESS_KEY}
app.r2.secret-key=${R2_SECRET_KEY}
app.r2.bucket=${R2_BUCKET_NAME}
app.frontend.url=${FRONTEND_URL}
```

### Render free tier notes
- Spins down after 15 minutes of inactivity
- First request after spin-down takes ~30 seconds (cold start)
- This is acceptable for a portfolio project — mention it honestly
- If you want to avoid it: add a free uptime monitor on uptimerobot.com to ping your app every 14 minutes

---

## 12. What to Put on Your Resume

### Project bullet points
```
Beat Battle — Real-time creative battle platform
• Built a Spring Boot REST API with JWT auth, room state machine (WAITING→ACTIVE→VOTING→CLOSED),
  and server-side timed transitions using Spring @Scheduled
• Implemented real-time room events via Spring WebSocket + STOMP, broadcasting participant counts,
  submission updates, and server-authoritative countdown timers
• Designed file upload pipeline using Cloudflare R2 presigned URLs, enabling direct browser-to-storage
  uploads with server-side validation (type, size, auth)
• Modeled a multi-entity PostgreSQL schema with business-rule constraints (unique vote per user/submission,
  one submission per user per room) and a materialized view for leaderboard aggregation
• Cached leaderboard results in Redis (Upstash) with TTL-based invalidation on battle close events
• Deployed backend on Render, frontend on Vercel, database on Neon — $0/month infrastructure cost
```

### Skills this adds to your resume
- Spring Boot (Web, Security, Data JPA, WebSocket, Scheduling)
- Real-time systems (WebSockets, STOMP)
- State machine design
- File storage (Cloudflare R2, presigned URLs)
- PostgreSQL (constraints, indexes, materialized views, aggregations)
- Redis caching with TTL
- React with React Query

---

## 13. Interview Prep

These are questions you will actually get asked. Write your answers in your own words after you build each feature.

**Q: How does your room state machine work?**
A: Each room has a status column. A Spring @Scheduled job runs every 30 seconds and queries for rooms whose transition timestamp has passed. It updates status in a transaction protected by optimistic locking (@Version) so two scheduler runs can't double-transition the same room.

**Q: How do you prevent double voting?**
A: Two layers. First, a UNIQUE constraint on (submission_id, voter_id) in PostgreSQL — even if two requests arrive simultaneously, only one can commit. Second, the service layer checks before inserting and returns a 409 Conflict with a clear message.

**Q: Why presigned URLs instead of uploading through your backend?**
A: Three reasons. One: large files block server threads on Render's free tier. Two: direct upload is faster — the file never travels through the backend. Three: Render has bandwidth limits; R2 has zero egress fees. The tradeoff is slightly more complex flow but the benefits are clear.

**Q: How does your WebSocket timer work? Can a client fake the time?**
A: The timer is server-authoritative. The server broadcasts the remaining seconds based on its own system time. The client just displays what the server says. Submission acceptance is also gated server-side — we check `Instant.now().isBefore(room.getVotingStartsAt())`, so even if a client sends a request at the exact deadline, the server decides.

**Q: How does your leaderboard work at scale?**
A: For portfolio scale, it's a materialized view refreshed after each battle closes. Redis caches the top 100 with a 5-minute TTL. If this were production scale with thousands of battles per day, I'd move to an incremental update model — updating individual user stats on battle close rather than recalculating everything. Possibly move to Redis sorted sets for real-time ranking.

**Q: What would you do differently if you had to support 10,000 concurrent users?**
A: Replace the in-memory WebSocket connection manager with Redis pub/sub so multiple backend instances can broadcast to the same room. Move the scheduler to a distributed job queue (Quartz or ShedLock) so only one instance runs transitions. Add horizontal scaling on Render. This is a good segue into distributed systems concepts.

---

## Appendix — Useful Links

| Resource | URL |
|---|---|
| Spring Initializr | start.spring.io |
| Neon (Postgres) | neon.tech |
| Upstash (Redis) | upstash.com |
| Cloudflare R2 | developers.cloudflare.com/r2 |
| Render | render.com |
| Vercel | vercel.com |
| Baeldung Spring Security | baeldung.com/security-spring |
| Baeldung JPA | baeldung.com/the-persistence-layer-with-spring-and-jpa |
| Vlad Mihalcea JPA | vladmihalcea.com/tutorials/hibernate |
| jjwt library | github.com/jwtk/jjwt |
| AWS SDK v2 Java | docs.aws.amazon.com/sdk-for-java/latest/developer-guide |
| TanStack Query | tanstack.com/query/latest |
| React Router v6 | reactrouter.com |
| @stomp/stompjs | stomp.js.org |

---

*Last updated: project planning phase. Update this document as decisions change.*
