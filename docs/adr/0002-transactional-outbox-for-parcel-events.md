# 0002 — Transactional outbox for parcel events

- Status: Accepted
- Date: 2026-03-17

## Context

Downstream services react to parcel events:

- `notification-service` emails and pushes status changes to customers;
- `customer-care` opens a goodwill voucher for the customer when a parcel is reported late (`parcel.late-detected`)
  and charges it back to the seller when the seller missed the hand-over.

Publishing to Kafka inside the request would either lose events (crash after commit) or publish events for
transactions that roll back.

## Decision

Use cases publish domain events through `ParcelEventPublisher`. The implementation writes an `outbox_message` row in
the same transaction (`Propagation.MANDATORY`, so publishing outside a transaction fails fast). `OutboxRelay` sends
pending rows to Kafka every second, in id order, and marks them published.

Message key is the parcel id, so all events of a parcel land on the same partition in order. Payloads are described
in `contracts/events/`.

## Consequences

- Delivery is at-least-once; consumers deduplicate on `(parcelId, eventType, occurredAt)`.
- A late-detection event has a direct cost (voucher). Emitting it too early or twice is a customer-care incident, not
  a cosmetic bug.
- Published rows are purged after 7 days by a database job owned by the platform team.
