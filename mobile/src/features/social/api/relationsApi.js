import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

export async function fetchUserRelations(userId, { type, page = 0, size = 20 } = {}) {
  try {
    const response = await socialApiClient.get(`/api/v1/social/users/${userId}/relations`, {
      params: { type, page, size },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
