import { authApiClient } from "../../../services/http/authApiClient";
import { mapAxiosError, unwrapResponse } from "../../../services/http/apiResponse";

export async function loginWithEmail(payload) {
  try {
    const response = await authApiClient.post("/api/v1/auth/login", payload);
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
