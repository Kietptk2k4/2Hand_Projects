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

export function getOAuthRedirectUrl(provider) {
  const providers = {
    google: "/oauth2/authorization/google",
    facebook: "/oauth2/authorization/facebook",
  };

  const endpoint = providers[provider];
  if (!endpoint) return "";
  return `${AUTH_BASE_URL}${endpoint}`;
}

