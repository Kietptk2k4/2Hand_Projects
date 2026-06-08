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

export async function fetchAdminActionLogs(params) {
  return request("get", "/admin/api/v1/admin-action-logs", { params });
}

export async function fetchAdminActionLogDetail(logId) {
  return request("get", `/admin/api/v1/admin-action-logs/${logId}`);
}