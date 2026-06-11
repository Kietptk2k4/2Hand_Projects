import { apiClient } from "../../../../../services/http/apiClient";
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

async function request(client, method, url, { params, data } = {}) {
  try {
    const response = await client.request({ method, url, params, data });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function searchInvestigationUsers(query, limit = 20) {
  return request(apiClient, "get", "/api/v1/admin/users/search", {
    params: { query, limit },
  });
}

export async function getUsersForInvestigation({
  status,
  q,
  sort = "created_at",
  page = 1,
  size = 20,
} = {}) {
  const params = { sort, page, size };
  if (status) params.status = status;
  if (q) params.q = q;
  return request(apiClient, "get", "/api/v1/admin/users/investigation", { params });
}

export async function getInvestigationProfile(userId) {
  return request(adminApiClient, "get", `/admin/api/v1/users/${userId}/profile`);
}

export async function getCurrentEnforcements(userId) {
  return request(adminApiClient, "get", `/admin/api/v1/users/${userId}/enforcements/current`);
}

export async function getEnforcementHistory(userId, { page = 1, size = 20 } = {}) {
  return request(adminApiClient, "get", `/admin/api/v1/users/${userId}/enforcements/history`, {
    params: { page, size },
  });
}

export async function getInvestigationLoginHistory(
  userId,
  { page = 1, limit = 20, success, from, to } = {},
) {
  const params = { page, limit };
  if (success !== undefined && success !== null && success !== "") {
    params.success = success;
  }
  if (from) params.from = from;
  if (to) params.to = to;
  return request(apiClient, "get", `/api/v1/admin/users/${userId}/login-history`, { params });
}

export async function getInvestigationUserSessions(userId, { page = 1, limit = 20, status = "ACTIVE" } = {}) {
  return request(apiClient, "get", `/api/v1/admin/users/${userId}/sessions`, {
    params: { page, limit, status },
  });
}

export async function suspendUser(userId, payload) {
  return request(adminApiClient, "post", `/admin/api/v1/users/${userId}/suspend`, { data: payload });
}

export async function banUser(userId, payload) {
  return request(adminApiClient, "post", `/admin/api/v1/users/${userId}/ban`, { data: payload });
}

export async function restrictUser(userId, payload) {
  return request(adminApiClient, "post", `/admin/api/v1/users/${userId}/restrict`, { data: payload });
}

export async function revokeUserEnforcement(enforcementId, payload = {}) {
  return request(adminApiClient, "post", `/admin/api/v1/user-enforcements/${enforcementId}/revoke`, {
    data: payload,
  });
}

export async function revokeAdminSession(sessionId, payload = {}) {
  return request(adminApiClient, "post", `/admin/api/v1/admin-sessions/${sessionId}/revoke`, {
    data: payload,
  });
}
