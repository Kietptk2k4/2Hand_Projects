import { socialApiClient } from "../../../services/http/socialApiClient";
import { mapAxiosError, unwrapResponse } from "./socialApiResponse";

export async function searchPosts({ q, page = 0, size = 20 } = {}) {
  try {
    const response = await socialApiClient.get("/api/v1/social/search/posts", {
      params: { q, page, size },
    });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
