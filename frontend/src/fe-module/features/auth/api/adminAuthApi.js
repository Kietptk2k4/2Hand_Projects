import axios from "axios";

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

export async function loginWithAdminEmail(payload) {
  try {
    const response = await httpClient.post("/api/v1/auth/admin/login", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function logoutAdminWithRefreshToken(refreshToken, accessToken) {
  try {
    const response = await httpClient.post(
      "/api/v1/auth/admin/logout",
      { refresh_token: refreshToken },
      {
        headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : {},
      },
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
