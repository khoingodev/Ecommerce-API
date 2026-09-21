# Ecommerce API

A full-stack ecommerce application built with **Java 17**, **Spring Boot**, **Spring Data JPA**, **PostgreSQL**, **JWT**, and **React + Vite**.

The project is organized as a single repository with independently runnable backend and frontend applications.

## Features

- Product catalog with public read access
- Admin product management
  - Create products
  - Update products
  - Delete products
  - Manage stock quantity
- JWT authentication
  - User registration
  - User login
  - BCrypt password hashing
  - `USER` and `ADMIN` roles
- Shopping cart with browser persistence
- Transactional checkout
- Pessimistic stock locking to prevent overselling
- Customer order history
- Admin order management
  - View all orders
  - Update order status
- PostgreSQL persistence
- Flyway database migrations
- Bean Validation and global exception handling
- Swagger/OpenAPI documentation
- Spring Boot Actuator health endpoint
- Docker Compose development environment
- Unit and integration tests

## Tech Stack

### Backend

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- JSON Web Token via JJWT
- PostgreSQL
- Flyway
- Lombok
- Maven

### Frontend

- React
- Vite
- Lucide React
- Nginx for production container serving

## Repository Structure

```text
ecommerce-api/
├── backend/
│   ├── src/main/java/
│   ├── src/main/resources/
│   │   ├── db/migration/
│   │   └── application.properties
│   ├── src/test/
│   ├── Dockerfile
│   ├── pom.xml
│   └── mvnw.cmd
├── frontend/
│   ├── src/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── database/
├── docker-compose.yml
└── .env.example
```

## Requirements

For local development without Docker:

- Java 17+
- Maven Wrapper
- Node.js 20+
- PostgreSQL 15+

For the containerized setup:

- Docker Desktop with Compose

## Quick Start With Docker Compose

1. Create a local environment file:

```powershell
Copy-Item .env.example .env
```

2. Edit `.env` and replace all placeholder values:

```env
DB_USERNAME=postgres
DB_PASSWORD=choose-a-strong-password
JWT_SECRET=use-at-least-32-random-characters
SEED_ADMIN_EMAIL=admin@example.com
SEED_ADMIN_PASSWORD=choose-a-strong-admin-password
```

3. Start the complete stack:

```powershell
docker compose up --build
```

4. Open the applications:

- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs
- Health check: http://localhost:8080/actuator/health

Stop the stack with:

```powershell
docker compose down
```

To remove the PostgreSQL volume as well:

```powershell
docker compose down -v
```

The `-v` option deletes local database data.

## Local Development

### 1. Prepare PostgreSQL

Create a database named `ecommerce` and make sure PostgreSQL is running on port `5432`.

You can create the schema manually using:

```text
backend/database/ecommerce_setup.sql
```

When creating the schema manually, keep Flyway disabled:

```properties
spring.flyway.enabled=false
```

Alternatively, let Flyway manage the schema by setting:

```powershell
$env:FLYWAY_ENABLED="true"
```

Do not run the manual setup script and Flyway migrations as competing schema managers in the same database.

### 2. Configure backend environment variables

From PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/ecommerce"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-postgres-password"
$env:JWT_SECRET="your-random-secret-with-at-least-32-characters"
```

### 3. Start the backend

```powershell
cd backend
./mvnw.cmd spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

### 4. Start the frontend

Open another terminal:

```powershell
cd frontend
npm install
npm run dev
```

The frontend runs at:

```text
http://localhost:5173
```

The frontend expects the backend at `http://localhost:8080` by default. To override it, create `frontend/.env`:

```env
VITE_API_URL=http://localhost:8080
```

## Authentication

Registration and login are public:

```text
POST /api/auth/register
POST /api/auth/login
```

Example registration request:

```json
{
  "email": "buyer@example.com",
  "password": "password123"
}
```

The response contains a JWT access token. Use it for protected requests:

```text
Authorization: Bearer <access-token>
```

Admin users can be seeded when running with environment variables:

```powershell
$env:SEED_ADMIN_ENABLED="true"
$env:SEED_ADMIN_EMAIL="admin@example.com"
$env:SEED_ADMIN_PASSWORD="strong-admin-password"
```

The seed operation updates or creates the configured account with the `ADMIN` role.

## API Overview

### Public endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a user |
| `POST` | `/api/auth/login` | Login and receive a JWT |
| `GET` | `/api/products` | List products |
| `GET` | `/api/products/{id}` | Get a product |
| `GET` | `/api-docs` | OpenAPI specification |
| `GET` | `/swagger-ui.html` | Swagger UI |
| `GET` | `/actuator/health` | Application health |

### Authenticated user endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/orders` | Create an order from a product and quantity |
| `GET` | `/api/orders/mine` | Get the authenticated user's orders |

### Admin endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/products` | Create a product |
| `PUT` | `/api/products/{id}` | Update a product |
| `DELETE` | `/api/products/{id}` | Delete a product |
| `GET` | `/api/admin/orders` | List all orders |
| `PATCH` | `/api/admin/orders/{id}/status` | Update order status |

Supported order statuses:

```text
PENDING, PROCESSING, SHIPPED, COMPLETED, CANCELLED
```

## Testing

Run all backend tests:

```powershell
cd backend
./mvnw.cmd test
```

The test suite uses an in-memory H2 database and covers:

- Spring application context startup
- Order stock and total calculation
- Insufficient stock handling
- JWT registration and protected API access
- Admin authorization boundaries
- Authenticated order history
- OpenAPI availability

Build the frontend:

```powershell
cd frontend
npm run build
```

## Database Migrations

Flyway migrations are located at:

```text
backend/src/main/resources/db/migration/
```

Current migrations create:

- Products and orders
- Application users
- User ownership for orders

For Docker Compose, Flyway is enabled automatically. For manual local schema setup, use `backend/database/ecommerce_setup.sql` and keep Flyway disabled.

## Security Notes

- Do not commit `.env` files or real credentials.
- Use a strong, randomly generated `JWT_SECRET` in non-test environments.
- Replace the example PostgreSQL password before running the application.
- The frontend stores the access token in browser local storage for this portfolio MVP. A production deployment may prefer secure, HTTP-only refresh-token cookies.
- Restrict CORS origins before deploying publicly.

## Project Status

This repository represents a functional ecommerce MVP suitable for portfolio demonstration and continued production hardening. Potential next improvements include refresh-token rotation, payment integration, product images/categories, delivery addresses, rate limiting, CI/CD, and cloud deployment.
