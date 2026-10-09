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
