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
      message: payload?.message || "Có lỗi xảy ra. Vui lòng thử lại.",
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
    message: payload?.message || "Có lỗi xảy ra. Vui lòng thử lại.",
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
export async function fetchSystemConfigs(params) {
  return request("get", "/admin/api/v1/system-configs", { params });
}

export async function createSystemConfig(body) {
  return request("post", "/admin/api/v1/system-configs", { data: body });
}

export async function updateSystemConfig(configId, body) {
  return request("patch", `/admin/api/v1/system-configs/${configId}`, { data: body });
}

export async function toggleSystemConfig(configId, body) {
  return request("patch", `/admin/api/v1/system-configs/${configId}/toggle`, { data: body });
}

export async function fetchSystemConfigHistory(configId, params) {
  return request("get", `/admin/api/v1/system-configs/${configId}/history`, { params });
}