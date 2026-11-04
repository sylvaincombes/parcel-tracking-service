# AGENTS.md

Conventions for working on `parcel-tracking-service`. Read this before changing code or reviewing a merge request.

## Layout

Hexagonal (ADR 0001): `domain` (no framework), `application` (use cases, transactions, authorization), `infra`
(adapters, Spring config), `presentation` (controllers, DTOs). Dependencies point inwards. Controllers call use cases,
never repositories.

## Commands

- `./gradlew build`: compile and run all tests.
- `./gradlew test --tests '*ParcelTest'`: a single test class.
- `docker compose up -d` then `./gradlew bootRun --args='--spring.profiles.active=local'`.
- `cd backoffice-widget && npm test`: back-office widget tests (Node 24+).

## Back-office widget

`backoffice-widget/` is the TypeScript view of the back-office API (`/back-office/v1/**`) embedded in the seller back
office. Its types in `src/api.ts` mirror the JSON of the controllers. New fields are ignored until the widget uses
them; renaming or removing a field, or changing an enum value or a limit, needs the widget change in the same merge
request.

## Parcel lifecycle

- All state changes go through the `Parcel` aggregate and `ParcelRepository.save`, which enforces optimistic locking
  on `parcel.version` (ADR 0003). Concurrent updates surface as `409 Conflict`; carriers retry.
- Allowed transitions are in `ParcelStatus.canMoveTo`. Terminal statuses: `DELIVERED`, `RETURNED_TO_SENDER`,
  `CANCELLED`.
- Domain events go through `ParcelEventPublisher` inside the use case transaction (outbox, ADR 0002). Never call
  `KafkaTemplate` from a use case.

## Back office

Every back-office use case that touches a parcel calls `ParcelAccessPolicy` (ADR 0004): `requireCanView` for reads,
`requireCanManage` for changes, `resolveSellerScope` for listings. Include an "another seller" test.

## Database migrations

See `docs/database-migrations.md`. In short:

- Changesets on `main` are immutable; fix forward with a new changeset.
- Changesets that are not on `main` yet (added on your branch or on the branch you stack on) may be edited in place;
  only `main` is ever deployed. Check with `git log origin/main -- <file>`.
- Every changeset has an explicit `rollback` matching what it does.
- `parcel` and `tracking_event` are large and hot: no backfill `UPDATE`, no blocking `NOT NULL` or index creation in a
  changeset; use `CREATE INDEX CONCURRENTLY` with `runInTransaction: false`.

## Upstream contracts

`contracts/carrier-registry/openapi.yaml` is the source of truth for the registry (ADR 0005). Rely on what it marks
required, including variant-specific fields once the variant is known; model only the absence it allows.

## Gateway routes

`deploy/gateway/routes.yaml` (ADR 0006): anchored regexes, every path parameter matched with `[^/]+`, explicit methods
and `auth`. Parameter validation belongs to the service.

## Tests

- JUnit 5, AssertJ, MockK for third-party types only; our own ports use the fakes in `src/test/.../fixtures`.
- Test names are sentences describing behavior (`` `a delivered parcel is never late` ``).
- `DeliveryAttemptClassifierTest.KnownLimitations` pins accepted gaps of the classifier (ADR 0007). Those assertions
  describe current, accepted behavior; do not "fix" them without a product decision.

## Commits and merge requests

Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `build:`, `chore:`). One topic per merge
request; schema change, code and tests in the same merge request.
