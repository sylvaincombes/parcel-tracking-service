import assert from "node:assert/strict";
import { test } from "node:test";
import { sortByRegistration } from "../src/sortParcels.ts";
import { parcelSummary } from "./fixtures.ts";

const parcels = [
  parcelSummary({ parcelId: "tuesday", registeredAt: "2026-11-03T10:00:00Z" }),
  parcelSummary({ parcelId: "monday", registeredAt: "2026-11-02T09:15:00Z" }),
  parcelSummary({ parcelId: "wednesday", registeredAt: "2026-11-04T16:45:00Z" }),
];

test("lists the most recently registered parcels first", () => {
  const sorted = sortByRegistration(parcels, "newest");

  assert.deepEqual(
    sorted.map((parcel) => parcel.parcelId),
    ["wednesday", "tuesday", "monday"],
  );
});

test("lists the earliest registered parcels first", () => {
  const sorted = sortByRegistration(parcels, "oldest");

  assert.deepEqual(
    sorted.map((parcel) => parcel.parcelId),
    ["monday", "tuesday", "wednesday"],
  );
});

test("keeps the API order of parcels registered at the same instant in both directions", () => {
  const sameInstant = [
    parcelSummary({ parcelId: "first", registeredAt: "2026-11-02T09:15:00Z" }),
    parcelSummary({ parcelId: "second", registeredAt: "2026-11-02T09:15:00.000Z" }),
    parcelSummary({ parcelId: "third", registeredAt: "2026-11-02T09:15:00Z" }),
  ];

  assert.deepEqual(
    sortByRegistration(sameInstant, "newest").map((parcel) => parcel.parcelId),
    ["first", "second", "third"],
  );
  assert.deepEqual(
    sortByRegistration(sameInstant, "oldest").map((parcel) => parcel.parcelId),
    ["first", "second", "third"],
  );
});

test("orders registration instants written with a varying number of fractional digits", () => {
  const precise = [
    parcelSummary({ parcelId: "half-second", registeredAt: "2026-11-02T09:15:00.5Z" }),
    parcelSummary({ parcelId: "whole-second", registeredAt: "2026-11-02T09:15:00Z" }),
    parcelSummary({ parcelId: "microseconds", registeredAt: "2026-11-02T09:15:00.123456Z" }),
  ];

  assert.deepEqual(
    sortByRegistration(precise, "oldest").map((parcel) => parcel.parcelId),
    ["whole-second", "microseconds", "half-second"],
  );
});

test("returns a new list and leaves the given one untouched", () => {
  const given = [...parcels];

  const sorted = sortByRegistration(given, "oldest");

  assert.notEqual(sorted, given);
  assert.deepEqual(given, parcels);
});
