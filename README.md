# Receipt Manager

A full stack app for recording receipts and reviewing monthly spending. This project is also a hands-on way to learn Spring Boot: the backend uses REST controllers, services, repositories, JPA entities, validation, Spring Security, and database migrations.

## Features

- Register and log in with a user account.
- Create, view, update, and delete receipts, including store, date, category, and item details.
- Browse receipts with pagination.
- View monthly spending summaries, including totals by store.
- Export a monthly report as CSV or PDF.
- Keep each user's receipts protected by JWT-based authentication.

## Tech stack

- **Backend:** Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, Bean Validation, Maven
- **Authentication:** JSON Web Tokens (JWT)
- **Database:** MySQL 8, Flyway migrations
- **PDF reports:** OpenPDF
- **Frontend:** React 19, Vite, React Router, Axios
- **Local services:** Docker Compose

## Run with Docker Compose

Requirements: Docker Desktop (or Docker Engine with the Compose plugin).

1. From the project root, create your local environment file:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Start the app:

   ```powershell
   docker compose up --build
   ```

3. Open the frontend at <http://localhost:5173>. The backend API is at <http://localhost:8080>.

Compose starts MySQL and waits for its health check before starting the backend. On first startup, Flyway applies the SQL migrations. To stop the services, press `Ctrl+C`, then run `docker compose down`. The database data is stored in a named Docker volume; `docker compose down -v` also deletes that data.

The checked-in `.env.example` values are for local development only. Change the database passwords and JWT secret before using this app in a shared or production environment.

## Run locally for development

Requirements: Java 17, Maven, Node.js 20+, npm, and Docker Compose (for MySQL).

1. Create `.env` from `.env.example` as above, then start just the database:

   ```powershell
   docker compose up -d mysql
   ```

2. In a terminal, start the backend:

   ```powershell
   cd backend
   mvn spring-boot:run
   ```

   The API listens on port `8080`. Its database and JWT settings have development defaults in `application.yml`, matching `.env.example`.

3. In a second terminal, start the frontend:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

4. Open the Vite URL printed in the terminal, normally <http://localhost:5173>. Vite proxies `/api` requests to the backend on port `8080`.

## Learning your way around the Spring Boot backend

The backend source is under `backend/src/main/java/com/receiptmanager`:

- `controller/` maps HTTP routes to application operations.
- `service/` contains receipt, authentication, and report logic.
- `repository/` uses Spring Data JPA to access the database.
- `entity/` defines the JPA database model; `dto/` defines API request and response shapes.
- `security/` handles JWT creation and authentication; `config/` configures Spring Security and CORS.
- `exception/` centralizes API error responses.
- `backend/src/main/resources/db/migration/` contains Flyway schema migrations. The app validates the schema against the JPA model at startup.

Useful API routes (receipt and report routes require a bearer token after login):

| Method | Route | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Create an account |
| `POST` | `/api/auth/login` | Log in and receive a JWT |
| `GET`, `POST` | `/api/receipts` | List (paginated) or create receipts |
| `GET`, `PUT`, `DELETE` | `/api/receipts/{id}` | Read, update, or delete one receipt |
| `GET` | `/api/reports/monthly?year=2026&month=10` | Get a monthly summary |
| `GET` | `/api/reports/monthly/export?year=2026&month=10` | Download the summary as CSV |
| `GET` | `/api/reports/monthly/export/pdf?year=2026&month=10` | Download the summary as PDF |


