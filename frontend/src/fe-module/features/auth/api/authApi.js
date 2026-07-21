import axios from "axios";
import { apiClient } from "../../../services/http/apiClient";
import { attachNgrokGatewayInterceptor, ngrokGatewayHeaderObject } from "../../../services/http/ngrokGatewayHeaders";
import { resolveServiceBaseUrl } from "../../../services/http/resolveServiceBaseUrl";

const AUTH_BASE_URL = resolveServiceBaseUrl(import.meta.env.VITE_AUTH_SERVICE_BASE_URL);

const httpClient = axios.create({
  baseURL: AUTH_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

attachNgrokGatewayInterceptor(httpClient);

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

export async function resendEmailVerification(payload) {
  try {
    const response = await httpClient.post("/api/v1/auth/resend-email-verification", payload);
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

export async function fetchOAuthSession() {
  try {
    const response = await axios.get(`${AUTH_BASE_URL}/api/v1/auth/oauth/session`, {
      withCredentials: true,
      headers: ngrokGatewayHeaderObject(AUTH_BASE_URL),
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

  const redirectUri =
    typeof window !== "undefined"
      ? `${window.location.origin}/oauth/success`
      : "";
  const baseUrl = `${AUTH_BASE_URL}${endpoint}`;
  if (!redirectUri) {
    return baseUrl;
  }

  const params = new URLSearchParams({ redirect_uri: redirectUri });
  return `${baseUrl}?${params.toString()}`;
}

export async function getMyProfile() {
  try {
    const response = await apiClient.get("/api/v1/users/me");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchPublicUserProfile(userId) {
  try {
    const response = await apiClient.get(`/api/v1/users/${userId}/public-profile`);
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

export async function requestCoverUploadUrl(payload) {
  try {
    const response = await apiClient.post("/api/v1/users/me/cover/upload-url", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateMyCover(payload) {
  try {
    const response = await apiClient.patch("/api/v1/users/me/cover", payload);
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

export async function createAdminRole(payload) {
  try {
    const response = await apiClient.post("/api/v1/admin/roles", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateAdminRole(roleId, payload) {
  try {
    const response = await apiClient.patch(`/api/v1/admin/roles/${roleId}`, payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function deleteAdminRole(roleId) {
  try {
    const response = await apiClient.delete(`/api/v1/admin/roles/${roleId}`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function getUsersForRbac({ status, q, sort = "email", page = 1, size = 20 } = {}) {
  try {
    const params = { sort, page, size };
    if (status) params.status = status;
    if (q) params.q = q;
    const response = await apiClient.get("/api/v1/admin/users", { params });
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

export async function getPermissionCatalog() {
  try {
    const response = await apiClient.get("/api/v1/admin/permissions");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function assignPermissionToRole(roleId, payload) {
  try {
    const response = await apiClient.post(`/api/v1/admin/roles/${roleId}/permissions`, payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function revokePermissionFromRole(roleId, permissionCode) {
  try {
    const response = await apiClient.delete(
      `/api/v1/admin/roles/${roleId}/permissions/${encodeURIComponent(permissionCode)}`,
    );
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

