import { getTimelineTransitionLabel } from "../constants/orderDetailConstants";

function pushEvents(target, events, kind) {
  (events || []).forEach((event) => {
    if (!event?.occurredAt) return;
    target.push({
      id: event.id || `${kind}-${event.occurredAt}-${event.newStatus}`,
      label: getTimelineTransitionLabel(event),
      occurredAt: event.occurredAt,
      kind,
      newStatus: event.newStatus,
    });
  });
}

export function buildOrderTimelineEvents(track) {
  if (!track) return [];

  const merged = [];
  pushEvents(merged, track.orderTimeline, "order");
  pushEvents(merged, track.payment?.timeline, "payment");

  (track.shipments || []).forEach((shipment, index) => {
    pushEvents(merged, shipment.timeline, `shipment-${index}`);
  });

  merged.sort((a, b) => new Date(a.occurredAt) - new Date(b.occurredAt));

  return merged.map((event, index) => ({
    ...event,
    isLatest: index === merged.length - 1,
  }));
}
