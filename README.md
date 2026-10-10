# LogStream — Distributed Log Analytics & Alerting Platform

LogStream is a full-stack log ingestion and search prototype. It accepts structured log events over gRPC, indexes them with Apache Lucene, and exposes a REST endpoint that the React dashboard uses to search indexed logs.

## Features

- **gRPC log ingestion** on port `9090` using Protocol Buffers.
- **Lucene indexing** for timestamp, level, service, message, and response time.
- **REST search API** on port `8080`.
- **React + Vite dashboard** to search logs by service, level, or message.
- **Benchmark utility** for indexing and querying a large synthetic log dataset.
- **Spring Boot Actuator** and validation dependencies for application monitoring and request validation capabilities.

## Technology stack

- Java 17 and Spring Boot
- gRPC and Protocol Buffers
- Apache Lucene
- React 19 and Vite
- Maven

## Repository layout

```text
logstream/
├── backend/logstream-backend/
│   ├── src/main/java/logstream_backend/
│   │   ├── controller/       # REST log search API
│   │   ├── grpc/             # gRPC server and ingestion service
│   │   ├── lucene/           # Lucene indexing and search
│   │   ├── client/           # Sample gRPC client
│   │   └── benchmark/        # Synthetic indexing/search benchmark
│   ├── src/main/proto/log.proto
│   └── pom.xml
└── frontend/                 # React + Vite dashboard
```

## Prerequisites

Install JDK 17, Node.js (compatible with the Vite version in `frontend/package.json`), and npm. The Maven wrapper is included for building the backend.

## Run locally

### 1. Start the backend

From the repository root:

**Windows PowerShell**
```powershell
cd backend/logstream-backend
.\mvnw.cmd spring-boot:run
```

**macOS/Linux**
```bash
cd backend/logstream-backend
chmod +x mvnw
./mvnw spring-boot:run
```

The REST API uses port `8080`; the gRPC server is configured for port `9090`. Keep the backend terminal running.

### 2. Start the frontend

In a separate terminal, from the repository root:

```bash
cd frontend
npm install
npm run dev
```

Open the local URL printed by Vite (normally `http://localhost:5173`).

### 3. Send a sample log

With the backend running, open a second terminal:

**Windows PowerShell**
```powershell
cd backend/logstream-backend
.\mvnw.cmd -q -DskipTests package
java -cp target/classes;target/dependency/* logstream_backend.client.LogGrpcClient
```

The sample client uses the generated gRPC classes; if the plain classpath command does not include dependencies in your environment, run the client from your IDE with the Maven dependency classpath. It sends a sample `INFO` event from `payment-service`.

You can also run `logstream_backend.client.LogGrpcClient` from an IDE after the Maven project has built and protobuf/gRPC source generation has run.

## REST API

### Search logs

```http
GET http://localhost:8080/api/logs/search?field=service&query=payment-service
```

Supported fields in the dashboard: `service`, `level`, and `message`.

Example queries:

- `/api/logs/search?field=service&query=payment-service`
- `/api/logs/search?field=level&query=INFO`
- `/api/logs/search?field=message&query=payment`

The API returns a JSON array containing `timestamp`, `level`, `service`, `message`, and `responseTime`.

## Benchmark

Run `logstream_backend.benchmark.LuceneSearchBenchmark` from your IDE or with a Maven-configured classpath to create and search one million synthetic log documents. It prints indexing duration and a measured search duration. Benchmark timings depend on the machine and should be reported only after running the benchmark locally.

## Notes and limitations

- The Lucene index is stored in a local `logs-index/` directory. It is runtime data and is intentionally not committed.
- The current implementation is a learning/demo prototype. It does not yet provide authentication, a production alerting/rules engine, distributed storage, or deployment configuration.
- The REST search API currently returns up to 20 results.
- Ensure the frontend and backend are both running locally; the UI calls the backend at `http://localhost:8080`.
- The project has not been verified in this environment by running a full Java/Maven build, gRPC smoke test, and frontend production build. Please complete these checks before describing it as production-ready.

## Future improvements

- Add automated integration tests for gRPC ingestion and REST search.
- Add configurable alert rules and notifications.
- Add Docker Compose deployment and CI checks.
- Make API base URLs configurable through environment variables.
- Add pagination and structured error handling to search.
