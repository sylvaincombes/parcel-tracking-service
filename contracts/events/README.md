# Parcel events

Payloads we publish on `shoplane.parcel-tracking.parcel-events.v1` through the outbox (ADR 0002), one JSON schema per
`event-type` header. Consumers: `notification-service` (`parcel.status-changed`) and `customer-care` (both events).

## Changing a payload

- Adding a property is compatible: consumers ignore properties they do not know, and no schema sets
  `additionalProperties: false`. Add it to the schema in the same merge request as the payload change.
- Removing or renaming a property, changing its type or format, or adding a value to an enum is breaking: consumers
  validate payloads against the schema and drop invalid messages. Publish a `.v2` event type on a new topic and keep
  the `.v1` stream until every consumer has moved.
- Payload property names are the schema property names, camelCase.
