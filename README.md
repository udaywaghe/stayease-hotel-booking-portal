# StayEase Hotel Booking Portal

StayEase is a full-stack hotel discovery and booking application with a conversational travel guide. It uses React and Vite in the browser, a Spring Boot REST API, MySQL for hotel inventory and bookings, and Flyway for database migrations.

## Features

- Browse hotels, inspect rooms and amenities, and create or manage bookings.
- Check date availability using confirmed bookings and prevent overbooking with a transactional room lock.
- Ask the StayEase Travel Guide for hotel searches, price comparisons, sourced city attractions, and simple itineraries.
- Recommendations and prices come from database records. The assistant never confirms a booking; the booking API does that.
- Attraction cards link to official tourism sources. Distances are omitted when they are not verified.

## Screenshots

The `screenshots/` folder contains screenshots of the running app and assistant entry point.

## Prerequisites

- Java 25.
- Node.js 20.19+ or 22.12+ and npm. These versions satisfy the Vite 7 requirement. ([Vite documentation](https://vite.dev/guide/))
- MySQL Server 8.x on port 3306.
- Internet access on first backend startup so the Maven Wrapper can download Maven and dependencies.

A global Maven installation is not required; the backend includes `mvnw.cmd`.

## Run locally on Windows

### 1. Clone the repository

```powershell
git clone <your-github-repository-url>
cd StayEase
```

### 2. Create the MySQL database and app user

Start MySQL, then run this once in MySQL Workbench or the MySQL command line. Replace the example password with a strong local password.

```sql
CREATE DATABASE stayease_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER 'stayease_app'@'localhost' IDENTIFIED BY 'replace-with-a-strong-password';
GRANT ALL PRIVILEGES ON stayease_db.* TO 'stayease_app'@'localhost';
```

Flyway needs schema privileges for the app account so it can create the tables and migration history.

### 3. Configure backend environment

```powershell
cd backend
Copy-Item .env.example .env
notepad .env
```

Set `DB_USERNAME` and `DB_PASSWORD` to the MySQL account above. The `.env` file is ignored by Git. Keep real database and AI credentials out of source control.

By default the assistant uses its local intent extractor and needs no model key. To use OpenAI intent extraction, set `STAYEASE_AI_PROVIDER=openai` and provide `OPENAI_API_KEY` in this local `.env` file.

### 4. Start the backend

In the `backend` directory:

```powershell
.\run-dev.ps1
```

On startup, Flyway creates or updates the schema and inserts versioned sample hotel data. The API is at `http://localhost:8080`. Check `http://localhost:8080/actuator/health` for the health status and `http://localhost:8080/swagger-ui.html` for API documentation.

### 5. Start the frontend

Open a second PowerShell window at the repository root:

```powershell
cd react-project
Copy-Item .env.example .env
npm ci
npm run dev
```

Open the Vite URL printed in the terminal, usually `http://localhost:5173`. `VITE_API_BASE_URL` defaults to `http://localhost:8080/api`.

### 6. Try the main flow

1. Browse stays and open a hotel detail page.
2. Use **Explore city attractions** or the floating **Ask AI** icon.
3. Try a request such as: `Find hotels in Goa below ₹30,000`.
4. For availability, include both dates, for example: `Find hotels in Goa from 2027-12-10 to 2027-12-12`.
5. Open a recommendation and continue to the booking page. A reservation is confirmed only after the booking API accepts it.

The current hotel catalog has 13 seeded hotels, including 10 in Mumbai, Pune, Goa, Delhi, and Bengaluru. The catalog and prices are demo inventory, not live hotel offers.

## Database and migrations

Flyway migrations are in `backend/src/main/resources/db/migration/` and run automatically when Spring Boot starts:

- `V1__create_booking_schema.sql` creates the initial hotel, room, and booking schema.
- `V2__seed_demo_hotels.sql` inserts the original sample inventory.
- `V3__finalize_mysql_hotel_inventory.sql` applies the current schema adjustments and additional sample inventory.

The application uses three business tables: `hotels`, `rooms`, and `bookings`. See [backend/README.md](backend/README.md) for schema details and MySQL Workbench verification queries.

## Tests and checks

Run backend tests from `backend`:

```powershell
.\mvnw.cmd test
```

Run the opt-in MySQL integration tests against the configured local database:

```powershell
.\verify-mysql.ps1
```

Run frontend checks from `react-project`:

```powershell
npm run lint
npm run build
```

## Project layout

```text
backend/       Spring Boot API, JPA entities, services, tests, Flyway migrations
react-project/ React application, Vite config, UI and API clients
screenshots/   App screenshots for the project overview
DESIGN.md      Architecture, schema, booking and assistant design discussion
```

## Design

See [DESIGN.md](DESIGN.md) for architecture, database relationships, API responsibilities, booking concurrency, AI tool boundaries, and known limitations.
