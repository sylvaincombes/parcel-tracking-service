# 0003 — Optimistic locking on the parcel aggregate

- Status: Accepted
- Date: 2026-04-14

## Context

Carriers deliver webhooks at least once and in parallel: we regularly receive `OUT_FOR_DELIVERY`, `DELIVERED` and
retries of both within the same second for one parcel. The first implementation loaded the parcel, applied the event
and saved it; two concurrent requests could both read `OUT_FOR_DELIVERY` and the last commit won, which once turned
a delivered parcel back into `DELIVERY_FAILED` and re-triggered customer notifications.

## Decision

- `parcel.version` is a JPA `@Version` column. The aggregate carries the version it was read with, and
  `JpaParcelRepository.save` refuses to save a parcel whose version no longer matches the stored one.
- Every state change goes through the aggregate (`Parcel.record`, `Parcel.cancel`, `Parcel.markLate`) and
  `ParcelRepository.save`.
- `OptimisticLockingFailureException` maps to `409 Conflict`. Carriers retry on 409; the retry re-reads the parcel and
  the lifecycle rules (`ParcelStatus.canMoveTo`) decide.

## Consequences

- Hot parcels see occasional 409s; that is expected and visible in the `http.server.requests` metrics.
