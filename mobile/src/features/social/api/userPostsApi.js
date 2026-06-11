import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

export async function fetchUserPosts(
  userId,
  { page = 0, size = 12, statusFilter = "published" } = {}
) {
  try {
    const response = await socialApiClient.get(`/api/v1/social/users/${userId}/posts`, {
      params: {
        page,
        size,
        status_filter: statusFilter,
      },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
