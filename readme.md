# Distributed Rate Limiter

A rate limiting system built from scratch using Spring Boot, Java 21, Redis and Lua.

The project started as a simple in-memory rate limiter and was gradually extended into a Redis-backed distributed token bucket implementation. The main goal was not just to make rate limiting work, but to understand what happens when multiple requests and multiple application instances are involved.

---

## What is this project?

A rate limiter controls how many requests a client can make within a certain period of time.

For example, if an API allows 5 requests and a client sends 10 requests very quickly, the first 5 can be accepted while the remaining requests are rejected with:

```text
HTTP 429 - Too Many Requests
```

The interesting part is making this work correctly when requests are coming concurrently and the application is running on multiple instances.

---

## What I built

The project went through several stages while learning different rate limiting approaches:

- Fixed Window
- Sliding Window
- Token Bucket
- Redis-backed Token Bucket
- Atomic Redis operations using Lua
- Per-client rate limiting
- Concurrent request testing
- Rate limit response metadata

The final implementation uses a Redis-backed Token Bucket.

---

## Architecture

```text
                 Client
                   |
                   | HTTP Request
                   v
             Spring Boot API
                   |
                   v
              RateLimiter
                   |
                   v
             Redis + Lua
                   |
          +--------+--------+
          |                 |
       Allowed           Rejected
          |                 |
          v                 v
       HTTP 200          HTTP 429
```

Redis stores the state of each client's token bucket.

For example:

```text
rate-limit:Aakash
rate-limit:Sky
rate-limit:John
```

Each client gets an independent bucket.

---

## Token Bucket

The final implementation uses the Token Bucket algorithm.

The bucket has a maximum capacity of 5 tokens.

```text
Capacity = 5
Refill Rate = 0.5 tokens/second
```

A request consumes one token.

If a token is available:

```text
Request
   |
   v
Token available?
   |
  YES
   |
   v
Consume token
   |
   v
Allow request
```

If the bucket is empty:

```text
Request
   |
   v
No token
   |
   v
Reject request
   |
   v
HTTP 429
```

The refill rate allows tokens to accumulate again over time.

With a refill rate of 0.5 tokens per second, one token becomes available approximately every 2 seconds.

---

## Why Redis?

The first version of the rate limiter stored bucket information in application memory.

That works with a single server, but it creates a problem when the application is scaled:

```text
                Load Balancer
               /      |      \
              v       v       v
           Server 1 Server 2 Server 3
              |       |       |
              +-------+-------+
                      |
                    Redis
```

If each server keeps its own bucket, the same client could effectively get a separate limit on every server.

Redis provides shared state between all application instances.

This makes the rate limiter suitable for a distributed environment.

---

## Why Lua?

There is an important concurrency problem when Redis operations are performed as separate commands.

Imagine two requests arrive at almost exactly the same time:

```text
Request A -> read tokens
Request B -> read tokens

Request A -> consume token
Request B -> consume token
```

Both requests could potentially make decisions using the same old state.

To avoid this, the refill, check and token consumption operations are performed inside a Redis Lua script.

Conceptually:

```text
Read bucket
    |
Calculate refill
    |
Check available tokens
    |
Consume token
    |
Update Redis
    |
Return result
```

Redis executes the Lua script atomically, which prevents the individual operations from being interleaved with another request.

---

## Response

The rate limiter returns information such as:

```text
allowed
limit
remaining
retryAfterSeconds
```

The HTTP API also exposes rate limiting information through response headers.

Example:

```http
X-RateLimit-Limit: 5
X-RateLimit-Remaining: 0
Retry-After: 2
```

When the bucket is exhausted:

```http
HTTP/1.1 429 Too Many Requests
```

---

## Example

Suppose the bucket starts with 5 tokens.

```text
Request 1 -> Allowed -> 4 remaining
Request 2 -> Allowed -> 3 remaining
Request 3 -> Allowed -> 2 remaining
Request 4 -> Allowed -> 1 remaining
Request 5 -> Allowed -> 0 remaining
Request 6 -> Rejected
```

After enough time passes, a new token becomes available and another request can be accepted.

---

## Concurrency Testing

I also tested the limiter with concurrent requests.

Test scenario:

```text
100 concurrent requests
Bucket capacity = 5
```

Result:

```text
Allowed: 5
Rejected: 95
```

This test was useful for verifying that multiple simultaneous requests don't accidentally consume the same token.

---

## Testing

The project contains tests for:

- Initial requests being allowed
- Sixth request being rejected
- Token refill
- Independent buckets for different clients
- Concurrent requests
- Redis-backed state

The tests can be run using the Maven wrapper:

```bash
.\mvnw.cmd clean test
```

To run a particular test:

```bash
.\mvnw.cmd -Dtest=RedisTokenBucketRateLimiterTest#shouldRejectSixthRequest test
```

---

## Tech Stack

- Java 21
- Spring Boot
- Maven
- Spring Data Redis
- Redis
- Redis Lua
- JUnit
- Postman

---

## Running the project

### 1. Start Redis

Make sure Redis is running locally.

### 2. Start the Spring Boot application

Run the application from IntelliJ or use:

```bash
.\mvnw.cmd spring-boot:run
```

### 3. Send a request

Example:

```http
GET http://localhost:8080/api/test
```

Add the client ID as a request header:

```http
X-Client-Id: Aakash
```

Send multiple requests and observe the remaining tokens.

---

## What I learned

The main purpose of this project was understanding the problems behind rate limiting rather than simply using an existing library.

Some of the important concepts I worked with were:

- Rate limiting algorithms
- Token buckets
- Redis as shared state
- Atomic operations
- Redis Lua scripts
- Race conditions
- Concurrent requests
- HTTP 429 responses
- Distributed application design

---

## Possible improvements

There are several things that could be added to make this closer to a production system:

- Configurable limits per API endpoint
- Different limits for different users
- Authentication-based client identification
- Redis cluster support
- Better failure handling when Redis is unavailable
- Rate limiting at the API gateway level
- Monitoring and metrics
- More detailed load testing

---

## Why I built it

I wanted to understand how systems protect APIs from excessive traffic and, more importantly, what changes when the application is distributed across multiple servers.

Building the limiter from the basic Fixed Window approach and eventually moving to Redis + Lua helped me understand the problem from both an algorithmic and system-design perspective.
