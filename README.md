# 🕵️ Ephemeral Messenger API (Backend)

A secure, self-destructing message service built with Java 21 and Spring Boot 3. This application allows users to generate one-time secure links for sensitive data. The data is stored in Redis with a strict TTL (Time-To-Live) and is cryptographically deleted immediately after being read.

[Live Frontend Demo](https://selfdestruct-client.vercel.app/) | [Frontend Repository](https://github.com/BailurVikramBhat/selfdestruct-client)

## 🚀 Key Features

🔥 Self-Destruct Mechanism: Messages are stored in Redis with a 10-minute TTL. Once a message is retrieved via the API, it is instantly deleted from the cache (Read-Once Policy).

🛡️ Rate Limiting (DDoS Protection): Implemented Token Bucket Algorithm using Bucket4j to restrict requests based on IP address, preventing spam and automation attacks.

👁️ Security Auditing (AOP): Uses Spring Aspect Oriented Programming (AOP) to log access attempts. It separates cross-cutting concerns (logging) from business logic while masking sensitive content in logs.

🐳 Dockerized: Fully containerized using a multi-stage Dockerfile (Maven Build + Eclipse Temurin JRE) for consistent deployment across environments.

☁️ Cloud Native: Configured for deployment on Render with dynamic port binding and environment-based configuration.

## 🏗️ Architecture

The application follows a standard layered architecture with a focus on high-performance caching.

```text
    User[Client] -->|POST / Secret| RateLimit[Bucket4j Filter]
    RateLimit --> Controller[Secret Controller]
    Controller --> Service[Secret Service]
    Service -->|Save (TTL 10m)| Redis[(Redis/Valkey)]
```

```text
    User -->|GET / Secret| Controller
    Controller --> Service
    Service -->|Fetch & Delete| Redis
    Service -.->|Audit Log| AOP[Aspect Logger]
```

## 🛠️ Tech Stack

Core: Java 21, Spring Boot 3.4.0

Data: Spring Data Redis (connected to Valkey on Render)

Security/Throttling: Bucket4j, Jakarta Validation

DevOps: Docker, Maven, Render (Cloud PaaS)

Testing: JUnit 5

## ⚙️ Getting Started

### Prerequisites
 - Java 21 SDK
 - Docker (Optional, for container run)
 - Redis (Running locally on port 6379)

1. Clone the Repository

   ```
   git clone [https://github.com/YOUR_USERNAME/ephemeral-backend.git](https://github.com/YOUR_USERNAME/ephemeral-backend.git)
   cd ephemeral-backend
   ```


2. Run Locally (Native)

   Ensure Redis is running (docker run -p 6379:6379 redis).

   `mvn spring-boot:run`
    
    The app will start at http://localhost:8080.

3. Run with Docker

## Build the image
`docker build -t ephemeral-api .`

## Run the container (Linking to host Redis)
`docker run -p 8080:8080 --network="host" ephemeral-api`


## 🔌 API Documentation

1. Create a Secret

Endpoint: `POST /api/secrets`

Request Body:
```json
{
"content": "My super secret password"
}
```

Constraints: Max 1000 characters. Cannot be empty.

Response (201 Created):
```json
{
"id": "550e8400-e29b-41d4-a716-446655440000"
}
```


2. Retrieve a Secret

Endpoint: `GET /api/secrets/{id}`

Response (200 OK):
```json
{
"content": "My super secret password"
}
```


Note: A second request to this same ID will result in a 404 error.

Response (404 Not Found):
```json
{
"error": "This message has self-destructed or never existed."
}
```

```json

Response (429 Too Many Requests):

{
"error": "Rate limit exceeded. Try again in 59 seconds."
}



## 🌍 Environment Variables

When deploying, the application expects the following variables (defaults to localhost if missing):

SPRING_DATA_REDIS_HOST - Hostname of the Redis server
SPRING_DATA_REDIS_PORT - Port of the Redis server


## 📝 License

This project is licensed under the MIT License.