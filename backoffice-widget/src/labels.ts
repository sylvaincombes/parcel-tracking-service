import type { AttemptReason, ParcelStatus } from "./api.ts";

export const statusLabels: Record<ParcelStatus, string> = {
  REGISTERED: "Label printed",
  PICKED_UP: "Picked up",
  IN_TRANSIT: "In transit",
  OUT_FOR_DELIVERY: "Out for delivery",
  DELIVERY_FAILED: "Delivery attempt failed",
  DELIVERED: "Delivered",
  RETURNED_TO_SENDER: "Returned to you",
  CANCELLED: "Cancelled",
};

export const attemptReasonLabels: Record<AttemptReason, string> = {
  REFUSED_BY_RECIPIENT: "Refused by the customer",
  ADDRESS_ISSUE: "Address problem",
  BUSINESS_CLOSED: "Business closed",
  RECIPIENT_ABSENT: "Customer not at home",
  OTHER: "See carrier message",
};

export const eventTypeLabels: Record<string, string> = {
  PICKED_UP: "Picked up by the carrier",
  IN_TRANSIT: "In transit",
  ARRIVED_AT_HUB: "Arrived at a sorting hub",
  OUT_FOR_DELIVERY: "Out for delivery",
  DELIVERY_ATTEMPT_FAILED: "Delivery attempt failed",
  DELIVERED: "Delivered",
  RETURNED_TO_SENDER: "Returned to you",
};
