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

async function request(method, url, { params } = {}) {
  try {
    const response = await adminApiClient.request({ method, url, params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function getOrderSupportDetail(orderId) {
  return request("get", `/admin/api/v1/support/orders/${orderId}`);
}

export async function getPaymentSupportDetail(paymentId) {
  return request("get", `/admin/api/v1/support/payments/${paymentId}`);
}

export async function getShipmentSupportDetail(shipmentId) {
  return request("get", `/admin/api/v1/support/shipments/${shipmentId}`);
}

export async function getWebhookLogsForSupport({
  provider,
  reference_id,
  status,
  from,
  to,
  page = 1,
  size = 20,
} = {}) {
  const params = { page, size };
  if (provider) params.provider = provider;
  if (reference_id) params.reference_id = reference_id;
  if (status) params.status = status;
  if (from) params.from = from;
  if (to) params.to = to;
  return request("get", "/admin/api/v1/support/webhook-logs", { params });
}
