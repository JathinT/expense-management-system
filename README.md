# Expense Management System

A small Version 1 application for creating and managing personal expenses. It supports adding expenses, listing them, viewing one by ID through the API, deleting them, and displaying the total amount.

## Tech stack

- Backend: Java 17, Spring Boot, Spring Data JPA, Maven, H2
- Frontend: React, JavaScript, Vite

## Project structure

```text
backend/   Spring Boot REST API, persistence, and service tests
frontend/  React and Vite user interface
```

## Run the backend

Requirements: JDK 17+ and Maven 3.6+.

```bash
cd backend
mvn spring-boot:run
```

The API runs at `http://localhost:8080`. It uses an in-memory H2 database, so data is cleared when the application stops.

Run backend unit tests with `mvn test` from `backend/`.

## Run the frontend

Requirements: Node.js 20.19+ or 22.12+.

```bash
cd frontend
npm install
npm run dev
```

Open the local URL printed by Vite (normally `http://localhost:5173`). Keep the backend running; Vite forwards `/api` requests to `http://localhost:8080`.

## API

All endpoints use JSON. An expense has `amount` (positive decimal), `category` (non-empty string), `description` (optional string), and `expenseDate` (ISO date, `YYYY-MM-DD`). The generated `id` is returned with each expense.

| Method | Path | Description | Success response |
| --- | --- | --- | --- |
| `POST` | `/api/expenses` | Create an expense | `201 Created`, expense JSON |
| `GET` | `/api/expenses` | List all expenses | `200 OK`, array of expense JSON |
| `GET` | `/api/expenses/{id}` | Get one expense | `200 OK`, expense JSON |
| `DELETE` | `/api/expenses/{id}` | Delete an expense | `204 No Content` |
| `GET` | `/api/expenses/total` | Calculate the total | `200 OK`, `{"total": 0}` |

Missing expenses return `404` with a message. Invalid create requests return `400` with field-level validation messages.

## Deploying the backend with Docker (Render)

This repository includes a Dockerfile for the backend (backend/Dockerfile) that builds the project using Maven on Java 17 and runs the generated Spring Boot jar. The Docker build runs the Maven package goal and therefore executes the project's tests as part of the image build.

Notes and usage:

- The Dockerfile is a multi-stage build:
  - Build stage: uses a Maven + JDK 17 image to run `mvn package` (tests will run).
  - Runtime stage: uses a JRE 17 image to run the produced jar.

- The application will listen on the port supplied by the PORT environment variable at runtime. The Dockerfile's CMD passes the PORT value to Spring Boot so hosted environments (like Render) can control the listening port.

Local build and run example:

```bash
# Build the Docker image from the backend directory
docker build -t expense-backend ./backend

# Run the container, mapping the PORT and exposing it on the host
docker run -e PORT=8080 -p 8080:8080 expense-backend
```

Deploying on Render:

1. Create a new Web Service on Render and connect your repository.
2. Choose the Docker option (Render will use the repository Dockerfile). The Dockerfile to use is `backend/Dockerfile` — if Render expects a Dockerfile in repo root, configure the service to provide the build context `backend/`.
3. Render will provide the `$PORT` environment variable automatically; the image will read this and run the application on the assigned port.

Security and tests:

- No secrets are committed in this change.
- The Docker build runs the Maven package phase which includes running unit tests; ensure tests pass before pushing or deploying.

