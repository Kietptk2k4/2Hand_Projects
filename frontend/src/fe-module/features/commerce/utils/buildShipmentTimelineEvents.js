import { getShipmentTimelineLabel } from "../constants/shipmentTrackingConstants";

export function buildShipmentTimelineEvents(tracking) {
  if (!tracking?.timeline?.length) return [];

  const sorted = [...tracking.timeline].sort(
    (a, b) => new Date(a.occurredAt) - new Date(b.occurredAt),
  );

  return sorted.map((event, index) => ({
    id: event.id || `shipment-tl-${index}-${event.occurredAt}`,
    label: getShipmentTimelineLabel(event),
    rawStatus: event.rawStatus,
    occurredAt: event.occurredAt,
    newStatus: event.newStatus,
    isLatest: index === sorted.length - 1,
  }));
}
