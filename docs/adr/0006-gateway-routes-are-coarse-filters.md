# 0006 — Gateway routes are coarse filters

- Status: Accepted
- Date: 2026-07-06

## Context

Routes for this service are declared in `deploy/gateway/routes.yaml` and applied by the platform team's gateway.
We first wrote precise regexes per path parameter (UUID pattern, tracking number pattern). A path that did not match
fell through to the gateway's generic HTML 404, so clients got no structured error, and every change to an id format
needed a gateway change reviewed by the platform team.

## Decision

- Route paths are anchored regexes (`^...$`) where every path parameter is matched with `[^/]+`, whatever its type.
- The service validates parameters and answers with structured errors: typed `@PathVariable` (UUID binding gives a 400
  `invalid_request`), value objects such as `TrackingNumber.of` (400), unknown resources (404 `parcel_not_found`).
- A route lists its HTTP methods explicitly and its `auth` mode: `none` for public and webhook routes,
  `backoffice-session` for `/back-office/**`.

## Consequences

- The gateway regexes are deliberately permissive; tightening one route breaks the uniform contract with clients and
  the platform tooling that lints this file (it expects `[^/]+` for parameters).
- Input validation lives in one place: the service.
