import axios from "axios";
import { authApiClient } from "../../../services/http/authApiClient";
import { resolveDevServiceBaseUrl } from "../../../services/http/resolveDevServiceBaseUrl";
import { getOAuthAuthorizationUrl } from "../utils/oauthRedirectUrls";
import { mapAxiosError, unwrapResponse } from "../../../services/http/apiResponse";

const AUTH_BASE_URL = resolveDevServiceBaseUrl("auth", process.env.EXPO_PUBLIC_AUTH_SERVICE_BASE_URL);

export async function loginWithEmail(payload) {
  try {
    const response = await authApiClient.post("/api/v1/auth/login", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function registerWithEmail(payload) {
  try {
    const response = await authApiClient.post("/api/v1/auth/register", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function changePassword(payload) {
  try {
    const response = await authApiClient.post("/api/v1/auth/change-password", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function forgotPassword(payload) {
  try {
    const response = await authApiClient.post("/api/v1/auth/forgot-password", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function verifyEmail(payload) {
  try {
    const response = await authApiClient.post("/api/v1/auth/verify-email", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function resendEmailVerification(payload) {
  try {
    const response = await authApiClient.post("/api/v1/auth/resend-email-verification", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchOAuthSession() {
  try {
    const response = await axios.get(`${AUTH_BASE_URL}/api/v1/auth/oauth/session`, {
      withCredentials: true,
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function exchangeOAuthCode(code) {
  try {
    const response = await axios.post(`${AUTH_BASE_URL}/api/v1/auth/oauth/exchange`, { code });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export function getOAuthRedirectUrl(provider) {
  return getOAuthAuthorizationUrl(provider);
}

export async function getLoginSessions() {
  try {
    const response = await authApiClient.get("/api/v1/users/me/sessions");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function logoutAllSessions() {
  try {
    const response = await authApiClient.post("/api/v1/users/me/sessions/logout-all");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function getLoginHistory({ limit = 20, offset = 0 } = {}) {
  try {
    const response = await authApiClient.get("/api/v1/users/me/login-history", {
      params: { limit, offset },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchPublicUserProfile(userId) {
  try {
    const response = await authApiClient.get(`/api/v1/users/${userId}/public-profile`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function logoutWithRefreshToken(refreshToken) {
  try {
    const response = await authApiClient.post("/api/v1/auth/logout", {
      refresh_token: refreshToken,
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function getMyProfile() {
  try {
    const response = await authApiClient.get("/api/v1/users/me");
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateMyProfile(payload) {
  try {
    const response = await authApiClient.put("/api/v1/users/me/profile", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function requestAvatarUploadUrl(payload) {
  try {
    const response = await authApiClient.post("/api/v1/users/me/avatar/upload-url", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateMyAvatar(payload) {
  try {
    const response = await authApiClient.patch("/api/v1/users/me/avatar", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateMyPrivacy(payload) {
  try {
    const response = await authApiClient.patch("/api/v1/users/me/privacy", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateMySettings(payload) {
  try {
    const response = await authApiClient.patch("/api/v1/users/me/settings", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function softDeleteMyAccount(payload) {
  try {
    const response = await authApiClient.post("/api/v1/users/me/soft-delete", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
