# LogStream — Distributed Log Analytics & Alerting Platform

LogStream is a full-stack log ingestion, search, statistics, and alerting prototype. It accepts structured log events over gRPC, indexes them with Apache Lucene, and exposes REST APIs consumed by a React dashboard.

## Features

- **gRPC ingestion** on port `9090` using Protocol Buffers.
- **Lucene indexing and search** for timestamp, severity, service, message, and response time.
- **REST APIs** for log search, aggregate statistics, alert-rule management, and triggered alerts.
- **Configurable alert rules** matching log level, service name, and/or message substring.
- **React + Vite dashboard** for searching indexed logs.
- **Benchmark utility** for synthetic Lucene indexing/search experiments.
- **GitHub Actions CI** builds the Java backend and React frontend on pushes and pull requests.

## Technology stack

Java 17 · Spring Boot · gRPC · Protocol Buffers · Apache Lucene · React 19 · Vite · Maven

## Repository layout

```text
logstream/
├── .github/workflows/ci.yml
├── backend/logstream-backend/
│   ├── src/main/java/logstream_backend/
│   │   ├── alerts/            # Rule matching and recent triggered alerts
│   │   ├── controller/        # REST search, statistics, and alert APIs
│   │   ├── grpc/              # gRPC server and ingestion service
│   │   ├── lucene/            # Lucene indexing and search
│   │   ├── client/            # Sample gRPC client
│   │   └── benchmark/         # Synthetic indexing/search benchmark
│   ├── src/main/proto/log.proto
│   └── pom.xml
└── frontend/                  # React + Vite dashboard
```

## Prerequisites

Install JDK 17, Node.js compatible with the Vite version in `frontend/package.json`, and npm. The Maven wrapper is included.

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

The REST API listens on port `8080`; the gRPC server listens on port `9090`.

### 2. Start the frontend

In a separate terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the URL printed by Vite (normally `http://localhost:5173`). You can configure the API URL by copying `frontend/.env.example` to `frontend/.env` and setting `VITE_API_BASE_URL`.

### 3. Send a sample log

Keep the backend running and open another terminal:

**Windows PowerShell**
```powershell
cd backend/logstream-backend
.\mvnw.cmd -q compile exec:java "-Dexec.mainClass=logstream_backend.client.LogGrpcClient"
```

**macOS/Linux**
```bash
cd backend/logstream-backend
./mvnw -q compile exec:java -Dexec.mainClass=logstream_backend.client.LogGrpcClient
```

The sample client sends an `INFO` event from `payment-service`.

## REST API

### Search logs

```http
GET http://localhost:8080/api/logs/search?field=service&query=payment-service
```

Supported fields: `service`, `level`, `message`, and `timestamp`. Results are currently limited to 20 entries.

Examples:
- `/api/logs/search?field=level&query=ERROR`
- `/api/logs/search?field=message&query=payment`
- `/api/logs/search?field=timestamp&query=2026-10-10T12:00:00Z`

### Aggregate statistics

```http
GET http://localhost:8080/api/logs/stats
```

Returns `totalLogs`, counts grouped by `level` and `service`, and average response time in milliseconds. Before any logs are ingested, the endpoint returns zero/empty statistics.

### Create an alert rule

```http
POST http://localhost:8080/api/alerts/rules
Content-Type: application/json

{
  "name": "Payment service errors",
  "level": "ERROR",
  "service": "payment-service",
  "messageContains": "",
  "enabled": true
}
```

At least one condition is required. Conditions provided together are combined with AND logic. Level matching is case-insensitive; service matching is case-insensitive; message matching is a case-insensitive substring match.

- `GET /api/alerts/rules` — list configured rules.
- `DELETE /api/alerts/rules/{id}` — delete a rule.
- `GET /api/alerts` — list recent triggered alerts (up to 500).
- Rules are evaluated whenever a gRPC log is ingested.

**Prototype limitation:** alert rules and recent triggered alerts are held in application memory and reset when the backend restarts. Notifications (email/webhook), authentication, distributed persistence, and production deployment are not included.

## Benchmark

Run `logstream_backend.benchmark.LuceneSearchBenchmark` from your IDE or with a Maven-configured classpath to create and search synthetic log documents. Benchmark timings depend on your machine; report measured values only after running it locally.

## Data and operational notes

- The Lucene index is written to the local `logs-index/` directory and is runtime data, not committed to Git.
- The API currently supports a local development frontend at `localhost:5173`; update CORS settings if you deploy the UI elsewhere.
- GitHub Actions runs backend tests/build and frontend production build. Check the [latest CI run](https://github.com/student-suraj/logstream/actions) before submitting.
