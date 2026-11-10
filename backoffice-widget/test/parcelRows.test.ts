import assert from "node:assert/strict";
import { test } from "node:test";
import { toParcelRows } from "../src/parcelRows.ts";
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

test("lists the rows newest first unless asked otherwise", () => {
  const parcels = [
    parcelSummary({ parcelId: "earlier", registeredAt: "2026-11-02T09:15:00Z" }),
    parcelSummary({ parcelId: "later", registeredAt: "2026-11-03T10:00:00Z" }),
  ];

  assert.deepEqual(
    toParcelRows(parcels).map((row) => row.parcelId),
    ["later", "earlier"],
  );
  assert.deepEqual(
    toParcelRows(parcels, "oldest").map((row) => row.parcelId),
    ["earlier", "later"],
  );
});
