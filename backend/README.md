# StayEase Backend and Database

Spring Boot 3 REST API for the StayEase frontend. Uses Java 25, Maven Wrapper, MySQL 8, Spring Data JPA, Flyway, Bean Validation, and springdoc OpenAPI.

## Install and configure MySQL

1. Install MySQL Server 8.x with the MySQL Installer on Windows, or the MySQL Community Server packages for your operating system. Start the MySQL service on port `3306`. MySQL Workbench is optional and can be installed alongside the server.
2. In MySQL Workbench, connect to your local server and create the database and a dedicated application account. Replace the example password before running the SQL:

   ```sql
   CREATE DATABASE stayease_db
     CHARACTER SET utf8mb4
     COLLATE utf8mb4_0900_ai_ci;

   CREATE USER 'stayease_app'@'localhost' IDENTIFIED BY 'replace-with-a-strong-password';
   GRANT ALL PRIVILEGES ON stayease_db.* TO 'stayease_app'@'localhost';
   ```

   Flyway needs schema privileges on this database to create its history table, tables, indexes, and constraints.
3. Copy `.env.example` to `.env` and replace the placeholder password with the account password. `.env` is git-ignored. The included PowerShell scripts read this local file without printing its values; Spring Boot itself does not load `.env` files automatically.

   `dev` uses the local MySQL connection settings. `prod` requires an explicit database host and name and enables TLS. Do not commit real credentials.

## Run the backend

Install Java 25. The Maven Wrapper downloads Maven 3.9.9 automatically; a global Maven installation is not required. From this directory run:

```powershell
.\run-dev.ps1
```

The API listens on port 8080. Swagger UI is at `/swagger-ui.html`; health is at `/actuator/health`. Set the frontend's `VITE_API_BASE_URL` to `http://localhost:8080/api`.

## Schema and migrations

Hibernate uses `ddl-auto=validate`; Flyway is the only schema manager. Existing V1 and V2 migrations are retained so databases that already applied them keep a valid migration history. V3 upgrades the amenity representation to MySQL JSON, removes the former collection table, adds the hotel search index and positive inventory checks, and inserts seven additional hotels and their rooms. Flyway records each migration in `flyway_schema_history` and runs each version once.

The final business schema has three tables:

- `hotels`: hotel details and amenities JSON, with the location/area search index.
- `rooms`: room categories, hotel foreign key, price, inventory, capacity, and optimistic version column.
- `bookings`: room foreign key, unique booking reference, dates, guest details, amount, status, and date/search indexes.

The room lock in a `READ_COMMITTED` booking transaction serializes availability checks and inserts. Overlap is `existing.check_in < requested.check_out AND existing.check_out > requested.check_in`; only `CONFIRMED` rows consume room inventory. Cancellation locks the same room and changes status to `CANCELLED`, releasing inventory in future availability calculations.

The versioned seed data contains 13 hotels total, including 10 across Mumbai, Pune, Goa, Delhi, and Bengaluru, with realistic INR prices and room inventory. Images use Unsplash URLs; the frontend has a local placeholder asset if an image cannot load.

## Verify with MySQL Workbench

After starting the backend once, refresh the schema tree in Workbench and run:

```sql
USE stayease_db;
SHOW TABLES;
SELECT installed_rank, version, description, success
FROM flyway_schema_history ORDER BY installed_rank;
SELECT area, COUNT(*) AS hotels FROM hotels GROUP BY area ORDER BY area;
SELECT COUNT(*) AS requested_city_hotels FROM hotels
WHERE area IN ('Mumbai','Pune','Goa','Delhi','Bengaluru');
SELECT h.name, h.location, JSON_LENGTH(h.amenities) AS amenity_count,
       r.room_type, r.price_per_night, r.total_rooms, r.capacity
FROM hotels h JOIN rooms r ON r.hotel_id = h.id
ORDER BY h.area, h.name, r.price_per_night;
SELECT status, COUNT(*) AS bookings FROM bookings GROUP BY status;
```

