import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

export async function fetchSocialProfile(userId) {
  try {
    const response = await socialApiClient.get(`/api/v1/social/users/${userId}/profile`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
