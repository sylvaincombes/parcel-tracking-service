# 0001 — Hexagonal layout with a framework-free domain

- Status: Accepted
- Date: 2026-03-02

## Context

The service starts small (register parcels, ingest carrier events, expose tracking) but will grow integrations:
carrier webhooks, the carrier registry, Kafka consumers of parcel events, the seller back office. We want the parcel
lifecycle rules to stay testable without a Spring context or a database.

## Decision

Four packages under `com.shoplane.parceltracking`:

- `domain`: aggregates, value objects, domain services and ports (`ParcelRepository`, `CarrierDirectory`,
  `ParcelEventPublisher`). No Spring, no JPA, no Jackson.
- `application`: use cases. They orchestrate the domain, own transactions (`@Transactional`) and authorization
  (`application.access`).
- `infra`: adapters implementing the ports (JPA, outbox and Kafka relay, Feign client to the carrier registry),
  Spring configuration, schedulers.
- `presentation`: REST controllers, request/response DTOs, webhook signature checks, error mapping.

Dependencies point inwards only. Controllers call use cases, never repositories.

## Consequences

- Domain services (`BusinessCalendar`, `EtaCalculator`, `LateParcelDetector`, `DeliveryAttemptClassifier`) are plain
  classes wired in `infra.config.DomainConfig`.
- Unit tests use in-memory fakes from `src/test/.../fixtures` instead of mocks of our own ports.
