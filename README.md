# parcel-tracking-service

Tracks Shoplane parcels from the seller's hand-over to the carrier until delivery or return.

- Sellers register parcels from the back office once the label is printed.
- Carriers push tracking events through signed webhooks.
- Customers follow their parcel on the public tracking page (status, carrier link, delivery window).
- Downstream services consume parcel events from Kafka (status changes, late parcels).

## Stack

Kotlin 2.4, Spring Boot 4.1, PostgreSQL 16 with Liquibase, Kafka (transactional outbox), OpenFeign client to
`carrier-registry`, Gradle with the Kotlin DSL and a version catalog (`gradle/libs.versions.toml`).

## Running locally

```bash
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=local'
```

The service expects `carrier-registry` on `http://localhost:8091`; point `CARRIER_REGISTRY_URL` elsewhere if needed.

## Build and test

```bash
./gradlew build
```

Unit tests only, no Docker needed. Use cases are tested with the in-memory fakes in `src/test/.../fixtures`.

## API

| Method | Path | Audience |
|---|---|---|
| `GET` | `/public/v1/tracking/{trackingNumber}` | customers (public, rate-limited) |
| `POST` | `/webhooks/v1/carriers/{carrierCode}/events` | carriers (HMAC signature in `X-Carrier-Signature`) |
| `GET` | `/back-office/v1/parcels` | sellers, support agents |
| `POST` | `/back-office/v1/parcels` | sellers |
| `GET` | `/back-office/v1/parcels/lookup?trackingNumber={trackingNumber}` | sellers, support agents |
| `GET` | `/back-office/v1/parcels/{parcelId}` | sellers, support agents |
| `POST` | `/back-office/v1/parcels/{parcelId}/cancel` | sellers, support agents |

Gateway routes for these paths are declared in `deploy/gateway/routes.yaml`.

## Back-office widget

`backoffice-widget/` holds the parcel list and timeline that the seller back office embeds. It reads the back-office
API above with the seller's session and ships from this repository so that API and widget change together.

```bash
cd backoffice-widget && npm test
```

TypeScript, no runtime dependencies, tests with the Node test runner (Node 24 or later).

## Documentation

- `AGENTS.md`: conventions for anyone (human or tool) changing this repository.
- `docs/adr/`: architecture decisions.
- `docs/database-migrations.md`: how to write Liquibase changesets for our large tables.
- `contracts/`: upstream API contracts and the payloads we publish.
