# StayEase Design

## Architecture

The React/Vite frontend handles hotel browsing, search, booking, cancellation, and the travel assistant. It sends JSON requests to the Spring Boot REST API. Controllers validate requests, services apply business rules, and Spring Data JPA/Hibernate reads and writes MySQL. Flyway manages schema and sample-data migrations. The assistant uses hotel and availability services; it cannot query the database directly or confirm bookings.

## Database design

The schema has three business tables: `hotels`, `rooms`, and `bookings`. A hotel has many room categories; each category has inventory, nightly price, and guest capacity. A category can have many bookings over time. Foreign keys enforce these relationships. Required fields, unique booking references, date and positive-value checks, indexes, and `DECIMAL(12,2)` prices protect data integrity. Amenities are stored as JSON on hotels. Flyway seeds demo data once; Hibernate validates the schema.

## API design

- `GET /api/hotels`, `/api/hotels/search`, `/api/hotels/{id}`: list, search, and retrieve hotel details.
- `GET /api/hotels/{id}/availability`: check rooms for dates and guest count.
- `GET /api/recommendations`: filter and rank hotels by location, budget, and amenities.
- `POST /api/bookings`: create a reservation; `GET /api/bookings/{reference}?email=...` retrieves it; `POST /api/bookings/{reference}/cancel?email=...` cancels it.
- `POST /api/assistant/chat`: return a conversational reply with structured hotel, attraction, itinerary, and search information as applicable.

Invalid requests return client errors; unavailable inventory and duplicate cancellation return conflicts. AI provider failures return a service-unavailable response.

## Booking logic and simultaneous requests

Check-in cannot be in the past, and check-out must be later than check-in. Nights are the date difference. A booking overlaps requested dates when `existing.check_in < requested.check_out` and `existing.check_out > requested.check_in`. Only confirmed bookings count; cancelled bookings release inventory. A category is available when overlapping bookings are fewer than its room count and its capacity fits the guest count.

The backend calculates the accommodation total from the database nightly price times the number of nights; the browser cannot set the price. The current API total excludes taxes and fees, while the page displays a separate 12% estimate. These should be unified in a server-side quote before payments are added.

Booking creation runs in a transaction and locks the room-category row before checking occupancy. If two users request the last room, one obtains the lock and books it; the other checks after that transaction commits, sees full inventory, and receives a conflict. Search availability can become stale, so the create request always checks again under the lock. Cancellation locks the booking and room, then marks the booking cancelled.

## Design choices

MySQL and foreign keys suit the related hotel, inventory, and booking data. Tracking interchangeable room categories keeps the schema simple; assigning physical rooms would require more inventory and housekeeping logic. Pessimistic locking is straightforward and protects the final unit, though it can limit throughput for popular categories. Flyway makes schema changes repeatable. A rule-based assistant works without an API key; an optional model provider understands more varied wording but adds cost and network dependency. Curated attractions avoid invented places and distances, but cover fewer locations than a maintained Places service.

## Surprise feature: travel assistant

A floating **Ask AI** button keeps trip help available without adding a permanent navigation tab. The dedicated chat page accepts natural language and keeps short-lived conversation context. A replaceable provider extracts structured details such as destination, budget, dates, guests, and amenities. Backend services retrieve real hotel prices and availability; curated attraction data includes source links. The assistant can guide users to booking, but only the booking API can confirm a reservation.

## Limitations and improvements

Hotel inventory and prices are demo data. There is no authentication, payment/refund flow, email confirmation, or physical-room assignment; reference-plus-email lookup is not strong production identity verification. The assistant has limited local intent rules and attraction coverage, and its in-memory context is lost on restart. Next steps are server-owned tax and fee quotes, stronger booking verification, shared conversation storage, a maintained attractions provider, and load-tested concurrency and end-to-end coverage.
