# StayEase Design Discussion

## Goals

StayEase provides a straightforward hotel discovery and reservation experience backed by a relational inventory model. The assistant improves discovery while preserving the existing booking workflow and its server-side rules.

## Overall architecture

```text
React + Vite
  ├── hotel browsing, detail, booking and manage-booking pages
  └── floating Ask AI launcher → assistant chat page
          │ JSON over HTTP
          ▼
Spring Boot REST API
  ├── controllers: HTTP validation and response mapping
  ├── services: hotel search, availability, bookings, assistant orchestration
  ├── repositories: Spring Data JPA queries
  └── replaceable AI intent provider
          │ JPA / Hibernate
          ▼
MySQL 8 ── schema changes and sample data managed by Flyway
```

The React client is responsible for presentation and navigation. Spring Boot owns business rules, price and availability calculations, and booking confirmation. The AI provider extracts structured intent; it does not access repositories or compose SQL.

## Database design

The business schema uses three tables:

- **hotels** stores name, location, area, descriptive content, image URL, rating and amenities.
- **rooms** represents a room category within a hotel. It holds the nightly price, category inventory and maximum guest capacity. Each room row references one hotel.
- **bookings** references one room category and stores the booking reference, customer, dates, guest count, total, state and creation time.

This design keeps the catalog small and matches the existing application model. A hotel has many room categories; a room category has many bookings. Foreign keys protect those relationships. Booking references are unique. Monetary values use decimal precision. Amenities are stored as MySQL JSON on the hotel record.

Flyway owns schema changes, and Hibernate validates the schema at startup. Versioned sample data is inserted once; it is not recreated on every application launch.

## Booking and availability

Availability uses the half-open date overlap rule:

```text
existing.check_in < requested.check_out
AND existing.check_out > requested.check_in
```

Only `CONFIRMED` bookings consume room category inventory. Cancelled bookings are excluded, so cancellation releases availability. For booking creation the backend starts a transaction, locks the room row, counts overlapping confirmed bookings, and inserts only if inventory remains. This serializes concurrent booking attempts for the same room category. Database constraints remain a final integrity boundary.

The backend calculates the booking amount from the selected database room price and dates. Client-supplied prices are not authoritative. The assistant only links to the booking flow; it never issues a booking reference or announces success.

## API design

The REST API is grouped by resource and use case:

- `/api/hotels` lists and searches hotel records.
- `/api/hotels/{id}` retrieves a hotel and its room categories.
- `/api/hotels/{id}/availability` checks rooms for dates and guest count.
- `/api/bookings` creates a reservation; reference lookup and cancellation use dedicated routes.
- `/api/assistant/chat` accepts a message and optional session ID, then returns a reply, typed intent, database hotel cards, optional sourced attractions/itinerary, estimate note and search context.

Controllers validate request shape. Services apply application rules. API errors are returned in a consistent error response; model-provider outages return HTTP 503.

## Travel assistant design

The assistant uses a provider interface so structured intent extraction can be replaced. The local provider supports common phrases without credentials. An optional OpenAI provider uses strict JSON schema output for intent fields such as city, budget, dates, guests, amenities and trip length.

After extraction, the Spring service validates the fields and calls allowlisted backend services. Hotel names, room prices and availability come from MySQL. Date searches filter to room categories that meet guest capacity and are available for the requested dates. “Cheaper” follow-ups retain the current search context. Attractions come from a curated local catalog with official tourism source links. Distances are omitted unless verified. Itineraries organize catalog entries and do not invent transit or entry costs.

Conversation state is held in process memory for up to one hour and capped to limit growth. It is suitable for a single-process demo; a multi-instance deployment should use a shared TTL store such as Redis.

## Key design decisions

- **Three-table business schema:** hotels, room categories and bookings are enough for this version; no unnecessary customer or payment tables were added.
- **Room categories as inventory units:** inventory is tracked by category, matching the existing frontend’s room selection. Individual physical room assignment is outside scope.
- **Server-owned booking and pricing:** this avoids trusting stale or modified browser data.
- **Pessimistic room lock:** serializes capacity checks for same-category booking attempts; it trades some contention for predictable protection from overbooking.
- **Optional model provider:** the app remains usable in local development without an API key. Model output only selects intent and filters.
- **Curated attractions:** official source links provide a safer demo than unverified map distances or invented places.
- **Floating assistant launcher:** the assistant stays reachable without taking a permanent navbar tab; a dedicated chat route remains available.

## Limitations and next steps

- Seed hotels and prices are demo records, not live inventory or guaranteed market rates.
- The local intent extractor recognizes a limited vocabulary. Configure the optional model provider for more flexible language understanding.
- Attraction data is a small city-level catalog. It does not provide verified hotel-specific distances, opening-hour status, transport routes or ticket prices.
- Session context is in memory and resets on restart. Production needs shared, authenticated session storage and retention controls.
- There is no user authentication, payment processing, external hotel channel integration, or individual room assignment.
- Production hardening should add rate limiting, structured observability, provider cost limits, secret rotation, deployment automation and end-to-end browser tests.
