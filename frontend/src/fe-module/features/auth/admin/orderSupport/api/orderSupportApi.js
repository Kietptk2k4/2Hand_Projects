import { adminApiClient } from "../../../../../services/http/adminApiClient";

function normalizeErrors(errors) {
  if (!errors) return [];
  if (Array.isArray(errors)) return errors;
  if (typeof errors === "object") {
    return Object.entries(errors).map(([field, reason]) => ({
      field,
      reason: Array.isArray(reason) ? reason[0] : reason,
    }));
  }
  return [];
}

function unwrapResponse(response) {
  const payload = response?.data;
  if (!payload || payload.success !== true) {
    throw {
      code: payload?.code || response?.status || 500,
      message: payload?.message || "Co loi xay ra. Vui long thu lai.",
      errors: normalizeErrors(payload?.errors),
    };
  }
  return payload.data;
}

function mapAxiosError(error) {
  const status = error?.response?.status || 500;
  const payload = error?.response?.data;
  return {
    code: payload?.code || status,
    message: payload?.message || "Co loi xay ra. Vui long thu lai.",
    errors: normalizeErrors(payload?.errors),
  };
}

async function request(method, url, { params, data } = {}) {
  try {
    const response = await adminApiClient.request({ method, url, params, data });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function listOrdersForSupport({
  q,
  status,
  payment_status,
  payment_method,
  from,
  to,
  sort = "created_at",
  page = 1,
  size = 20,
} = {}) {
  const params = { page, size, sort };
  if (q) params.q = q;
  if (status) params.status = status;
  if (payment_status) params.payment_status = payment_status;
  if (payment_method) params.payment_method = payment_method;
  if (from) params.from = from;
  if (to) params.to = to;
  return request("get", "/admin/api/v1/support/orders", { params });
}

export async function getOrderSupportDetail(orderId) {
  return request("get", `/admin/api/v1/support/orders/${orderId}`);
}

export async function getPaymentsForSupport({
  q,
  status,
  reconciliation_status,
  payment_method,
  order_id,
  from,
  to,
  page = 1,
  size = 20,
} = {}) {
  const params = { page, size };
  if (q) params.q = q;
  if (status) params.status = status;
  if (reconciliation_status) params.reconciliation_status = reconciliation_status;
  if (payment_method) params.payment_method = payment_method;
  if (order_id) params.order_id = order_id;
  if (from) params.from = from;
  if (to) params.to = to;
  return request("get", "/admin/api/v1/support/payments", { params });
}

export async function getPaymentSupportDetail(paymentId) {
  return request("get", `/admin/api/v1/support/payments/${paymentId}`);
}

export async function getShipmentSupportDetail(shipmentId) {
  return request("get", `/admin/api/v1/support/shipments/${shipmentId}`);
}

export async function listShipmentSupport({
  q,
  status,
  carrier,
  order_id,
  from,
  to,
  sort = "updated_at",
  page = 1,
  size = 20,
} = {}) {
  const params = { page, size, sort };
  if (q) params.q = q;
  if (status) params.status = status;
  if (carrier) params.carrier = carrier;
  if (order_id) params.order_id = order_id;
  if (from) params.from = from;
  if (to) params.to = to;
  return request("get", "/admin/api/v1/support/shipments", { params });
}

export async function overrideShipmentStatus(shipmentId, { status, reason, force = false } = {}) {
  return request("patch", `/admin/api/v1/support/shipments/${shipmentId}/status`, {
    data: { status, reason, force: Boolean(force) },
  });
}

export async function getWebhookLogsForSupport({
  provider,
  reference_id,
  q,
  event_type,
  status,
  from,
  to,
  page = 1,
  size = 20,
} = {}) {
  const params = { page, size };
  if (provider) params.provider = provider;
  if (reference_id) params.reference_id = reference_id;
  if (q) params.q = q;
  if (event_type) params.event_type = event_type;
  if (status) params.status = status;
  if (from) params.from = from;
  if (to) params.to = to;
  return request("get", "/admin/api/v1/support/webhook-logs", { params });
}

export async function getWebhookLogStatsForSupport({
  provider,
  reference_id,
  q,
  event_type,
  status,
  from,
  to,
} = {}) {
  const params = {};
  if (provider) params.provider = provider;
  if (reference_id) params.reference_id = reference_id;
  if (q) params.q = q;
  if (event_type) params.event_type = event_type;
  if (status) params.status = status;
  if (from) params.from = from;
  if (to) params.to = to;
  return request("get", "/admin/api/v1/support/webhook-logs/stats", { params });
}

export async function getWebhookLogDetailForSupport(logId, provider) {
  return request("get", `/admin/api/v1/support/webhook-logs/${logId}`, {
    params: { provider },
  });
}

export async function exportWebhookLogsForSupport(filters = {}) {
  const params = { format: "csv" };
  const {
    provider,
    reference_id,
    q,
    event_type,
    status,
    from,
    to,
  } = filters;
  if (provider) params.provider = provider;
  if (reference_id) params.reference_id = reference_id;
  if (q) params.q = q;
  if (event_type) params.event_type = event_type;
  if (status) params.status = status;
  if (from) params.from = from;
  if (to) params.to = to;

  try {
    const response = await adminApiClient.request({
      method: "get",
      url: "/admin/api/v1/support/webhook-logs/export",
      params,
      responseType: "blob",
    });
    return response.data;
  } catch (error) {
    throw mapAxiosError(error);
  }
}
