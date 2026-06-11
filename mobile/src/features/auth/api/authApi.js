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
