import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

export async function fetchTrendingHashtags({ limit = 5 } = {}) {
  try {
    const response = await socialApiClient.get("/api/v1/social/search/trending-hashtags", {
      params: { limit },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchSuggestedUsers({ page = 0, limit = 3, size } = {}) {
  try {
    const response = await socialApiClient.get("/api/v1/social/users/suggestions", {
      params: {
        page,
        limit: size ?? limit,
      },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}