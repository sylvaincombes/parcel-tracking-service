# Late parcel detection

The service flags parcels that are still in flight after their delivery deadline and publishes `parcel.late-detected`.
customer-care opens a goodwill voucher for the customer when it receives the event, and charges it back to the seller
when the seller missed the hand-over (ADR 0002). A parcel flagged too early or twice is a customer-care incident.

## Schedule

`LateParcelScheduler` runs the detection on the cron `parcel-tracking.late-detection.cron`, evaluated in UTC.
`application.yml` sets `0 15 * * * *` (Spring cron, seconds first): every hour at minute 15.

## Which parcels are checked

A run reads parcels that:

- have a pick-up date (`picked_up_on`), the UTC date of the first `PICKED_UP` event reported by the carrier;
- are `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY` or `DELIVERY_FAILED`;
- are not flagged yet (`late_detected_at` is null).

A parcel whose carrier never reports `PICKED_UP` (for instance `REGISTERED` straight to `IN_TRANSIT`) has no pick-up
date and is never checked. Parcels of a carrier unknown to carrier-registry are skipped.

Parcels are read in batches of 500, ordered by parcel id, each batch starting after the last id of the previous one.
Each parcel is checked and flagged in its own transaction.

## Deadline

```
deadline = pick-up date + (carrier max transit days + grace business days) business days
```

- The carrier max transit days come from carrier-registry (`transitDays.max`).
- The grace period is `parcel-tracking.late-detection.grace-business-days` (1 in `application.yml`).
- Business days are Monday to Friday, except the national public holidays listed in
  `parcel-tracking.calendar.public-holidays`. Regional holidays are business days.
- Counting starts the day after pick-up: the pick-up day itself is not counted.

A parcel is late when the current UTC date is strictly after its deadline.

Example: a parcel picked up on Tuesday 2026-11-10 by a carrier with at most 2 transit days, with a grace period of 1
business day, must be delivered within 3 business days. Wednesday 2026-11-11 is a public holiday and the weekend does
not count, so the deadline is Monday 2026-11-16. The parcel is flagged by the first run on Tuesday 2026-11-17 if it is
still in flight.

## Flagging

In the parcel's transaction, the run:

1. sets `late_detected_at` on the parcel and saves it (optimistic locking on `parcel.version`, ADR 0003);
2. publishes `parcel.late-detected` through the outbox (`outbox_message`, ADR 0002).

The outbox relay sends the event to `shoplane.parcel-tracking.parcel-events.v1` with the parcel id as key and the
`event-type` header `parcel.late-detected`. The payload (`parcelId`, `trackingNumber`, `sellerId`,
`deliveryDeadline`, `occurredAt`) is described in `contracts/events/parcel-late-detected.schema.json`.

A parcel is flagged once: flagged parcels are excluded from later runs, and `late_detected_at` keeps the first
detection time. It stays set after delivery, which is how the back office shows a parcel as late.

## Turning it off

Set `parcel-tracking.late-detection.enabled=false`. The property is read at start-up, so the change needs a restart.
It is not set in `application.yml` (detection is on) and is `false` in the `local` profile.

Parcels still in flight that became late while detection was off are flagged by the first run after it is turned
back on.
