import axios from "axios";
import { apiClient } from "../../../services/http/apiClient";

const AUTH_BASE_URL = import.meta.env.VITE_AUTH_SERVICE_BASE_URL || "";

const httpClient = axios.create({
  baseURL: AUTH_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

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

export async function loginWithEmail(payload) {
  try {
    const response = await httpClient.post("/api/v1/auth/login", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function registerWithEmail(payload) {
  try {
    const response = await httpClient.post("/api/v1/auth/register", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function changePassword(payload, accessToken) {
  try {
    const response = await httpClient.post("/api/v1/auth/change-password", payload, {
      headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function forgotPassword(payload) {
  try {
    const response = await httpClient.post("/api/v1/auth/forgot-password", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function verifyEmail(payload) {
  try {
    const response = await httpClient.post("/api/v1/auth/verify-email", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function logoutWithRefreshToken(refreshToken) {
  try {
    const response = await httpClient.post("/api/v1/auth/logout", {
      refresh_token: refreshToken,
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export function getOAuthRedirectUrl(provider) {
  const providers = {
    google: "/oauth2/authorization/google",
    facebook: "/oauth2/authorization/facebook",
  };

  const endpoint = providers[provider];
  if (!endpoint) return "";
  return `${AUTH_BASE_URL}${endpoint}`;
}

export async function getMyProfile() {
  try {
    const response = await apiClient.get("/api/v1/users/me");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateMyProfile(payload) {
  try {
    const response = await apiClient.put("/api/v1/users/me/profile", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function requestAvatarUploadUrl(payload) {
  try {
    const response = await apiClient.post("/api/v1/users/me/avatar/upload-url", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateMyAvatar(payload) {
  try {
    const response = await apiClient.patch("/api/v1/users/me/avatar", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateMyPrivacy(payload) {
  try {
    const response = await apiClient.patch("/api/v1/users/me/privacy", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateMySettings(payload) {
  try {
    const response = await apiClient.patch("/api/v1/users/me/settings", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function softDeleteMyAccount(payload) {
  try {
    const response = await apiClient.post("/api/v1/users/me/soft-delete", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function getLoginSessions() {
  try {
    const response = await apiClient.get("/api/v1/users/me/sessions");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function logoutAllSessions() {
  try {
    const response = await apiClient.post("/api/v1/users/me/sessions/logout-all");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function getLoginHistory({ limit = 20, offset = 0 } = {}) {
  try {
    const response = await apiClient.get("/api/v1/users/me/login-history", {
      params: { limit, offset },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function getAdminRoles() {
  try {
    const response = await apiClient.get("/api/v1/admin/roles");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function getRolePermissions(roleId) {
  try {
    const response = await apiClient.get(`/api/v1/admin/roles/${roleId}/permissions`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function getUserPermissions(userId) {
  try {
    const response = await apiClient.get(`/api/v1/admin/users/${userId}/permissions`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function assignRoleToUser(userId, payload) {
  try {
    const response = await apiClient.post(`/api/v1/admin/users/${userId}/roles`, payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function revokeRoleFromUser(userId, roleId) {
  try {
    const response = await apiClient.delete(`/api/v1/admin/users/${userId}/roles/${roleId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function getAdminLoginHistory(userId, { page = 1, limit = 20, success, from, to } = {}) {
  try {
    const params = { page, limit };
    if (success !== undefined && success !== null && success !== "") {
      params.success = success;
    }
    if (from) params.from = from;
    if (to) params.to = to;

    const response = await apiClient.get(`/api/v1/admin/users/${userId}/login-history`, { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function getAdminUserSessions(userId, { page = 1, limit = 20, status = "ACTIVE" } = {}) {
  try {
    const response = await apiClient.get(`/api/v1/admin/users/${userId}/sessions`, {
      params: { page, limit, status },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

