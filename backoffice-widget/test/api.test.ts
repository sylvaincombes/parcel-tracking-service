import assert from "node:assert/strict";
import { test } from "node:test";
import { PAGE_SIZE, ParcelsApiError, parcelsApi } from "../src/api.ts";
import { parcelSummary } from "./fixtures.ts";

function recordingFetch(response: Response) {
  const requested: string[] = [];
  const fetchFn = async (input: string) => {
    requested.push(input);
    return response;
  };
  return { requested, fetchFn };
}

test("requests one page of parcels with the widget page size", async () => {
  const { requested, fetchFn } = recordingFetch(Response.json([parcelSummary()]));

  const parcels = await parcelsApi("https://backoffice.shoplane.example", fetchFn).listParcels(2);

  assert.deepEqual(requested, [`https://backoffice.shoplane.example/back-office/v1/parcels?page=2&size=${PAGE_SIZE}`]);
  assert.equal(parcels[0].trackingNumber, "CW4481207795FR");
});

test("surfaces API errors with their HTTP status", async () => {
  const { fetchFn } = recordingFetch(new Response('{"code":"access_denied"}', { status: 403 }));

  await assert.rejects(parcelsApi("", fetchFn).getParcel("6f1c2b9e-3d4a-4c5b-9e8f-1a2b3c4d5e6f"), (error: unknown) => {
    assert.ok(error instanceof ParcelsApiError);
    assert.equal(error.status, 403);
    return true;
  });
});
