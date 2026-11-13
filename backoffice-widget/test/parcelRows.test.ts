import assert from "node:assert/strict";
import { test } from "node:test";
import { countOutForDeliveryToday, toParcelRows } from "../src/parcelRows.ts";
import { parcelSummary } from "./fixtures.ts";

test("shows the status label and the registration day in Paris time", () => {
  const [row] = toParcelRows([parcelSummary({ status: "OUT_FOR_DELIVERY", registeredAt: "2026-11-02T23:30:00Z" })]);

  assert.equal(row.statusLabel, "Out for delivery");
  assert.equal(row.registeredOn, "03/11/2026");
});

test("shows the late badge only for parcels flagged late", () => {
  const rows = toParcelRows([
    parcelSummary({ parcelId: "on-time", late: false }),
    parcelSummary({ parcelId: "late", late: true }),
  ]);

  assert.deepEqual(
    rows.map((row) => [row.parcelId, row.showLateBadge]),
    [
      ["on-time", false],
      ["late", true],
    ],
  );
});

test("highlights only the parcels out for delivery today", () => {
  const rows = toParcelRows([
    parcelSummary({ parcelId: "in-transit", status: "IN_TRANSIT" }),
    parcelSummary({ parcelId: "out-for-delivery", status: "OUT_FOR_DELIVERY" }),
    parcelSummary({ parcelId: "delivered", status: "DELIVERED" }),
  ]);

  assert.deepEqual(
    rows.map((row) => [row.parcelId, row.outForDeliveryToday]),
    [
      ["in-transit", false],
      ["out-for-delivery", true],
      ["delivered", false],
    ],
  );
});

test("counts the parcels out for delivery today for the header chip", () => {
  const rows = toParcelRows([
    parcelSummary({ parcelId: "first", status: "OUT_FOR_DELIVERY" }),
    parcelSummary({ parcelId: "second", status: "DELIVERY_FAILED" }),
    parcelSummary({ parcelId: "third", status: "OUT_FOR_DELIVERY" }),
  ]);

  assert.equal(countOutForDeliveryToday(rows), 2);
});

test("counts no parcel out for delivery in an empty list", () => {
  assert.equal(countOutForDeliveryToday(toParcelRows([])), 0);
});
