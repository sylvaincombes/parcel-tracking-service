# 0004 — Back-office ownership checks live in the service

- Status: Accepted
- Date: 2026-05-12

## Context

The seller back office calls `/back-office/v1/**`. The API gateway authenticates the back-office session and forwards
the identity as `X-Backoffice-User-Id`, `X-Backoffice-Seller-Id` and `X-Backoffice-Roles` (client-supplied values are
stripped). The gateway knows who the user is, not which parcels belong to which seller. Parcel ids are UUIDs but they
leak: they appear in exports, support tickets and email links. Ids are not a secret.

## Decision

- Every back-office use case that reads or changes a parcel loads it, then calls `ParcelAccessPolicy` before returning
  or mutating anything: `requireCanView` for reads, `requireCanManage` for changes.
- Listing endpoints resolve the seller scope with `ParcelAccessPolicy.resolveSellerScope`; sellers never pass another
  seller's id.
- `SUPPORT_AGENT` users can view and manage every parcel. Seller users without `SELLER_ADMIN` or `SELLER_OPERATOR`
  are read-only.
- The check lives in the application layer, not in controllers, so that every entry point (REST, future batch
  imports) gets it.

## Consequences

- A new back-office use case without a `ParcelAccessPolicy` call is a cross-seller data leak or tampering bug.
- Tests of each back-office use case include the "another seller" case.
