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
export async function fetchSystemAnnouncements(params) {
  return request("get", "/admin/api/v1/system-announcements", { params });
}

export async function createSystemAnnouncement(body) {
  return request("post", "/admin/api/v1/system-announcements", { data: body });
}

export async function publishSystemAnnouncement(announcementId, body) {
  return request("post", `/admin/api/v1/system-announcements/${announcementId}/publish`, { data: body });
}

export async function pinSystemAnnouncement(announcementId, body) {
  return request("patch", `/admin/api/v1/system-announcements/${announcementId}/pin`, { data: body });
}

export async function cancelSystemAnnouncement(announcementId) {
  return request("post", `/admin/api/v1/system-announcements/${announcementId}/cancel`);
}