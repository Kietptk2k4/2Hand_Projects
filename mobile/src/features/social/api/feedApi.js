import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

export async function fetchGlobalFeed({ page = 0, size = 20 } = {}) {
  try {
    const response = await socialApiClient.get("/api/v1/social/feed/global", {
      params: { page, size },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchFollowingFeed({ page = 0, size = 20 } = {}) {
  try {
    const response = await socialApiClient.get("/api/v1/social/feed/following", {
      params: { page, size },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