To inspect date availability for a room category, substitute its room ID and dates:

```sql
SELECT r.total_rooms - COUNT(b.id) AS rooms_available
FROM rooms r
LEFT JOIN bookings b ON b.room_id = r.id
  AND b.status = 'CONFIRMED'
  AND b.check_in < '2027-01-15'
  AND b.check_out > '2027-01-12'
WHERE r.id = 'h1-r1'
GROUP BY r.id, r.total_rooms;
```

## API endpoints

- `GET /api/hotels`, `GET /api/hotels/{id}`, `GET /api/hotels/search?location=&checkIn=&checkOut=`
- `GET /api/hotels/{id}/availability?checkIn=&checkOut=&guests=`
- `POST /api/bookings`, `GET /api/bookings/{reference}?email=`, `POST /api/bookings/{reference}/cancel?email=`
- `GET /api/recommendations?location=&budget=&amenities=`

Example booking request (the backend gets the room price from the database and computes the total):

```json
{
  "hotelId": "h1",
  "roomName": "Palace Room",
  "name": "Asha Guest",
  "email": "asha@example.com",
  "checkIn": "2027-01-12",
  "checkOut": "2027-01-15",
  "guests": 2
}
```

## Tests and package

`./mvnw.cmd test` runs H2-backed repository and service tests for hotel/room persistence, foreign keys, unique references, date overlap boundaries, cancellation availability, rollback, pricing, date validation, capacity, retrieval/cancellation errors, and two simultaneous requests for the last room. Use `./mvnw.cmd clean package` to build the executable JAR.

After creating and filling `.env`, run `./verify-mysql.ps1` to opt into the MySQL integration test. It checks the actual MySQL version, Flyway V1–V3 status, the three final tables, JSON amenities, foreign keys, sample counts, and a create/retrieve/cancel booking cycle inside a rolled-back test transaction. This check is separate from the default H2 suite and will not run without `RUN_MYSQL_INTEGRATION_TESTS=true` (the script sets this flag).

# AI travel assistant

The React app's `/assistant` page sends messages to `POST /api/assistant/chat`. The Spring service keeps a bounded conversational context in memory and delegates intent extraction through `AiProvider`. Hotel search, nightly prices and date availability always come from `HotelService` and the MySQL hotel/room/booking records; model output is only structured intent/filter data. The model cannot issue SQL or confirm bookings.

By default, `STAYEASE_AI_PROVIDER=local` uses the built-in rule based extractor, which supports common supported city, budget, amenity, date and trip-duration phrases without an external key. For natural language model extraction, set `STAYEASE_AI_PROVIDER=openai` and provide `OPENAI_API_KEY` in the local `backend/.env`; optionally set `OPENAI_MODEL` (default `gpt-4.1-mini`). The key is used only by the backend and is never sent to the browser. The provider uses the OpenAI Responses API with strict JSON schema output. If unavailable, the API returns HTTP 503 and the chat shows a retry message.

The nearby guide currently uses a small curated attraction list linked to official Maharashtra, Goa, Delhi and Karnataka tourism pages. It intentionally does not show hotel-to-attraction distances because those are not currently verified. Day plans organize those sourced entries but do not invent transport, ticket or meal costs. Hotel price cards show listed nightly room prices; totals for requested dates are estimates and are not booking confirmations. A booking is confirmed only by the existing booking API.

Assistant context is held in process memory for up to one hour and is lost on restart; production deployments with multiple backend instances should move session state to a shared store with TTL. Search remains constrained to Mumbai, Pune, Goa, Delhi and Bengaluru in this implementation.

### Assistant API example

```http
POST /api/assistant/chat
Content-Type: application/json

{"message":"Find hotels in Mumbai below ₹5,000","sessionId":null}
```

The response includes a session ID, reply, typed intent, matching database hotels, optional sourced attractions/itinerary, and search details used to carry dates and guest count into booking navigation.
