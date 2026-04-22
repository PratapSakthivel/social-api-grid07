# Grid07 Backend Assignment

Spring Boot microservice with Redis-backed guardrail system for concurrent bot interaction management.

## 🎯 Objective

Build a robust, high-performance Spring Boot microservice that acts as the central API gateway and guardrail system. This system handles concurrent requests, manages distributed state using Redis, and implements event-driven scheduling.

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.2.5**
- **PostgreSQL** (Neon Cloud) - Source of truth for content
- **Redis 7** (Docker) - Gatekeeper for counters, cooldowns, and notifications
- **Maven** - Build tool

## 🚀 How to Run

### Prerequisites
- Java 17+ installed
- Docker installed and running
- Maven installed

### Setup Steps

1. **Start Redis**
```bash
docker-compose up -d
```

2. **Start Spring Boot Application**
```bash
cd backend
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Database Configuration
The application uses Neon PostgreSQL (cloud-hosted). Connection details are in `backend/src/main/resources/application.properties`.

## 📡 API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/posts` | Create a new post (USER or BOT) |
| POST | `/api/posts/{postId}/comments` | Add comment with guardrails for bots |
| POST | `/api/posts/{postId}/like?userId={id}` | Like a post (+20 virality points) |

### Example Requests

**Create Post:**
```json
POST /api/posts
{
  "authorId": 1,
  "authorType": "USER",
  "content": "Hello world!"
}
```

**Add Comment:**
```json
POST /api/posts/1/comments
{
  "authorId": 1,
  "authorType": "BOT",
  "content": "Nice post!",
  "depthLevel": 1
}
```

**Like Post:**
```
POST /api/posts/1/like?userId=1
```

## 🔒 Thread Safety — How Atomic Locks Work

### 1. Horizontal Cap (100 bot replies per post)

**Implementation:** Redis `INCR post:{id}:bot_count`

**Why it's thread-safe:**
- Redis `INCR` is atomic at the command level
- Even with 200 concurrent requests hitting at the exact same millisecond, Redis processes each `INCR` sequentially
- After incrementing, if the new value exceeds 100, we immediately `DECR` to roll back
- This guarantees the count never exceeds 100, regardless of concurrency

**Code Flow:**
```java
Long newCount = redisTemplate.opsForValue().increment(RedisKeys.botCount(postId));
if (newCount > HORIZONTAL_CAP) {
    redisTemplate.opsForValue().decrement(RedisKeys.botCount(postId)); // Rollback
    return false; // Reject
}
return true; // Allow
```

### 2. Cooldown Cap (10-minute bot→human cooldown)

**Implementation:** Redis `SET key value NX EX 600`

**Why it's thread-safe:**
- Uses `setIfAbsent()` which maps to Redis `SET key value NX EX ttl`
- `NX` means "set only if key does Not eXist"
- This is a single atomic Redis command
- If two threads try to set the same cooldown key simultaneously, only one wins
- No race condition is possible

**Code Flow:**
```java
Boolean wasSet = redisTemplate.opsForValue()
    .setIfAbsent(key, "1", Duration.ofMinutes(10));
return Boolean.TRUE.equals(wasSet); // true = allowed, false = blocked
```

### 3. Vertical Cap (max depth 20)

**Implementation:** Pure arithmetic check on `depthLevel` field

**Why it's thread-safe:**
- No shared state involved
- Simple comparison: `depthLevel <= 20`
- No concurrency concern

## 🏗️ Architecture Decision

**PostgreSQL** is the source of truth for all post/comment content.

**Redis** acts as the gatekeeper — the database is only written to **after** Redis guardrails allow the action.

This ensures the DB never has more than 100 bot comments even under extreme concurrent load.

### Request Flow:
```
1. Bot comment request arrives
2. Check Redis vertical cap (depth ≤ 20)
3. Check Redis horizontal cap (bot count ≤ 100) — ATOMIC INCR
4. Check Redis cooldown cap (10 min) — ATOMIC SET NX EX
5. If ANY guardrail fails → rollback Redis changes, return 429
6. If ALL guardrails pass → ONLY THEN save to PostgreSQL
7. Update virality score in Redis
8. Trigger notification logic
```

## 📊 Redis Keys Reference

