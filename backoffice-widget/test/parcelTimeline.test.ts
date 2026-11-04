import assert from "node:assert/strict";
import { test } from "node:test";
import { toTimeline } from "../src/parcelTimeline.ts";
import { parcelDetail } from "./fixtures.ts";

test("lists the most recent event first", () => {
  const timeline = toTimeline(parcelDetail());

  assert.deepEqual(
    timeline.map((entry) => entry.title),
    ["Delivery attempt failed", "Arrived at a sorting hub", "Picked up by the carrier"],
  );
});

test("explains failed attempts with the attempt reason and other events with their location", () => {
  const [failedAttempt, hub] = toTimeline(parcelDetail());

  assert.equal(failedAttempt.detail, "Customer not at home");
  assert.equal(hub.detail, "Hub Saint-Priest");
});

test("falls back to the raw event type when it has no label", () => {
  const timeline = toTimeline(
    parcelDetail({ events: [{ type: "HELD_AT_CUSTOMS", occurredAt: "2026-11-03T08:00:00Z", location: null, attemptReason: null }] }),
  );

  assert.equal(timeline[0].title, "HELD_AT_CUSTOMS");
});
