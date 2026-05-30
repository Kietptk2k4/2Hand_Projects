import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, normalizeErrors } from "./socialApiResponse";

function unwrapFollowResponse(response) {
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

export async function followUser(userId) {
  try {
    const response = await socialApiClient.post(`/api/v1/social/users/${userId}/follow`);
    return unwrapFollowResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function unfollowUser(userId) {
  try {
    const response = await socialApiClient.delete(`/api/v1/social/users/${userId}/follow`);
    return unwrapFollowResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
