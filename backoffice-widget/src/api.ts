export type ParcelStatus =
  | "REGISTERED"
  | "PICKED_UP"
  | "IN_TRANSIT"
  | "OUT_FOR_DELIVERY"
  | "DELIVERY_FAILED"
  | "DELIVERED"
  | "RETURNED_TO_SENDER"
  | "CANCELLED";

export type AttemptReason =
  | "REFUSED_BY_RECIPIENT"
  | "ADDRESS_ISSUE"
  | "BUSINESS_CLOSED"
  | "RECIPIENT_ABSENT"
  | "OTHER";

export interface ParcelSummary {
  parcelId: string;
  trackingNumber: string;
  carrierCode: string;
  orderReference: string;
  status: ParcelStatus;
  late: boolean;
  registeredAt: string;
}

export interface ParcelEvent {
  type: string;
  occurredAt: string;
  location: string | null;
  attemptReason: AttemptReason | null;
}

export interface ParcelDetail {
  parcelId: string;
  trackingNumber: string;
  carrierCode: string;
  sellerId: number;
  orderReference: string;
  status: ParcelStatus;
  deliveryInstructions: string | null;
  pickedUpOn: string | null;
  deliveredAt: string | null;
  lateDetectedAt: string | null;
  registeredAt: string;
  events: ParcelEvent[];
}

export const PAGE_SIZE = 100;

export type Fetch = (input: string, init?: RequestInit) => Promise<Response>;

export class ParcelsApiError extends Error {
  readonly status: number;

  constructor(status: number, body: string) {
    super(`Back-office API answered ${status}: ${body}`);
    this.name = "ParcelsApiError";
    this.status = status;
  }
}

export function parcelsApi(baseUrl: string, fetchFn: Fetch = fetch) {
  async function getJson<T>(path: string): Promise<T> {
    const response = await fetchFn(`${baseUrl}${path}`, {
      credentials: "include",
      headers: { Accept: "application/json" },
    });
    if (!response.ok) {
      throw new ParcelsApiError(response.status, await response.text());
    }
    return (await response.json()) as T;
  }

  return {
    listParcels(page: number): Promise<ParcelSummary[]> {
      return getJson(`/back-office/v1/parcels?page=${page}&size=${PAGE_SIZE}`);
    },
    getParcel(parcelId: string): Promise<ParcelDetail> {
      return getJson(`/back-office/v1/parcels/${encodeURIComponent(parcelId)}`);
    },
  };
}
