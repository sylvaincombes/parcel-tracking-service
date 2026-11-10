import type { ParcelSummary } from "./api.ts";

export type RegistrationOrder = "newest" | "oldest";

type ParcelComparator = (first: ParcelSummary, second: ParcelSummary) => number;

const registrationComparators: Record<RegistrationOrder, ParcelComparator> = {
  newest: (first, second) => registeredAt(second) - registeredAt(first),
  oldest: (first, second) => registeredAt(first) - registeredAt(second),
};

export function sortByRegistration(parcels: ParcelSummary[], direction: RegistrationOrder): ParcelSummary[] {
  return parcels.toSorted(registrationComparators[direction]);
}

function registeredAt(parcel: ParcelSummary): number {
  return Date.parse(parcel.registeredAt);
}
