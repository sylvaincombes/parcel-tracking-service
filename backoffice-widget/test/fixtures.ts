import type { ParcelDetail, ParcelSummary } from "../src/api.ts";

export function parcelSummary(overrides: Partial<ParcelSummary> = {}): ParcelSummary {
  return {
    parcelId: "6f1c2b9e-3d4a-4c5b-9e8f-1a2b3c4d5e6f",
    trackingNumber: "CW4481207795FR",
    carrierCode: "colisway",
    orderReference: "SL-2026-0048213",
    status: "IN_TRANSIT",
    late: false,
    registeredAt: "2026-11-02T09:15:00Z",
    ...overrides,
  };
}

export function parcelDetail(overrides: Partial<ParcelDetail> = {}): ParcelDetail {
  return {
    parcelId: "6f1c2b9e-3d4a-4c5b-9e8f-1a2b3c4d5e6f",
    trackingNumber: "CW4481207795FR",
    carrierCode: "colisway",
    sellerId: 4417,
    orderReference: "SL-2026-0048213",
    status: "DELIVERY_FAILED",
    deliveryInstructions: null,
    pickedUpOn: "2026-11-02",
    deliveredAt: null,
    lateDetectedAt: null,
    registeredAt: "2026-11-02T09:15:00Z",
    events: [
      { type: "PICKED_UP", occurredAt: "2026-11-02T16:40:00Z", location: "Lyon Gerland", attemptReason: null },
      { type: "ARRIVED_AT_HUB", occurredAt: "2026-11-03T04:10:00Z", location: "Hub Saint-Priest", attemptReason: null },
      {
        type: "DELIVERY_ATTEMPT_FAILED",
        occurredAt: "2026-11-04T11:25:00Z",
        location: "Villeurbanne",
        attemptReason: "RECIPIENT_ABSENT",
      },
    ],
    ...overrides,
  };
}
