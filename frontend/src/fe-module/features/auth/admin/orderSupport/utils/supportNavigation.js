const SUPPORT_PARAM_KEYS = [
  "orderId",
  "paymentId",
  "shipmentId",
  "wh_provider",
  "wh_reference_id",
  "wh_q",
  "wh_event_type",
  "wh_status",
  "wh_from",
  "wh_to",
  "wh_page",
  "wh_size",
  "wh_log_id",
  "wh_log_provider",
  // legacy webhook keys
  "provider",
  "reference_id",
  "status",
  "from",
  "to",
  "page",
  "size",
];

export function isValidUuid(value) {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(
    (value || "").trim(),
  );
}

export function buildSupportSearchParams({
  tab,
  orderId,
  paymentId,
  shipmentId,
  webhookFilters = {},
  preserve,
}) {
  const next = new URLSearchParams();

  if (preserve) {
    for (const [key, value] of preserve.entries()) {
      if (
        key !== "section" &&
        key !== "tab" &&
        !SUPPORT_PARAM_KEYS.includes(key)
      ) {
        next.set(key, value);
      }
    }
  }

  next.set("section", "orderSupport");
  next.set("tab", tab);

  if (orderId) next.set("orderId", orderId);
  if (paymentId) next.set("paymentId", paymentId);
  if (shipmentId) next.set("shipmentId", shipmentId);

  const {
    provider,
    reference_id: referenceId,
    q,
    event_type: eventType,
    status,
    from,
    to,
    page,
    size,
  } = webhookFilters;

  if (provider) next.set("wh_provider", provider);
  if (referenceId) next.set("wh_reference_id", referenceId);
  if (q) next.set("wh_q", q);
  if (eventType) next.set("wh_event_type", eventType);
  if (status) next.set("wh_status", status);
  if (from) next.set("wh_from", from);
  if (to) next.set("wh_to", to);
  if (page) next.set("wh_page", String(page));
  if (size) next.set("wh_size", String(size));

  return next;
}

export function navigateToOrderDetail(orderId, preserve) {
  return buildSupportSearchParams({
    tab: "order-detail",
    orderId,
    preserve,
  });
}

export function navigateToPaymentDetail(paymentId, preserve, orderId) {
  return buildSupportSearchParams({
    tab: "payment-detail",
    paymentId,
    orderId,
    preserve,
  });
}

export function navigateToShipmentDetail(shipmentId, preserve, orderId) {
  return buildSupportSearchParams({
    tab: "shipment-detail",
    shipmentId,
    orderId,
    preserve,
  });
}

export function navigateToWebhookLogs(filters = {}, preserve) {
  return buildSupportSearchParams({
    tab: "webhook-logs",
    webhookFilters: filters,
    preserve,
  });
}
