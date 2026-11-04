import type { ParcelSummary } from "./api.ts";
import { statusLabels } from "./labels.ts";

export interface ParcelRow {
  parcelId: string;
  trackingNumber: string;
  carrierCode: string;
  orderReference: string;
  statusLabel: string;
  showLateBadge: boolean;
  registeredOn: string;
}

const registeredOnFormat = new Intl.DateTimeFormat("fr-FR", {
  dateStyle: "short",
  timeZone: "Europe/Paris",
});

export function toParcelRows(parcels: ParcelSummary[]): ParcelRow[] {
  return parcels.map((parcel) => ({
    parcelId: parcel.parcelId,
    trackingNumber: parcel.trackingNumber,
    carrierCode: parcel.carrierCode,
    orderReference: parcel.orderReference,
    statusLabel: statusLabels[parcel.status],
    showLateBadge: parcel.late,
    registeredOn: registeredOnFormat.format(new Date(parcel.registeredAt)),
  }));
}
