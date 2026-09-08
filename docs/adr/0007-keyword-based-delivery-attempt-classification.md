# 0007 — Keyword-based delivery attempt classification

- Status: Accepted
- Date: 2026-09-07

## Context

Carriers report failed delivery attempts with free text in English or French. Sellers asked to see why an attempt
failed so they can contact the customer (wrong address, nobody home, refused). The reason is a hint shown in the back
office only; it does not drive the parcel lifecycle, notifications or compensation.

## Decision

- `DeliveryAttemptClassifier` matches lower-cased substrings against an ordered list of rules; the first matching rule
  wins, otherwise `OTHER`.
- No NLP, no language detection, no fuzzy matching. `OTHER` is always a safe answer: the back office then shows the
  carrier's raw message.
- Known limitations are accepted and pinned in `DeliveryAttemptClassifierTest.KnownLimitations`: rule order beats
  meaning, negations are ignored, only English and French keywords, typos fall back to `OTHER`. Changing a pinned case
  is a product decision.
- A new rule or keyword must not change the reason of messages that existing rules already classify; overlaps are
  resolved by rule order and covered by a test.

## Consequences

- Misclassification costs a wrong hint in the back office, nothing else.
- Adding a carrier language means adding keywords, not a new mechanism.
