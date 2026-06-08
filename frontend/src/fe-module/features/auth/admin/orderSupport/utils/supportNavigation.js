const SUPPORT_PARAM_KEYS = [
  "orderId",
  "paymentId",
  "shipmentId",
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
    status,
    from,
    to,
    page,
    size,
  } = webhookFilters;

  if (provider) next.set("provider", provider);
  if (referenceId) next.set("reference_id", referenceId);
  if (status) next.set("status", status);
  if (from) next.set("from", from);
  if (to) next.set("to", to);
  if (page) next.set("page", String(page));
  if (size) next.set("size", String(size));

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