| Key Pattern | Type | Purpose | TTL |
|-------------|------|---------|-----|
| `post:{id}:virality_score` | String (int) | Running virality score | None |
| `post:{id}:bot_count` | String (int) | Total bot replies (cap: 100) | None |
| `cooldown:bot_{botId}:human_{humanId}` | String | Blocks bot repeat interaction | 10 min |
| `notif:cooldown:user_{userId}` | String | Notification throttle | 15 min |
| `user:{userId}:pending_notifs` | List | Buffered notification strings | None |

## 🎯 Virality Score System

- **Bot Reply:** +1 point
- **Human Like:** +20 points
- **Human Comment:** +50 points

All updates happen in real-time using Redis `INCR` operations.

## 🔔 Notification Engine

### Smart Batching Logic:
1. **First bot interaction** → Send immediate notification, set 15-min cooldown
2. **Subsequent interactions during cooldown** → Buffer in Redis list
3. **CRON sweeper (every 5 minutes)** → Summarize and send batched notifications

### Console Output Examples:
```
[NOTIF] Push Notification Sent to User 1: Bot 1 replied to your post 1
[NOTIF] Buffered notification for User 1: Bot 2 replied to your post 1
[SWEEPER] Summarized Push Notification to User 1: Bot 2 and 4 others interacted with your posts.
```

## ✅ Testing & Validation

### Race Condition Test (The Spam Test)

**Scenario:** 200 concurrent bot comment requests on a single post

**Expected Result:** Exactly 100 comments saved to database

**Verification:**
```bash
# Check Redis counter
docker exec -it grid07-redis redis-cli GET post:1:bot_count
# Expected: 100

# Check database
SELECT COUNT(*) FROM comments WHERE post_id = 1 AND author_type = 'BOT';
-- Expected: 100
```

### Statelessness Audit

**Verified:** No HashMap, static Map, static List, or static counters in codebase.

All state lives in Redis only.

### Data Integrity

**Verified:** `commentRepository.save()` is called ONLY after all Redis guardrails pass.

If any guardrail fails, the method returns early with 429 status, and no database write occurs.

## 📦 Project Structure

```
grid07-backend/
├── backend/
│   ├── src/main/java/com/grid07/backend/
│   │   ├── config/          → RedisConfig, SchedulingConfig
│   │   ├── controller/      → PostController
│   │   ├── service/         → PostService, ViralityService, NotificationService
│   │   ├── repository/      → JPA repositories
│   │   ├── entity/          → User, Bot, Post, Comment
│   │   ├── dto/             → Request/Response DTOs
│   │   ├── constants/       → RedisKeys, ApplicationConstants
│   │   └── scheduler/       → NotificationScheduler
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── docker-compose.yml       → Redis service
├── postman/
│   └── grid07.postman_collection.json
└── README.md
```

## 🧪 Test Scenarios Covered

✅ **Phase 1:** Core entities, repositories, and 3 REST endpoints  
✅ **Phase 2:** Redis virality engine with atomic guardrails  
✅ **Phase 3:** Notification throttling with smart batching  
✅ **Phase 4:** Race condition testing, statelessness audit, deliverables

### Specific Tests:
- ✅ Virality score tracking (bot reply +1, human like +20, human comment +50)
- ✅ Horizontal cap: 101st bot comment rejected with 429
- ✅ Vertical cap: depth 21 rejected with 429
- ✅ Cooldown cap: second interaction within 10 min rejected with 429
- ✅ Concurrency: 200 simultaneous requests → exactly 100 saved
- ✅ Notification throttling: immediate send → buffer → batch sweep
- ✅ CRON sweeper: summarized notifications every 5 minutes

## 🔗 GitHub Repository

https://github.com/PratapSakthivel/social-api-grid07

## 👨‍💻 Author

Pratap Sakthivel  
Email: pratapssakthivel@gmail.com

## 📝 Notes

- PostgreSQL switched from Docker to Neon cloud for better reliability
- Redis remains in Docker for local development
- All atomic operations use Redis native commands (INCR, SET NX EX)
- No Java-level synchronization needed — Redis handles all concurrency
- Notification sweeper runs every 5 minutes for testing (production: 15 min)

---

**Built for Grid07 Backend Engineering Assignment**
