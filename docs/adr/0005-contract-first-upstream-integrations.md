# 0005 — Contract-first upstream integrations

- Status: Accepted
- Date: 2026-06-02

## Context

We depend on `carrier-registry` (owned by the Carrier Integrations team) for carrier names, tracking URL templates,
transit times and lifecycle (active or retired). Early on we guessed field optionality from sample payloads and
littered the code with fallbacks for values the registry always sends.

## Decision

- The upstream OpenAPI document is vendored in `contracts/<service>/openapi.yaml` and updated in the same merge request
  as any client change. It is the source of truth for what can be absent.
- DTOs mirror the contract. When the contract uses `oneOf` variants, the DTO flattens them and fields that only exist
  in some variants become nullable in Kotlin. Code that has already established the variant (for instance
  `status == RETIRED`) relies on that variant's required fields.
- We do not add defensive fallbacks for values the contract marks required. A contract violation is the upstream's
  incident: it surfaces as a 5xx and an alert, not as silently degraded data.
- Absence that the contract allows (unknown carrier: `404`) is modelled explicitly, as `CarrierDirectory.findCarrier`
  returning `null`.

## Consequences

- Reviewers check client changes against the vendored contract, not against intuition about what "might be null".
- The Carrier Integrations team notifies consumers before any breaking change; contract updates land here first.
