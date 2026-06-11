import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

export async function fetchSavedPosts({ page = 0, size = 20 } = {}) {
  try {
    const response = await socialApiClient.get("/api/v1/social/posts/saved", {
      params: { page, size },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
