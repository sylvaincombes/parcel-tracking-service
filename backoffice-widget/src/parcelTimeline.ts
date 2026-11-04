import type { ParcelDetail, ParcelEvent } from "./api.ts";
import { attemptReasonLabels, eventTypeLabels } from "./labels.ts";

export interface TimelineEntry {
  occurredAt: string;
  title: string;
  detail: string | null;
}

export function toTimeline(parcel: ParcelDetail): TimelineEntry[] {
  return parcel.events.toReversed().map(toEntry);
}

function toEntry(event: ParcelEvent): TimelineEntry {
  return {
    occurredAt: event.occurredAt,
    title: eventTypeLabels[event.type] ?? event.type,
    detail: event.attemptReason ? attemptReasonLabels[event.attemptReason] : event.location,
  };
}
